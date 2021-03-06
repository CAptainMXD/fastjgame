/*
 *
 *  * Copyright 2019 wjybxx
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.wjybxx.fastjgame.test;

import com.wjybxx.fastjgame.utils.timer.DefaultTimerSystem;
import com.wjybxx.fastjgame.utils.timer.ExceptionHandlers;
import com.wjybxx.fastjgame.utils.timer.TimerHandle;
import com.wjybxx.fastjgame.utils.timer.TimerSystem;
import com.wjybxx.fastjgame.utils.TimeUtils;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.LockSupport;
import java.util.stream.IntStream;

/**
 * 新的定时器系统的测试
 * <p>
 * 节选输出：
 * --------------- sleepTimeSec 9 ---------------------- （初始）
 * timeoutTask1 1565234223563
 * timeoutTask2 1565234223563
 * fixDelayTask 1565234223563
 * fixRateTask 1565234223563
 * fixRateTask 1565234223564
 * fixRateTask 1565234223564
 * <p>
 * --------------- sleepTimeSec 7 ---------------------- （中途）
 * fixRateTask 1565234292568
 * fixRateTask 1565234292568
 * fixRateTask 1565234292568
 * fixDelayTask 1565234292568
 *
 * @author wjybxx
 * @version 1.0
 * date - 2019/8/8
 * github - https://github.com/hl845740757
 */
public class TimerSystemTest {

    @SuppressWarnings("unused")
    public static void main(String[] args) {
        TimerSystem timerSystem = new DefaultTimerSystem();
        // 局部变量是为了调试(debug能获取到引用)
        final TimerHandle handle1 = timerSystem.newTimeout(2 * TimeUtils.SEC, handle -> {
            System.out.println("two second " + System.currentTimeMillis());
        });

        final TimerHandle handle2 = timerSystem.newTimeout(TimeUtils.SEC, handle -> {
            System.out.println("one second " + System.currentTimeMillis());
        });

        final TimerHandle handle3 = timerSystem.newTimeout(2 * TimeUtils.SEC, handle -> {
            System.out.println("two second2 " + System.currentTimeMillis());
        });

        final TimerHandle handle4 = timerSystem.newFixedDelay(0, 3 * TimeUtils.SEC, handle -> {
            System.out.println("fixDelayTask " + System.currentTimeMillis());
        });

        final TimerHandle handle5 = timerSystem.newFixRate(0, 3 * TimeUtils.SEC, handle -> {
            System.out.println("fixRateTask " + System.currentTimeMillis());
        });

        final TimerHandle handle6 = timerSystem.newFixRate(0, 5 * TimeUtils.SEC, handle -> {
            throw new RuntimeException("timer6 LOG");
        }).setExceptionHandler(ExceptionHandlers.LOG);

        final TimerHandle handle7 = timerSystem.newFixRate(0, 5 * TimeUtils.SEC, handle -> {
            throw new RuntimeException("timer7 AUTO CLOSE");
        }).setExceptionHandler(ExceptionHandlers.CLOSE);

        IntStream.rangeClosed(1, 10).forEach(index -> {
            timerSystem.tick();
            // 睡眠的时长不同，会打乱执行节奏
            int sleepTimeSec = ThreadLocalRandom.current().nextInt(1, 10);
            System.out.println("\n --------------- sleepTimeSec " + sleepTimeSec + " ----------------------");
            LockSupport.parkNanos(TimeUtils.NANO_PER_MILLISECOND * TimeUtils.SEC * sleepTimeSec);
        });

        timerSystem.close();
    }
}
