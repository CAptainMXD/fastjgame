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

package com.wjybxx.fastjgame.utils.concurrent.event;

import com.wjybxx.fastjgame.utils.concurrent.EventLoop;

import javax.annotation.concurrent.Immutable;

/**
 * {@link EventLoop}进入终止状态事件
 *
 * @author wjybxx
 * @version 1.0
 * date - 2019/10/26
 * github - https://github.com/hl845740757
 */
@Immutable
public class EventLoopTerminalEvent {

    private final EventLoop terminatedEventLoop;

    public EventLoopTerminalEvent(EventLoop terminatedEventLoop) {
        this.terminatedEventLoop = terminatedEventLoop;
    }

    public EventLoop getTerminatedEventLoop() {
        return terminatedEventLoop;
    }
}
