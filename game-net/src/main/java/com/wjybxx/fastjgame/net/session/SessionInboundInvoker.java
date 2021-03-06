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

import javax.annotation.Nullable;

/**
 * {@link SessionPipeline}入站事件调度器。
 *
 * @author wjybxx
 * @version 1.0
 * date - 2019/9/25
 * github - https://github.com/hl845740757
 */
public interface SessionInboundInvoker {

    /**
     * 当session激活时，向下传递sessionActive事件；
     * 它将导致{@link SessionPipeline}中的下一个{@link SessionInboundHandler#onSessionActive(SessionHandlerContext)}方法被调用。
     */
    void fireSessionActive();

    /**
     * 当session变为未激活时，向下传递sessionInactive事件；
     * 它将导致{@link SessionPipeline}中的下一个{@link SessionInboundHandler#onSessionInactive(SessionHandlerContext)}方法被调用。
     */
    void fireSessionInactive();

    /**
     * 当session读取到一个消息时，向下传递读事件；
     * 它将导致{@link SessionPipeline}中的下一个{@link SessionInboundHandler#read(SessionHandlerContext, Object)} 方法被调用。
     *
     * @param msg 消息内容，由于解码可能失败，因此可能null
     */
    void fireRead(@Nullable Object msg);

    /**
     * 当当某一个{@link SessionInboundHandler}处理事件出现异常时，向下传递异常。
     * 它将导致{@link SessionPipeline}中的下一个{@link SessionInboundHandler#onExceptionCaught(SessionHandlerContext, Throwable)} 方法被调用。
     *
     * @param cause 异常信息
     */
    void fireExceptionCaught(Throwable cause);
}
