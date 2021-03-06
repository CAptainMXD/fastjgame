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

package com.wjybxx.fastjgame.utils.concurrent;

/**
 * 受保护的操作，与{@link BlockingOperationException}成一对。
 * 一个只允许当前线程访问，一个不允许当前线程访问。
 *
 * @author wjybxx
 * @version 1.0
 * date - 2020/2/15
 */
public class GuardedOperationException extends RuntimeException {

    public GuardedOperationException() {
    }

    public GuardedOperationException(String message) {
        super(message);
    }

    public GuardedOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    public GuardedOperationException(Throwable cause) {
        super(cause);
    }

}
