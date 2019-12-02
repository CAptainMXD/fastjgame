/*
 *  Copyright 2019 wjybxx
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to iBn writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.wjybxx.fastjgame.concurrent.disruptor;

import com.lmax.disruptor.AlertException;
import com.lmax.disruptor.Sequence;
import com.lmax.disruptor.SequenceBarrier;
import com.lmax.disruptor.WaitStrategy;
import com.wjybxx.fastjgame.concurrent.disruptor.BusySpinWaitStrategyFactory.BusySpinWaitStrategy;

import javax.annotation.Nonnull;

/**
 * 该策略下，当没有事件可消费时，使用{@link Thread#yield()}进行等待。<b>
 * 该策略在等待时，延迟较低，但有较高的CPU使用率。但是当其它线程需要CPU资源时，比{@link BusySpinWaitStrategy}更容易让出CPU资源。<b>
 * 当CPU资源足够时，推荐使用该策略。
 * 注意：<p>
 * 1. 每自旋一定次数才会执行一次{@link DisruptorEventLoop#loopOnce()}
 * {@link YieldWaitStrategy}
 *
 * @author wjybxx
 * @version 1.0
 * date - 2019/11/28
 * github - https://github.com/hl845740757
 */
public class YieldWaitStrategyFactory implements WaitStrategyFactory {

    private static final int SPIN_TRIES = 100;
    private static final int DEFAULT_WAIT_TIMES_THRESHOLD = 1024;

    private final int spinTries;
    private final int waitTimesThreshold;

    public YieldWaitStrategyFactory() {
        this(SPIN_TRIES, DEFAULT_WAIT_TIMES_THRESHOLD);
    }

    /**
     * @param spinTries          最大自旋次数，超过等待次数后尝试让出CPU
     * @param waitTimesThreshold 每等待多少次执行一次事件循环
     */
    public YieldWaitStrategyFactory(int spinTries, int waitTimesThreshold) {
        this.spinTries = spinTries;
        this.waitTimesThreshold = waitTimesThreshold;
    }

    @Nonnull
    @Override
    public WaitStrategy newWaitStrategy(DisruptorEventLoop eventLoop) {
        return new YieldWaitStrategy(eventLoop, spinTries, waitTimesThreshold);
    }

    /**
     * {@link YieldWaitStrategy}使用<b>自旋 + yield</b>在屏障上等待生产者生产数据。
     * 特征：该策略有着延迟的较低，较高的吞吐量，以及较高的CPU使用率。 当CPU核心数足够时，建议使用该策略。
     * <p>
     * 注意：该策略将使用100%的cpu，但是当其它线程需要CPU资源时，比{@link BusySpinWaitStrategy}更容易让出CPU资源。
     */
    static class YieldWaitStrategy implements WaitStrategy {

        private final DisruptorEventLoop eventLoop;
        private final int spinTries;
        private final int waitTimesThreshold;

        YieldWaitStrategy(DisruptorEventLoop eventLoop, int spinTries, int waitTimesThreshold) {
            this.eventLoop = eventLoop;
            this.spinTries = spinTries;
            this.waitTimesThreshold = waitTimesThreshold;
        }

        @Override
        public long waitFor(final long sequence, Sequence cursor, final Sequence dependentSequence,
                            final SequenceBarrier barrier) throws AlertException {
            long availableSequence;
            int counter = spinTries;
            int waitTimes = 0;

            while ((availableSequence = dependentSequence.get()) < sequence) {
                counter = applyWaitMethod(barrier, counter);
                // 每隔一段时间执行一次循环
                if (++waitTimes == waitTimesThreshold) {
                    waitTimes = 0;
                    eventLoop.safeLoopOnce();
                }
            }

            return availableSequence;
        }

        @Override
        public void signalAllWhenBlocking() {
            // 没有消费者在这里阻塞，因此什么也不干
        }

        private int applyWaitMethod(final SequenceBarrier barrier, int counter)
                throws AlertException {
            // 检查中断、停止信号
            barrier.checkAlert();

            if (counter > 0) {
                --counter;
            } else {
                Thread.yield();
            }
            return counter;
        }
    }
}
