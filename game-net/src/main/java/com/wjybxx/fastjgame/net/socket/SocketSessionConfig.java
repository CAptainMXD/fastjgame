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

package com.wjybxx.fastjgame.net.socket;

import com.wjybxx.fastjgame.net.session.SessionConfig;
import com.wjybxx.fastjgame.utils.CheckUtils;

/**
 * socket连接配置
 * <p>
 * 消息队列中的消息总限制为： {@link #maxPendingMessages} + {@link #maxCacheMessages}，一个不包含另一个，容易配置。
 *
 * @author wjybxx
 * @version 1.0
 * date - 2019/9/26
 * github - https://github.com/hl845740757
 */
public final class SocketSessionConfig extends SessionConfig {

    private static final int MIN_PENDING_MESSAGES = 20;
    private static final int MIN_CACHE_MESSAGE = 100;

    private final int sndBuffer;
    private final int rcvBuffer;
    private final int maxFrameLength;
    private final int readTimeout;
    private final int pingIntervalMs;

    private final int connectTimeoutMs;
    // ------------------------------------- 消息确认机制参数 -----------------------------
    private final boolean autoReconnect;
    private final int maxConnectTimes;

    private final int verifyTimeoutMs;
    private final int maxVerifyTimes;

    private final int ackTimeoutMs;

    private final int maxPendingMessages;
    private final int maxCacheMessages;

    private SocketSessionConfig(SocketSessionConfigBuilder builder) {
        super(builder);
        this.sndBuffer = builder.sndBuffer;
        this.rcvBuffer = builder.rcvBuffer;
        this.maxFrameLength = builder.maxFrameLength;
        this.connectTimeoutMs = builder.connectTimeoutMs;
        this.readTimeout = builder.readTimeout;
        this.pingIntervalMs = builder.pingIntervalMs;

        this.autoReconnect = builder.autoReconnect;
        this.maxConnectTimes = builder.maxConnectTimes;

        this.verifyTimeoutMs = builder.verifyTimeoutMs;
        this.maxVerifyTimes = builder.maxVerifyTimes;

        this.ackTimeoutMs = builder.ackTimeoutMs;
        this.maxPendingMessages = builder.maxPendingMessages;
        this.maxCacheMessages = builder.maxCacheMessages;
    }

    /**
     * @return socket发送缓冲区大小
     */
    public int sndBuffer() {
        return sndBuffer;
    }

    /**
     * @return socket接收缓冲区大小
     */
    public int rcvBuffer() {
        return rcvBuffer;
    }

    /**
     * @return 允许的最大帧长度
     */
    public int maxFrameLength() {
        return maxFrameLength;
    }

    /**
     * @return 建立连接超时时间- 毫秒
     */
    public int connectTimeoutMs() {
        return connectTimeoutMs;
    }

    /**
     * @return 读超时时间 - 秒
     */
    public int readTimeout() {
        return readTimeout;
    }

    /**
     * @return 心跳时间间隔，毫秒
     */
    public long pingIntervalMs() {
        return pingIntervalMs;
    }

    /**
     * @return 是否开启了断线重连/消息确认机制。注意：必须双方都开启，否则消息确认将失败。
     */
    public boolean isAutoReconnect() {
        return autoReconnect;
    }

    /**
     * @return <b>每一次</b>建立socket的最大尝试次数，如果指定次数内无法连接到对方，则关闭session。
     */
    public int maxConnectTimes() {
        return maxConnectTimes;
    }

    /**
     * @return 建立socket后，发起session连接请求的超时时间 （毫秒）
     * (等待建立连接应答的超时时间)
     */
    public int verifyTimeoutMs() {
        return verifyTimeoutMs;
    }

    /**
     * @return 建立socket后的验证阶段的最大尝试验证次数
     */
    public int maxVerifyTimes() {
        return maxVerifyTimes;
    }

    /**
     * @return 一个消息的超时时间 - 毫秒
     */
    public int ackTimeoutMs() {
        return ackTimeoutMs;
    }

    /**
     * @return 消息队列中允许的已发送未确认消息数，一旦到达该阈值，则暂停消息发送 (限流) - 实际是漏桶大小。
     * 注意：该值不建议太大，太大会加大消息重发压力。
     */
    public int maxPendingMessages() {
        return maxPendingMessages;
    }

