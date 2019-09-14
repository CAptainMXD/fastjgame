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

package com.wjybxx.fastjgame.eventloop;

import com.wjybxx.fastjgame.concurrent.EventLoop;
import com.wjybxx.fastjgame.concurrent.ListenableFuture;
import com.wjybxx.fastjgame.concurrent.Promise;
import com.wjybxx.fastjgame.manager.NetManagerWrapper;
import com.wjybxx.fastjgame.misc.HostAndPort;
import com.wjybxx.fastjgame.misc.PortRange;
import com.wjybxx.fastjgame.net.*;
import com.wjybxx.fastjgame.net.initializer.*;
import com.wjybxx.fastjgame.net.injvm.JVMPort;
import com.wjybxx.fastjgame.utils.ConcurrentUtils;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import java.io.IOException;
import java.net.BindException;
import java.util.Map;

/**
 * NetContext的基本实现
 *
 * @author wjybxx
 * @version 1.0
 * date - 2019/8/3
 * github - https://github.com/hl845740757
 */
@ThreadSafe
class NetContextImp implements NetContext {

    private static final Logger logger = LoggerFactory.getLogger(NetContextImp.class);

    private final long localGuid;
    private final RoleType localRole;
    private final EventLoop localEventLoop;
    private final NetEventLoopImp netEventLoop;
    private final NetManagerWrapper managerWrapper;

    NetContextImp(long localGuid, RoleType localRole, EventLoop localEventLoop,
                  NetEventLoopImp netEventLoop, NetManagerWrapper managerWrapper) {
        this.localGuid = localGuid;
        this.localRole = localRole;
        this.localEventLoop = localEventLoop;
        this.netEventLoop = netEventLoop;
        this.managerWrapper = managerWrapper;

        logger.info("User {}-{} create NetContext!", localRole, localGuid);
    }

    @Override
    public long localGuid() {
        return localGuid;
    }

    @Override
    public RoleType localRole() {
        return localRole;
    }

    @Override
    public EventLoop localEventLoop() {
        return localEventLoop;
    }

    @Override
    public NetEventLoop netEventLoop() {
        return netEventLoop;
    }

    @Override
    public ListenableFuture<?> deregister() {
        // 逻辑层调用
        return netEventLoop.deregisterContext(localGuid);
    }

    void afterRemoved() {
        // 尝试删除自己的痕迹
        managerWrapper.getSocketS2CSessionManager().removeUserSession(localGuid, "deregister");
        managerWrapper.getSocketC2SSessionManager().removeUserSession(localGuid, "deregister");
        managerWrapper.getHttpSessionManager().removeUserSession(localGuid);

        managerWrapper.getJvmc2SSessionManager().removeUserSession(localGuid, "deregister");
        managerWrapper.getJvms2CSessionManager().removeUserSession(localGuid, "deregister");

        logger.info("User {}-{} NetContext removed!", localRole, localGuid);
    }

    private int maxFrameLength() {
        return managerWrapper.getNetConfigManager().maxFrameLength();
    }

    @Override
    public ListenableFuture<HostAndPort> bindTcpRange(String host, PortRange portRange,
                                                      @Nonnull ProtocolCodec codec,
                                                      @Nonnull SessionLifecycleAware lifecycleAware,
                                                      @Nonnull ProtocolDispatcher protocolDispatcher,
                                                      @Nonnull SessionSenderMode sessionSenderMode) {
        final PortContext portContext = new PortContext(lifecycleAware, protocolDispatcher, sessionSenderMode);
        final TCPServerChannelInitializer tcpServerChannelInitializer = new TCPServerChannelInitializer(localGuid, maxFrameLength(),
                codec, portContext, managerWrapper.getNetEventManager());
        return bindRange(host, portRange, tcpServerChannelInitializer);
    }

    @Override
    public ListenableFuture<HostAndPort> bindWSRange(String host, PortRange portRange, String websocketUrl,
                                                     @Nonnull ProtocolCodec codec,
                                                     @Nonnull SessionLifecycleAware lifecycleAware,
                                                     @Nonnull ProtocolDispatcher protocolDispatcher,
                                                     @Nonnull SessionSenderMode sessionSenderMode) {
        final PortContext portContext = new PortContext(lifecycleAware, protocolDispatcher, sessionSenderMode);
        final WsServerChannelInitializer wsServerChannelInitializer = new WsServerChannelInitializer(localGuid, websocketUrl, maxFrameLength(),
                codec, portContext, managerWrapper.getNetEventManager());
        return bindRange(host, portRange, wsServerChannelInitializer);
    }

    private ListenableFuture<HostAndPort> bindRange(String host, PortRange portRange, @Nonnull ChannelInitializer<SocketChannel> initializer) {
        // 这里一定不是网络层，只有逻辑层才会调用bind
        return netEventLoop.submit(() -> {
            try {
                return managerWrapper.getSocketS2CSessionManager().bindRange(this, host, portRange, initializer);
            } catch (BindException e) {
                ConcurrentUtils.rethrow(e);
                // unreachable
                return null;
            }
        });
    }

