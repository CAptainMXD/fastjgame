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

package com.wjybxx.fastjgame.misc;

import com.wjybxx.fastjgame.async.AbstractMethodHandle;
import com.wjybxx.fastjgame.concurrent.GenericFailureFutureResultListener;
import com.wjybxx.fastjgame.concurrent.GenericFutureResultListener;
import com.wjybxx.fastjgame.concurrent.GenericSuccessFutureResultListener;
import com.wjybxx.fastjgame.concurrent.timeout.GenericTimeoutFutureResultListener;
import com.wjybxx.fastjgame.net.common.RpcCall;
import com.wjybxx.fastjgame.net.common.RpcFutureResult;
import com.wjybxx.fastjgame.net.session.Session;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * {@link RpcBuilder}的默认实现
 *
 * @author wjybxx
 * @version 1.0
 * date - 2019/8/23
 * github - https://github.com/hl845740757
 */
@NotThreadSafe
public class DefaultRpcBuilder<V> extends AbstractMethodHandle<Session, RpcFutureResult<V>, V> implements RpcBuilder<V> {

    /**
     * 远程方法信息
     */
    private RpcCall<V> call;

    /**
     * @param call 一般来讲，是用于转发的RpcCall
     */
    public DefaultRpcBuilder(RpcCall<V> call) {
        this.call = call;
    }

    /**
     * 该方法是生成的代码调用的。
     */
    public DefaultRpcBuilder(int methodKey, List<Object> methodParams, int lazyIndexes, int preIndexes) {
        this.call = new RpcCall<>(methodKey, methodParams, lazyIndexes, preIndexes);
    }

    @Override
    public RpcCall<V> getCall() {
        return call;
    }

    @Override
    public RpcBuilder<V> router(RpcRouter<V> router) {
        this.call = router.route(call).getCall();
        return this;
    }

    @Override
    public void send(@Nonnull Session session) throws IllegalStateException {
        session.send(call);
    }

    @Override
    public void sendAndFlush(Session session) {
        session.sendAndFlush(call);
    }

    @Override
    public void broadcast(@Nonnull Iterable<Session> sessionGroup) throws IllegalStateException {
        for (Session session : sessionGroup) {
            session.send(call);
        }
    }

    @Override
    public final void call(@Nonnull Session session) {
        final GenericFutureResultListener<RpcFutureResult<V>, V> listener = detachListener();
        if (listener == null) {
            session.send(call);
        } else {
            session.<V>call(this.call).addListener(listener);
        }
    }

    @Override
    public void callAndFlush(@Nonnull Session session) {
        final GenericFutureResultListener<RpcFutureResult<V>, V> listener = detachListener();
        if (listener == null) {
            session.sendAndFlush(call);
        } else {
            session.<V>callAndFlush(call).addListener(listener);
        }
    }

    @Override
    public V syncCall(@Nonnull Session session) throws ExecutionException {
        return session.syncCall(call);
    }

    @Override
    public RpcBuilder<V> onSuccess(GenericSuccessFutureResultListener<RpcFutureResult<V>, V> listener) {
        super.onSuccess(listener);
        return this;
    }

    @Override
    public RpcBuilder<V> onFailure(GenericFailureFutureResultListener<RpcFutureResult<V>, V> listener) {
        super.onFailure(listener);
        return this;
    }

    @Override
    public RpcBuilder<V> onComplete(GenericFutureResultListener<RpcFutureResult<V>, V> listener) {
        super.onComplete(listener);
        return this;
    }

    @Override
    public RpcBuilder<V> onTimeout(GenericTimeoutFutureResultListener<RpcFutureResult<V>, V> listener) {
        addListener(listener);
        return this;
    }
}
