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

package com.wjybxx.fastjgame.net.manager;

import com.google.inject.Inject;
import com.wjybxx.fastjgame.net.eventloop.NetEventLoop;
import com.wjybxx.fastjgame.net.module.NetEventLoopModule;
import com.wjybxx.fastjgame.utils.concurrent.EventLoopHolder;

import javax.annotation.concurrent.NotThreadSafe;

/**
 * NetEventLoop管理器，使得{@link NetEventLoopModule}中的管理器可以获取运行环境。
 *
 * @author wjybxx
 * @version 1.0
 * date - 2019/8/3
 * github - https://github.com/hl845740757
 */
@NotThreadSafe
public class NetEventLoopManager extends EventLoopHolder<NetEventLoop> {

    @Inject
    public NetEventLoopManager() {

    }
}
