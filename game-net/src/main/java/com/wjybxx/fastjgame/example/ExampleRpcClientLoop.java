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
import com.wjybxx.fastjgame.concurrent.RejectedExecutionHandler;
import com.wjybxx.fastjgame.concurrent.RejectedExecutionHandlers;
import com.wjybxx.fastjgame.concurrent.disruptor.DisruptorEventLoop;
import com.wjybxx.fastjgame.concurrent.disruptor.DisruptorWaitStrategyType;
import com.wjybxx.fastjgame.eventloop.NetContext;
import com.wjybxx.fastjgame.misc.DefaultProtocolDispatcher;
import com.wjybxx.fastjgame.misc.HostAndPort;
import com.wjybxx.fastjgame.net.common.SessionDisconnectAware;
import com.wjybxx.fastjgame.net.local.LocalPort;
import com.wjybxx.fastjgame.net.local.LocalSessionConfig;
import com.wjybxx.fastjgame.net.session.Session;
import com.wjybxx.fastjgame.net.socket.SocketSessionConfig;
import com.wjybxx.fastjgame.utils.NetUtils;
import com.wjybxx.fastjgame.utils.TimeUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * rpc请求客户端示例
 *
 * @version 1.2
 * date - 2019/8/26
 */
public class ExampleRpcClientLoop extends DisruptorEventLoop {

    private final LocalPort localPort;
    /**
     * 是否已建立tcp连接
     */
    private Session session;
    private long startTime;
    private int index;

    public ExampleRpcClientLoop(@Nonnull ThreadFactory threadFactory,
                                @Nonnull RejectedExecutionHandler rejectedExecutionHandler,
                                @Nullable LocalPort localPort) {
        super(null, threadFactory, rejectedExecutionHandler, DisruptorWaitStrategyType.TIMEOUT);
        this.localPort = localPort;
    }

    @Override
    protected long timeoutInNano() {
        return TimeUnit.MILLISECONDS.toNanos(50);
    }

    @Override
    protected void init() throws Exception {
        super.init();
        NetContext netContext = ExampleConstants.netEventLoop.createContext(ExampleConstants.CLIENT_GUID, this);

        if (localPort != null) {
            LocalSessionConfig config = LocalSessionConfig.newBuilder()
                    .setCodec(ExampleConstants.reflectBasedCodec)
                    .setLifecycleAware(new ServerDisconnectAward())
                    .setDispatcher(new DefaultProtocolDispatcher())
                    .build();

            session = netContext.connectLocal(ExampleConstants.SESSION_ID, ExampleConstants.SERVER_GUID, localPort, config).get();
        } else {
            // 必须先启动服务器
            SocketSessionConfig config = SocketSessionConfig.newBuilder()
                    .setCodec(ExampleConstants.reflectBasedCodec)
                    .setLifecycleAware(new ServerDisconnectAward())
                    .setDispatcher(new DefaultProtocolDispatcher())
                    .setAutoReconnect(true)
                    .setRpcCallbackTimeoutMs((int) (5 * TimeUtils.MIN))
                    .setMaxConnectTimes(3)
                    .setMaxVerifyTimes(3)
                    .setVerifyTimeoutMs(3 * 1000)
                    .setAckTimeoutMs(3 * 1000)
                    .setMaxPendingMessages(50)
                    .setMaxCacheMessages(10000)
                    .build();

            final HostAndPort address = new HostAndPort(NetUtils.getLocalIp(), ExampleConstants.tcpPort);
            session = netContext.connectTcp(ExampleConstants.SESSION_ID, ExampleConstants.SERVER_GUID, address, config)
                    .get();
        }
        startTime = System.currentTimeMillis();
    }

    @Override
    protected void loopOnce() {
        if (session == null || System.currentTimeMillis() - startTime > 5 * TimeUtils.MIN) {
            shutdown();
            return;
        }
        sendRequest(index++);
    }

    private void sendRequest(final int index) {
        // 方法无返回值，也可以监听，只要调用的是call, sync, syncCall都可以获知调用结果，就像future
        ExampleRpcServiceRpcProxy.hello("wjybxx- " + index)
                .onSuccess(result -> System.out.println("hello - " + index + " - " + result))
                .call(session);

        ExampleRpcServiceRpcProxy.queryId("wjybxx-" + index)
                .onSuccess(result -> System.out.println("queryId - " + index + " - " + result))
                .call(session);
        // 模拟场景服务器通过网关发送给玩家 - 注意：序列化方式必须一致。
        ExampleRpcServiceRpcProxy.sendToPlayer(12345, "这里后期替换为protoBuf消息")
                .onSuccess(result -> System.out.println("sendToPlayer - " + index + " - invoke success"))
                .call(session);

        // 模拟玩家通过网关发送给场景服务器 - 注意：序列化方式必须一致。
        try {
            ExampleRpcServiceRpcProxy.sendToScene(13245, ExampleConstants.reflectBasedCodec.serializeToBytes("这里后期替换为protoBuf消息"))
                    .onSuccess(result -> System.out.println("sendToScene - " + index + " - invoke success"))
                    .call(session);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param result rpc调用结果
     * @param index  保存的索引(一个简单的上下文)
     */
    private void afterQueryId(Integer result, int index) {
        System.out.println("saferQueryId - " + index + " - " + result);
    }

    @Override
    protected void clean() throws Exception {
        super.clean();
        ExampleConstants.netEventLoop.shutdown();
    }

    private class ServerDisconnectAward implements SessionDisconnectAware {

        @Override
        public void onSessionDisconnected(Session session) {
            System.out.println(" =========== onSessionDisconnected ==============");
            ExampleRpcClientLoop.this.session = null;
            // 断开连接后关闭
            shutdown();
        }
    }

    public static void main(String[] args) {
        ExampleRpcClientLoop echoClientLoop = new ExampleRpcClientLoop(
                new DefaultThreadFactory("CLIENT"),
                RejectedExecutionHandlers.discard(),
                null);

        // 唤醒线程
        echoClientLoop.execute(() -> {
        });
    }
}
