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

package com.wjybxx.fastjgame.net.socket.ordered;

import com.wjybxx.fastjgame.manager.NetEventManager;
import com.wjybxx.fastjgame.net.ProtocolCodec;
import com.wjybxx.fastjgame.net.socket.ConnectRequest;
import com.wjybxx.fastjgame.net.socket.ConnectResponse;
import com.wjybxx.fastjgame.net.socket.ConnectResponseEvent;
import com.wjybxx.fastjgame.net.socket.NetMessageType;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.IOException;

/**
 * 客户端用编解码器。
 * 并非真正的非线程安全，而是唯一关联一个会话，没法共享。
 *
 * @author wjybxx
 * @version 1.0
 * date - 2019/5/7 12:10
 * github - https://github.com/hl845740757
 */
@NotThreadSafe
public class OrderedClientCodec extends OrderedBaseCodec {

    /**
     * 该channel关联的本地对象标识
     */
    private final long localGuid;
    /**
     * 该channel关联的远程对象标识
     */
    private final long serverGuid;
    /**
     * 是否已建立链接
     */
    private boolean connect = false;
    /**
     * 用于发布网络事件
     */
    private final NetEventManager netEventManager;

    public OrderedClientCodec(ProtocolCodec codec, long localGuid, long serverGuid, NetEventManager netEventManager) {
        super(codec);
        this.localGuid = localGuid;
        this.serverGuid = serverGuid;
        this.netEventManager = netEventManager;
    }

    // region 编码消息
    @Override
    public void write(ChannelHandlerContext ctx, Object msgTO, ChannelPromise promise) throws Exception {
        if (msgTO instanceof BatchOrderedMessageTO) {
            // 批量协议包
            writeBatchMessage(ctx, (BatchOrderedMessageTO) msgTO);
        } else if (msgTO instanceof SingleOrderedMessageTO) {
            // 单个协议包
            writeSingleMsg(ctx, (SingleOrderedMessageTO) msgTO, promise);
        } else if (msgTO instanceof ConnectRequest) {
            // 请求建立连接包
            writeConnectRequest(ctx, (ConnectRequest) msgTO, promise);
        } else {
            super.write(ctx, msgTO, promise);
        }
    }

    // endregion

    // region 读取消息
    @Override
    protected void readMsg(ChannelHandlerContext ctx, NetMessageType netMessageType, ByteBuf msg) throws IOException {
        switch (netMessageType) {
            case CONNECT_RESPONSE:
                tryReadConnectResponse(ctx, msg);
                break;
            case RPC_REQUEST:
                tryReadRpcRequestMessage(ctx, msg);
                break;
            case RPC_RESPONSE:
                tryReadRpcResponseMessage(ctx, msg);
                break;
            case ONE_WAY_MESSAGE:
                tryReadOneWayMessage(ctx, msg);
                break;
            case PING_PONG:
                tryReadAckPongMessage(ctx, msg);
                break;
            default:
                throw new IOException("unexpected netEventType " + netMessageType);
        }
    }

    /**
     * 服务器返回的建立连接验证结果
     */
    private void tryReadConnectResponse(ChannelHandlerContext ctx, ByteBuf msg) {
        ConnectResponse responseTO = readConnectResponse(msg);
        ConnectResponseEvent connectResponseParam = new ConnectResponseEvent(ctx.channel(), localGuid, serverGuid, responseTO);
        netEventManager.fireConnectResponse(connectResponseParam);

        // 标记为已连接
        if (connectResponseParam.isSuccess()) {
            connect = true;
        }
    }

    /**
     * 尝试读取远程的rpc请求消息
     */
    private void tryReadRpcRequestMessage(ChannelHandlerContext ctx, ByteBuf msg) {
        ensureConnected();

        OrderedMessageEvent orderedMessageEvent = readRpcRequestMessage(ctx.channel(), localGuid, serverGuid, msg);
        netEventManager.fireMessage(orderedMessageEvent);
    }

    /**
     * 读取我发起的Rpc的响应消息
     */
    private void tryReadRpcResponseMessage(ChannelHandlerContext ctx, ByteBuf msg) {
        ensureConnected();

        OrderedMessageEvent orderedMessageEvent = readRpcResponseMessage(ctx.channel(), localGuid, serverGuid, msg);
        netEventManager.fireMessage(orderedMessageEvent);
    }

    /**
     * 读取连接的服务器方发来的单向消息
     */
    private void tryReadOneWayMessage(ChannelHandlerContext ctx, ByteBuf msg) {
        ensureConnected();

        netEventManager.fireMessage(readOneWayMessage(ctx.channel(), localGuid, serverGuid, msg));
    }

    /**
     * 服务器返回的ack-pong包
     */
    private void tryReadAckPongMessage(ChannelHandlerContext ctx, ByteBuf msg) {
        ensureConnected();

        OrderedMessageEvent orderedMessageEvent = readAckPingPongMessage(ctx.channel(), localGuid, serverGuid, msg);
        netEventManager.fireMessage(orderedMessageEvent);
    }
    // endregion

    private void ensureConnected() {
        if (!connect) {
            throw new IllegalStateException();
        }
    }
}