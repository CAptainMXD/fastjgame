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

package com.wjybxx.fastjgame.redis;

import com.wjybxx.fastjgame.async.AsyncMethodHandle;
import com.wjybxx.fastjgame.concurrent.FutureResult;
import com.wjybxx.fastjgame.concurrent.GenericFutureFailureResultListener;
import com.wjybxx.fastjgame.concurrent.GenericFutureResultListener;
import com.wjybxx.fastjgame.concurrent.GenericFutureSuccessResultListener;

import javax.annotation.Nonnull;
import java.util.concurrent.ExecutionException;

/**
 * redis方法句柄，执行主体为{@link RedisService}
 *
 * @author wjybxx
 * @version 1.0
 * date - 2020/1/9
 * github - https://github.com/hl845740757
 */
public interface RedisMethodHandle<V> extends AsyncMethodHandle<RedisService, FutureResult<V>, V> {

    @Override
    void execute(@Nonnull RedisService redisService);

    @Override
    void call(@Nonnull RedisService redisService);

    @Override
    V syncCall(@Nonnull RedisService redisService) throws ExecutionException;

    @Override
    RedisMethodHandle<V> onSuccess(GenericFutureSuccessResultListener<FutureResult<V>, V> listener);

    @Override
    RedisMethodHandle<V> onFailure(GenericFutureFailureResultListener<FutureResult<V>> listener);

    @Override
    RedisMethodHandle<V> onComplete(GenericFutureResultListener<FutureResult<V>> listener);
}