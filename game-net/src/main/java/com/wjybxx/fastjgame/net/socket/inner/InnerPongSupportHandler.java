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

package com.wjybxx.fastjgame.net.socket.inner;

import com.wjybxx.fastjgame.manager.NetTimeManager;
import com.wjybxx.fastjgame.net.common.ConnectAwareTask;
import com.wjybxx.fastjgame.net.common.DisconnectAwareTask;
import com.wjybxx.fastjgame.net.common.PingPongMessage;
import com.wjybxx.fastjgame.net.session.SessionHandlerContext;
import com.wjybxx.fastjgame.net.session.SessionInboundHandlerAdapter;

/**
 * 心跳响应支持
 *
 * @author wjybxx
 * @version 1.0
 * date - 2019/10/1
 * github - https://github.com/hl845740757
 */
public class InnerPongSupportHandler extends SessionInboundHandlerAdapter {

    private NetTimeManager timeManager;
    private long lastReadTime;
    private long sessionTimeoutMs;

    @Override
    public void init(SessionHandlerContext ctx) throws Exception {
        // 缓存 - 减少栈深度
        timeManager = ctx.managerWrapper().getNetTimeManager();
        lastReadTime = timeManager.getSystemMillTime();
        sessionTimeoutMs = ctx.session().config().getSessionTimeoutMs();
    }

    @Override
    public void tick(SessionHandlerContext ctx) {
        if (timeManager.getSystemMillTime() - lastReadTime > sessionTimeoutMs) {
            // 太长时间未读到对方的消息了
            ctx.session().close();
        }
    }

    @Override
    public void onSessionActive(SessionHandlerContext ctx) throws Exception {
        ctx.localEventLoop().execute(new ConnectAwareTask(ctx.session()));
        ctx.fireSessionActive();
    }

    @Override
    public void onSessionInactive(SessionHandlerContext ctx) throws Exception {
        ctx.localEventLoop().execute(new DisconnectAwareTask(ctx.session()));
        ctx.fireSessionInactive();
    }

    @Override
    public void read(SessionHandlerContext ctx, Object msg) {
        lastReadTime = timeManager.getSystemMillTime();
        // 读取到一个心跳包，立即返回一个心跳包
        if (msg == PingPongMessage.INSTANCE) {
            ctx.session().fireWriteAndFlush(PingPongMessage.INSTANCE);
        } else {
            ctx.fireRead(msg);
        }
    }
}
