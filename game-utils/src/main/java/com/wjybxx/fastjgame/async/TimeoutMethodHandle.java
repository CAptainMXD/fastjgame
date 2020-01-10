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

package com.wjybxx.fastjgame.async;

import com.wjybxx.fastjgame.concurrent.GenericFailureFutureResultListener;
import com.wjybxx.fastjgame.concurrent.GenericFutureResultListener;
import com.wjybxx.fastjgame.concurrent.GenericSuccessFutureResultListener;
import com.wjybxx.fastjgame.concurrent.timeout.GenericTimeoutFutureResultListener;
import com.wjybxx.fastjgame.concurrent.timeout.TimeoutFutureResult;

/**
 * 具有超时时间的异步方法句柄
 *
 * @author wjybxx
 * @version 1.0
 * date - 2020/1/9
 * github - https://github.com/hl845740757
 */
public interface TimeoutMethodHandle<T, FR extends TimeoutFutureResult<V>, V> extends MethodHandle<T, FR, V> {

    @Override
    TimeoutMethodHandle<T, FR, V> onSuccess(GenericSuccessFutureResultListener<FR, V> listener);

    @Override
    TimeoutMethodHandle<T, FR, V> onFailure(GenericFailureFutureResultListener<FR, V> listener);

    @Override
    TimeoutMethodHandle<T, FR, V> onComplete(GenericFutureResultListener<FR, V> listener);

    /**
     * 设置超时失败时执行的回调。
     * 注意：只有当后续调用的是{@link #call(Object)}系列方法时才会有效。
     *
     * @param listener 回调逻辑
     * @return this
     */
    TimeoutMethodHandle<T, FR, V> onTimeout(GenericTimeoutFutureResultListener<FR, V> listener);
}
