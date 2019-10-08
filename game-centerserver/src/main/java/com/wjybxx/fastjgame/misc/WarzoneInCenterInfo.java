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

package com.wjybxx.fastjgame.misc;

import com.wjybxx.fastjgame.net.session.Session;

/**
 * WarzoneServer在CenterServer中的信息
 *
 * @author wjybxx
 * @version 1.0
 * date - 2019/5/15 13:51
 * github - https://github.com/hl845740757
 */
public class WarzoneInCenterInfo {

    private final Session session;

    public WarzoneInCenterInfo(Session session) {
        this.session = session;
    }

    public long getWarzoneWorldGuid() {
        return session.remoteGuid();
    }

    public Session getSession() {
        return session;
    }

}