    /**
     * @return 消息队列中允许缓存的尚未发送的消息总数（还未发送的消息数），一旦到达该值，则关闭session (避免无限缓存，内存溢出)。
     * 注意：该值太小，容易造成session关闭；该值太大，会增加内存压力。
     */
    public int maxCacheMessages() {
        return maxCacheMessages;
    }

    public static SocketSessionConfigBuilder newBuilder() {
        return new SocketSessionConfigBuilder();
    }

    public static class SocketSessionConfigBuilder extends SessionConfigBuilder<SocketSessionConfigBuilder, SocketSessionConfig> {

        private int sndBuffer = 64 * 1024;
        private int rcvBuffer = 64 * 1024;
        private int maxFrameLength = 8 * 1024;
        private int readTimeout = 45;
        private int pingIntervalMs = 5000;
        private int connectTimeoutMs = 10 * 1000;

        private boolean autoReconnect = false;
        private int maxConnectTimes = 3;

        private int verifyTimeoutMs = 10 * 1000;
        private int maxVerifyTimes = 3;

        private int ackTimeoutMs = 5 * 1000;

        private int maxPendingMessages = 50;
        private int maxCacheMessages = 500;

        @Override
        protected void checkParams() {
            super.checkParams();
        }

        public SocketSessionConfigBuilder setSndBuffer(int sndBuffer) {
            this.sndBuffer = CheckUtils.requirePositive(sndBuffer, "sndBuffer");
            return this;
        }

        public SocketSessionConfigBuilder setRcvBuffer(int rcvBuffer) {
            this.rcvBuffer = CheckUtils.requirePositive(rcvBuffer, "rcvBuffer");
            return this;
        }

        public SocketSessionConfigBuilder setMaxFrameLength(int maxFrameLength) {
            this.maxFrameLength = CheckUtils.requirePositive(maxFrameLength, "maxFrameLength");
            return this;
        }

        public SocketSessionConfigBuilder setPingIntervalMs(int pingIntervalMs) {
            this.pingIntervalMs = CheckUtils.requirePositive(pingIntervalMs, "pingIntervalMs");
            return this;
        }

        public SocketSessionConfigBuilder setReadTimeout(int readTimeout) {
            this.readTimeout = CheckUtils.requirePositive(readTimeout, "readTimeout");
            return this;
        }

        public SocketSessionConfigBuilder setConnectTimeoutMs(int connectTimeoutMs) {
            this.connectTimeoutMs = CheckUtils.requirePositive(connectTimeoutMs, "connectTimeoutMs");
            return this;
        }

        public SocketSessionConfigBuilder setAutoReconnect(boolean autoReconnect) {
            this.autoReconnect = autoReconnect;
            return this;
        }

        public SocketSessionConfigBuilder setVerifyTimeoutMs(int verifyTimeoutMs) {
            this.verifyTimeoutMs = CheckUtils.requirePositive(verifyTimeoutMs, "verifyTimeoutMs");
            return this;
        }

        public SocketSessionConfigBuilder setMaxVerifyTimes(int maxVerifyTimes) {
            this.maxVerifyTimes = CheckUtils.requirePositive(maxVerifyTimes, "maxVerifyTimes");
            return this;
        }

        public SocketSessionConfigBuilder setMaxPendingMessages(int maxPendingMessages) {
            this.maxPendingMessages = Math.max(MIN_PENDING_MESSAGES, CheckUtils.requirePositive(maxPendingMessages, "maxPendingMessages"));
            return this;
        }

        public SocketSessionConfigBuilder setMaxCacheMessages(int maxCacheMessages) {
            this.maxCacheMessages = Math.max(MIN_CACHE_MESSAGE, CheckUtils.requirePositive(maxCacheMessages, "maxCacheMessages"));
            return this;
        }

        public SocketSessionConfigBuilder setMaxConnectTimes(int maxConnectTimes) {
            this.maxConnectTimes = CheckUtils.requirePositive(maxConnectTimes, "maxConnectTimes");
            return this;
        }

        public SocketSessionConfigBuilder setAckTimeoutMs(int ackTimeoutMs) {
            this.ackTimeoutMs = CheckUtils.requirePositive(ackTimeoutMs, "ackTimeoutMs");
            return this;
        }

        @Override
        protected SocketSessionConfig newInstance() {
            return new SocketSessionConfig(this);
        }
    }
}
