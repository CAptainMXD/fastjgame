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

package com.wjybxx.fastjgame.net.session;

import com.wjybxx.fastjgame.net.common.ProtocolCodec;
import com.wjybxx.fastjgame.net.common.ProtocolDispatcher;
import com.wjybxx.fastjgame.net.common.SessionLifecycleAware;
import com.wjybxx.fastjgame.utils.CheckUtils;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.util.Objects;

/**
 * session的一些配置。
 * 目前决定使用不可变对象，可以减少对象数量。
 *
 * @author wjybxx
 * @version 1.0
 * date - 2019/9/26
 * github - https://github.com/hl845740757
 */
@Immutable
public class SessionConfig {

    private final SessionLifecycleAware lifecycleAware;
    private final ProtocolCodec codec;
    private final ProtocolDispatcher dispatcher;
    private final long sessionTimeoutMs;

    private final boolean rpcAvailable;
    private final long rpcCallbackTimeoutMs;
    private final long syncRpcTimeoutMs;

    protected SessionConfig(SessionConfigBuilder builder) {
        this.lifecycleAware = builder.lifecycleAware;
        this.codec = builder.protocolCodec;
        this.dispatcher = builder.protocolDispatcher;
        this.sessionTimeoutMs = builder.sessionTimeoutMs;

        this.rpcAvailable = builder.rpcAvailable;
        this.rpcCallbackTimeoutMs = builder.rpcCallbackTimeoutMs;
        this.syncRpcTimeoutMs = builder.syncRpcTimeoutMs;
    }

    /**
     * @return 生命周期回调
     */
    public SessionLifecycleAware lifecycleAware() {
        return lifecycleAware;
    }

    /**
     * @return 协议内容编解码器
     */
    public ProtocolCodec codec() {
        return codec;
    }

    /**
     * @return 协议内容分发器
     */
    public ProtocolDispatcher dispatcher() {
        return dispatcher;
    }

    /**
     * @return 会话超时时间，毫秒
     */
    public long getSessionTimeoutMs() {
        return sessionTimeoutMs;
    }

    /**
     * @return rpc是否可用(是否启用rpc支持)
     */
    public boolean isRpcAvailable() {
        return rpcAvailable;
    }

    /**
     * @return 异步rpc调用超时时间，毫秒
     */
    public long getRpcCallbackTimeoutMs() {
        return rpcCallbackTimeoutMs;
    }

    /**
     * @return 同步rpc调用超时时间，毫秒
     */
    public long getSyncRpcTimeoutMs() {
        return syncRpcTimeoutMs;
    }

    public static SessionConfigBuilder newBuilder() {
        return new SessionConfigBuilder();
    }

    public static class SessionConfigBuilder<T extends SessionConfigBuilder<T, U>, U extends SessionConfig> {

        private SessionLifecycleAware lifecycleAware;
        private ProtocolCodec protocolCodec;
        private ProtocolDispatcher protocolDispatcher;
        private int sessionTimeoutMs = 60 * 1000;

        private boolean rpcAvailable = true;
        private int rpcCallbackTimeoutMs = 15 * 1000;
        private int syncRpcTimeoutMs = 5 * 1000;

        public T setLifecycleAware(@Nonnull SessionLifecycleAware lifecycleAware) {
            this.lifecycleAware = lifecycleAware;
            return self();
        }

        public T setCodec(@Nonnull ProtocolCodec protocolCodec) {
            this.protocolCodec = protocolCodec;
            return self();
        }

        public T setDispatcher(@Nonnull ProtocolDispatcher protocolDispatcher) {
            this.protocolDispatcher = protocolDispatcher;
            return self();
        }

        public T setSessionTimeoutMs(int sessionTimeoutMs) {
            this.sessionTimeoutMs = CheckUtils.requirePositive(sessionTimeoutMs, "sessionTimeoutMs");
            return self();
        }

        public SessionConfigBuilder<T, U> setRpcAvailable(boolean rpcAvailable) {
            this.rpcAvailable = rpcAvailable;
            return this;
        }

        public T setRpcCallbackTimeoutMs(int rpcCallbackTimeoutMs) {
            this.rpcCallbackTimeoutMs = CheckUtils.requirePositive(rpcCallbackTimeoutMs, "rpcCallbackTimeoutMs");
            return self();
        }

        public T setSyncRpcTimeoutMs(int syncRpcTimeoutMs) {
            this.syncRpcTimeoutMs = CheckUtils.requirePositive(rpcCallbackTimeoutMs, "syncRpcTimeoutMs");
            return self();
        }

        public final U build() {
            checkParams();
            return newInstance();
        }

        @SuppressWarnings("unchecked")
        protected U newInstance() {
            return (U) new SessionConfig(this);
        }

        protected void checkParams() {
            Objects.requireNonNull(lifecycleAware, "lifecycleAware");
            Objects.requireNonNull(protocolCodec, "protocolCodec");
            Objects.requireNonNull(protocolDispatcher, "protocolDispatcher");
        }

        @SuppressWarnings("unchecked")
        protected final T self() {
            return (T) this;
        }

    }

}