    @Override
    public ListenableFuture<Session> connectTcp(long remoteGuid, RoleType remoteRole, HostAndPort remoteAddress,
                                                @Nonnull ProtocolCodec codec,
                                                @Nonnull SessionLifecycleAware lifecycleAware,
                                                @Nonnull ProtocolDispatcher protocolDispatcher,
                                                @Nonnull SessionSenderMode sessionSenderMode) {
        final TCPClientChannelInitializer initializer = new TCPClientChannelInitializer(localGuid, remoteGuid, maxFrameLength(),
                codec, managerWrapper.getNetEventManager());
        ChannelInitializerSupplier initializerSupplier = () -> initializer;
        return connect(remoteGuid, remoteRole, remoteAddress, lifecycleAware, protocolDispatcher, sessionSenderMode, initializerSupplier);
    }


    @Override
    public ListenableFuture<Session> connectWS(long remoteGuid, RoleType remoteRole, HostAndPort remoteAddress, String websocketUrl,
                                               @Nonnull ProtocolCodec codec,
                                               @Nonnull SessionLifecycleAware lifecycleAware,
                                               @Nonnull ProtocolDispatcher protocolDispatcher,
                                               @Nonnull SessionSenderMode sessionSenderMode) {
        final WsClientChannelInitializer initializer = new WsClientChannelInitializer(localGuid, remoteGuid, websocketUrl, maxFrameLength(),
                codec, managerWrapper.getNetEventManager());
        ChannelInitializerSupplier initializerSupplier = () -> initializer;
        return connect(remoteGuid, remoteRole, remoteAddress, lifecycleAware, protocolDispatcher, sessionSenderMode, initializerSupplier);
    }

    @Nonnull
    private ListenableFuture<Session> connect(long remoteGuid, RoleType remoteRole, HostAndPort remoteAddress,
                                              @Nonnull SessionLifecycleAware lifecycleAware,
                                              @Nonnull ProtocolDispatcher protocolDispatcher,
                                              @Nonnull SessionSenderMode sessionSenderMode,
                                              @Nonnull ChannelInitializerSupplier initializerSupplier) {
        final Promise<Session> promise = netEventLoop.newPromise();
        // 这里一定不是网络层，只有逻辑层才会调用connect
        netEventLoop.execute(() -> {
            managerWrapper.getSocketC2SSessionManager().connect(this, remoteGuid, remoteRole, remoteAddress,
                    initializerSupplier, lifecycleAware, protocolDispatcher, sessionSenderMode, promise);
        });
        return promise;
    }

    // ------------------------------------------- http 实现 ----------------------------------------

    @Override
    public ListenableFuture<HostAndPort> bindHttpRange(String host, PortRange portRange, @Nonnull HttpRequestDispatcher httpRequestDispatcher) {
        final HttpServerInitializer initializer = new HttpServerInitializer(localGuid, httpRequestDispatcher, managerWrapper.getNetEventManager());
        // 这里一定不是网络层，只有逻辑层才会调用bind
        return netEventLoop.submit(() -> {
            try {
                return managerWrapper.getHttpSessionManager().bindRange(this, host, portRange, initializer);
            } catch (Exception e) {
                ConcurrentUtils.rethrow(e);
                // unreachable
                return null;
            }
        });
    }

    @Override
    public Response syncGet(String url, @Nonnull Map<String, String> params) throws IOException {
        return managerWrapper.getHttpClientManager().syncGet(url, params);
    }

    @Override
    public void asyncGet(String url, @Nonnull Map<String, String> params, @Nonnull OkHttpCallback okHttpCallback) {
        managerWrapper.getHttpClientManager().asyncGet(url, params, localEventLoop, okHttpCallback);
    }

    @Override
    public Response syncPost(String url, @Nonnull Map<String, String> params) throws IOException {
        return managerWrapper.getHttpClientManager().syncPost(url, params);
    }

    @Override
    public void asyncPost(String url, @Nonnull Map<String, String> params, @Nonnull OkHttpCallback okHttpCallback) {
        managerWrapper.getHttpClientManager().asyncPost(url, params, localEventLoop, okHttpCallback);
    }

    // ----------------------------------------------- 本地调用支持 --------------------------------------------


    @Override
    public ListenableFuture<JVMPort> bindInJVM(@Nonnull ProtocolCodec codec,
                                               @Nonnull SessionLifecycleAware lifecycleAware,
                                               @Nonnull ProtocolDispatcher protocolDispatcher,
                                               @Nonnull SessionSenderMode sessionSenderMode) {
        final PortContext portContext = new PortContext(lifecycleAware, protocolDispatcher, sessionSenderMode);
        return netEventLoop.submit(() -> {
            return managerWrapper.getJvms2CSessionManager().bind(this, codec, portContext);
        });
    }

    @Override
    public ListenableFuture<Session> connectInJVM(@Nonnull JVMPort jvmPort,
                                                  @Nonnull SessionLifecycleAware lifecycleAware,
                                                  @Nonnull ProtocolDispatcher protocolDispatcher,
                                                  @Nonnull SessionSenderMode sessionSenderMode) {
        return jvmPort.connect(this, lifecycleAware, protocolDispatcher, sessionSenderMode);
    }
}
