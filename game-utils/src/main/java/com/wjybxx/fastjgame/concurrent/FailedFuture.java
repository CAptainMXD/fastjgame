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
package com.wjybxx.fastjgame.concurrent;

import javax.annotation.Nonnull;

/**
 * 表示Future关联的task早已失败。
 * 推荐使用{@link EventLoop#newFailedFuture(Throwable)} 代替使用该future的构造方法。
 *
 * @author wjybxx
 * @version 1.0
 * date - 2019/7/14
 * github - https://github.com/hl845740757
 */
public final class FailedFuture<V> extends CompleteFuture<V> {

    /**
     * 造成失败的原因
     */
    private final Throwable cause;

    /**
     * Creates a new instance.
     *
     * @param executor 该future用于通知的线程池
     * @param cause    任务失败的原因
     */
    public FailedFuture(@Nonnull EventLoop executor, @Nonnull Throwable cause) {
        super(executor);
        this.cause = cause;
    }


    @Nonnull
    @Override
    public Throwable cause() {
        return cause;
    }

    @Override
    public boolean isSuccess() {
        return false;
    }

    @Override
    public V getNow() {
        return null;
    }
}