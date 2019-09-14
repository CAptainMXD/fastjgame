/*
 *    Copyright 2019 wjybxx
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.wjybxx.fastjgame.net;

import com.google.protobuf.AbstractMessage;
import com.wjybxx.fastjgame.enummapper.NumberEnum;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import java.io.IOException;

/**
 * 协议编解码器。<br>
 * 注意：子类实现必须是线程安全的！因为它可能在多个线程中调用。但能保证的是：{@link ProtocolCodec}发布到网络层一定 happens-before 任意编解码方法！<br>
 * 因此建议的实现方式：在传递给网络层之前完成所有的初始化工作，并且所有的编解码工作都不会修改对象的状态。
 * <p>
 * Q: 如何减少{@link #cloneRpcRequest(Object)} {@link #cloneRpcResponse(Object)} {@link #cloneMessage(Object)}消耗？<br>
 * A: 网络层提供最小保证： 基本类型的包装类型、{@link String}、{@link AbstractMessage}、{@link Enum}、{@link NumberEnum}不进行拷贝，因为它们都是不可变对象，可以安全的共享。
 * 不过使用者也需要注意，这些对象是共享的，你不能用它们来加锁。<br>
 * PS: 多线程下对包装类型和字符串加锁是非常危险的，缓存池、常量池的存在可能导致非常严重的问题。
 *
 * @author wjybxx
 * @version 1.0
 * date - 2019/8/18
 * github - https://github.com/hl845740757
 */
@ThreadSafe
public interface ProtocolCodec {

    // ---------------------------------------- RPC请求 ---------------------------------

    /**
     * 编码rpc请求
     *
     * @param bufAllocator buf分配器
     * @param request      rpc请求内容
     * @return rpc请求对应的字节数组，为甚使用{@link ByteBuf}？ 减少中间数组对象，减少垃圾回收。
     */
    ByteBuf encodeRpcRequest(ByteBufAllocator bufAllocator, @Nonnull Object request) throws IOException;

    /**
     * 解码rpc请求
     *
     * @param data byteBuf数据
     * @return rpc请求内容
     */
    Object decodeRpcRequest(ByteBuf data) throws IOException;

    /**
     * 拷贝一个RPC请求。
     * 注意：为了性能和内存考虑，不一定是深拷贝，不可变对象不会进行拷贝。
     *
     * @param request rpc请求内容
     * @return new instance or the same object
     */
    Object cloneRpcRequest(@Nonnull Object request) throws IOException;

    // ----------------------------------------- RPC响应 -------------------------------------

    /**
     * 编码rpc响应结果
     * 当且仅当{@link RpcResponse#getBody() != null}时才会调用。
     *
     * @param bufAllocator buf分配器
     * @param body         rpc响应内容
     * @return rpc响应对应的字节数组
     */
    ByteBuf encodeRpcResponse(ByteBufAllocator bufAllocator, @Nonnull Object body) throws IOException;

    /**
     * 解码rpc响应内容。
     * 当且仅当{@link ByteBuf#readableBytes() > 0}时才会调用。
     *
     * @param data byteBuf数据
     * @return rpc响应内容
     */
    Object decodeRpcResponse(ByteBuf data) throws IOException;

    /**
     * 拷贝一个rpc响应内容。
     * 注意：为了性能和内存考虑，不一定是深拷贝，不可变对象不会进行拷贝。
     *
     * @param body 响应内容。
     * @return new instance or the same object
     */
    Object cloneRpcResponse(@Nonnull Object body) throws IOException;
    // ----------------------------------------- 单向消息 -------------------------------------

    /**
     * 编码一个单向消息
     *
     * @param bufAllocator buf分配器
     * @param message      消息内容
     * @return 消息对应的字节数组
     */
    ByteBuf encodeMessage(ByteBufAllocator bufAllocator, Object message) throws IOException;

    /**
     * 解码一个单向消息
     *
     * @param data byteBuf数据
     * @return 消息内容
     */
    Object decodeMessage(ByteBuf data) throws IOException;

    /**
     * 拷贝一个单向消息/通知。
     * 注意：为了性能和内存考虑，不一定是深拷贝，不可变对象不会进行拷贝。
     *
     * @param message 消息内容
     * @return new instance or the same object
     */
    Object cloneMessage(@Nonnull Object message) throws IOException;
}
