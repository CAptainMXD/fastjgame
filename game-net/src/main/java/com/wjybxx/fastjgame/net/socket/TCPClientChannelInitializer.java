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

import com.wjybxx.fastjgame.net.eventloop.NetEventLoop;
import com.wjybxx.fastjgame.net.utils.NetUtils;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.ReadTimeoutHandler;

import javax.annotation.concurrent.ThreadSafe;

/**
 * 客户端initializer示例
 *
 * @author wjybxx
 * @version 1.0
 * date - 2019/4/27 22:20
 * github - https://github.com/hl845740757
 */
@ThreadSafe
public class TCPClientChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final String sessionId;
    private final long localGuid;
    private final SocketSessionConfig config;
    private final NetEventLoop netEventLoop;

    public TCPClientChannelInitializer(String sessionId, long localGuid, SocketSessionConfig config, NetEventLoop netEventLoop) {
        this.sessionId = sessionId;
        this.localGuid = localGuid;
        this.config = config;
        this.netEventLoop = netEventLoop;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        // 读超时控制 - 注意：netty的EventLoop虽然支持定时任务任务，但是定时任务对EventLoop非常不友好，要尽量减少这种定时任务。
        pipeline.addLast(NetUtils.READ_TIMEOUT_HANDLER_NAME, new ReadTimeoutHandler(config.readTimeout()));
        pipeline.addLast(new LengthFieldBasedFrameDecoder(config.maxFrameLength(), 0, 4, 0, 4));
        pipeline.addLast(new ClientSocketCodec(config.codec(), sessionId, localGuid, netEventLoop));
    }
}
