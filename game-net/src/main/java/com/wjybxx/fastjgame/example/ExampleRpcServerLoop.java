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

package com.wjybxx.fastjgame.example;

import com.wjybxx.fastjgame.concurrent.DefaultThreadFactory;
import com.wjybxx.fastjgame.concurrent.Promise;
import com.wjybxx.fastjgame.concurrent.RejectedExecutionHandler;
import com.wjybxx.fastjgame.concurrent.RejectedExecutionHandlers;
import com.wjybxx.fastjgame.concurrent.disruptor.DisruptorEventLoop;
import com.wjybxx.fastjgame.concurrent.disruptor.DisruptorWaitStrategyType;
import com.wjybxx.fastjgame.eventloop.NetContext;
import com.wjybxx.fastjgame.misc.DefaultProtocolDispatcher;
import com.wjybxx.fastjgame.net.common.SessionLifecycleAware;
import com.wjybxx.fastjgame.net.local.LocalPort;
import com.wjybxx.fastjgame.net.local.LocalSessionConfig;
import com.wjybxx.fastjgame.net.session.Session;
import com.wjybxx.fastjgame.net.socket.SocketSessionConfig;
import com.wjybxx.fastjgame.utils.ConcurrentUtils;
import com.wjybxx.fastjgame.utils.NetUtils;
import com.wjybxx.fastjgame.utils.TimeUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.ThreadFactory;

/**
 * 示例rpc服务器
 *
 * @author wjybxx
 * @version 1.0
 * date - 2019/9/10
 * github - https://github.com/hl845740757
 */
class ExampleRpcServerLoop extends DisruptorEventLoop {

    private final DefaultProtocolDispatcher protocolDispatcher = new DefaultProtocolDispatcher();

    private final Promise<LocalPort> localPortPromise;

    private long startTime;
    private Session session;

    public ExampleRpcServerLoop(@Nonnull ThreadFactory threadFactory,
                                @Nonnull RejectedExecutionHandler rejectedExecutionHandler,
                                @Nullable Promise<LocalPort> localPortPromise) {
        super(null, threadFactory, rejectedExecutionHandler, DisruptorWaitStrategyType.YIELD);
        this.localPortPromise = localPortPromise;
    }

    @Override
    protected void init() throws Exception {
        super.init();
        // 创建网络环境
        NetContext netContext = ExampleConstants.netEventLoop.createContext(ExampleConstants.SERVER_GUID, this);
        // 注册rpc服务
        ExampleRpcServiceRpcRegister.register(protocolDispatcher, new ExampleRpcService());

        if (localPortPromise != null) {
            // 绑定jvm内部端口
            try {
                LocalSessionConfig config = LocalSessionConfig.newBuilder()
                        .setCodec(ExampleConstants.reflectBasedCodec)
                        .setLifecycleAware(new ClientLifeAware())
                        .setDispatcher(protocolDispatcher)
                        .build();

                final LocalPort localPort = netContext.bindLocal(config);
                localPortPromise.trySuccess(localPort);
            } catch (Exception e) {
                localPortPromise.tryFailure(e);
            }
        } else {
            // 监听tcp端口
            SocketSessionConfig config = SocketSessionConfig.newBuilder()
                    .setCodec(ExampleConstants.reflectBasedCodec)
                    .setLifecycleAware(new ClientLifeAware())
                    .setDispatcher(protocolDispatcher)
                    .setAutoReconnect(true)
                    .setRpcCallbackTimeoutMs((int) TimeUtils.MIN)
                    .setMaxPendingMessages(100)
                    .setMaxCacheMessages(10000)
                    .build();

            netContext.bindTcp(NetUtils.getLocalIp(), ExampleConstants.tcpPort, config);
        }
        startTime = System.currentTimeMillis();
    }

    @Override
    protected void loopOnce() {
        if (System.currentTimeMillis() - startTime > 5 * TimeUtils.MIN) {
            shutdown();
        }
    }

    @Override
    protected void clean() throws Exception {
        super.clean();
        ExampleConstants.netEventLoop.shutdown();
    }

    private class ClientLifeAware implements SessionLifecycleAware {

        @Override
        public void onSessionConnected(Session session) {
            System.out.println("-----------------onSessionConnected----------------------");
            ExampleRpcServerLoop.this.session = session;
        }

        @Override
        public void onSessionDisconnected(Session session) {
            System.out.println("----------------onSessionDisconnected---------------------");
            ExampleRpcServerLoop.this.session = null;
        }
    }

    public static void main(String[] args) {
        final ExampleRpcServerLoop serviceLoop = new ExampleRpcServerLoop(new DefaultThreadFactory("SERVICE"),
                RejectedExecutionHandlers.discard(),
                null);
        // 唤醒线程
        serviceLoop.execute(ConcurrentUtils.NO_OP_TASK);
    }
}
