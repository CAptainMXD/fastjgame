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

package com.wjybxx.fastjgame.utils.async;

import com.wjybxx.fastjgame.utils.concurrent.FutureResult;

/**
 * 执行成功才执行的监听器
 *
 * @author wjybxx
 * @version 1.0
 * date - 2020/1/8
 * github - https://github.com/hl845740757
 */
@FunctionalInterface
public interface GenericSuccessFutureResultListener<FR extends FutureResult<V>, V> extends GenericFutureResultListener<FR, V> {

    @Override
    default void onComplete(FR futureResult) {
        if (futureResult.isSuccess()) {
            onSuccess(futureResult.getNow());
        }
    }

    void onSuccess(V result);
}
