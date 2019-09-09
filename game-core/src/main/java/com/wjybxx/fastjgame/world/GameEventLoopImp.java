/*
 * Copyright 2019 wjybxx
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wjybxx.fastjgame.world;

import com.google.inject.Injector;
import com.wjybxx.fastjgame.concurrent.RejectedExecutionHandler;
import com.wjybxx.fastjgame.concurrent.SingleThreadEventLoop;
import com.wjybxx.fastjgame.eventloop.NetEventLoopGroup;
import com.wjybxx.fastjgame.module.WorldGroupModule;
import com.wjybxx.fastjgame.timer.DefaultTimerSystem;
import com.wjybxx.fastjgame.timer.FixedDelayHandle;
import com.wjybxx.fastjgame.timer.TimerSystem;
import com.wjybxx.fastjgame.world.GameEventLoopGroupImp.WorldStartInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadFactory;

/**
 * 游戏事件循环基本实现
 *
 * @author wjybxx
 * @version 1.0
 * date - 2019/8/4
 * github - https://github.com/hl845740757
 */
public class GameEventLoopImp extends SingleThreadEventLoop implements GameEventLoop {

    private static final Logger logger = LoggerFactory.getLogger(GameEventLoopImp.class);

    /**
     * 最多执行多少个任务，必须检测一次world循环
     */
    private static final int MAX_BATCH_SIZE = 2048;

    /**
     * 游戏世界需要的网络模块
     */
    private final NetEventLoopGroup netEventLoopGroup;

    /**
     * {@link WorldGroupModule}管理的线程安全的控制器
     */
    private final Injector groupInjector;

    /**
     * 要启动的world信息
     */
    private final WorldStartInfo worldStartInfo;
    private final TimerSystem timerSystem = new DefaultTimerSystem(1);
    /**
     * world
     */
    private World world;

    GameEventLoopImp(@Nullable GameEventLoopGroup parent,
                     @Nonnull ThreadFactory threadFactory,
                     @Nonnull RejectedExecutionHandler rejectedExecutionHandler,
                     @Nonnull NetEventLoopGroup netEventLoopGroup,
                     @Nonnull Injector groupInjector,
                     @Nonnull WorldStartInfo worldStartInfo) {

        super(parent, threadFactory, rejectedExecutionHandler);
        this.netEventLoopGroup = netEventLoopGroup;
        this.groupInjector = groupInjector;
        this.worldStartInfo = worldStartInfo;
    }

    @Override
    protected Queue<Runnable> newTaskQueue(int maxTaskNum) {
        return new ConcurrentLinkedQueue<>();
    }

    @Nullable
    @Override
    public GameEventLoopGroup parent() {
        return (GameEventLoopGroup) super.parent();
    }

    @Nonnull
    @Override
    public GameEventLoop next() {
        return (GameEventLoop) super.next();
    }

    @Override
    protected void init() throws Exception {
        super.init();
        final Injector worldInjector = groupInjector.createChildInjector(worldStartInfo.worldModule);
        // 发布自己，使得world内部可以访问 - 现在的模型下使用threadLocal也是可以的。
        final GameEventLoopMrg gameEventLoopMrg = worldInjector.getInstance(GameEventLoopMrg.class);
        gameEventLoopMrg.publish(this);

        // 创建world并尝试启动
        this.world = worldInjector.getInstance(World.class);
        world.startUp(worldStartInfo.startArgs);
        // init 出现任何异常，都会导致线程关闭，world会在clean的时候调用shutdown
        timerSystem.newFixedDelay(worldStartInfo.frameInterval, this::safeTickWorld);
    }

    @Override
    protected void loop() {
        for (; ; ) {
            // 指定执行任务最大数，避免导致world延迟过高
            runAllTasks(MAX_BATCH_SIZE);

            // 游戏世界刷帧
            timerSystem.tick();

            // 查询是否应该退出了
            if (confirmShutdown()) {
                break;
            }

            // 睡眠1毫秒，避免占用太多cpu
            try {
                Thread.sleep(1);
            } catch (InterruptedException ignore) {
            }
        }
    }

    private void safeTickWorld(FixedDelayHandle handle) {
        try {
            world.tick(System.currentTimeMillis());
        } catch (Exception e) {
            logger.warn("world {}-{} tick caught exception.", world.worldRole(), world.worldGuid(), e);
        }
    }

    @Override
    protected void clean() throws Exception {
        super.clean();

        // 关闭游戏world
        if (world != null) {
            safeShutdownWorld();
        }
    }

    private void safeShutdownWorld() {
        try {
            world.shutdown();
        } catch (Exception ex) {
            logger.warn("world shutdown caught exception.", ex);
        }
    }

    @Nonnull
    @Override
    public NetEventLoopGroup netEventLoopGroup() {
        return netEventLoopGroup;
    }

}
