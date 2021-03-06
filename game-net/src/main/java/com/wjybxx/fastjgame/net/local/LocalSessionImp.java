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

package com.wjybxx.fastjgame.net.local;

import com.wjybxx.fastjgame.net.eventloop.NetContext;
import com.wjybxx.fastjgame.net.manager.NetManagerWrapper;
import com.wjybxx.fastjgame.net.misc.SessionRegistry;
import com.wjybxx.fastjgame.net.session.AbstractSession;

/**
 * JVM内部会话
 *
 * @author wjybxx
 * @version 1.0
 * date - 2019/9/9
 * github - https://github.com/hl845740757
 */
public class LocalSessionImp extends AbstractSession implements LocalSession {

    public LocalSessionImp(NetContext netContext, String sessionId, long remoteGuid, LocalSessionConfig config,
                           NetManagerWrapper managerWrapper, SessionRegistry sessionRegistry) {
        super(netContext, sessionId, remoteGuid, config, managerWrapper, sessionRegistry);
    }

    @Override
    public LocalSessionConfig config() {
        return (LocalSessionConfig) super.config();
    }

}
