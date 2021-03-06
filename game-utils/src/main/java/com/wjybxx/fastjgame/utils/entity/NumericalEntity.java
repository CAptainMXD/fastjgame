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

package com.wjybxx.fastjgame.utils.entity;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * 数值型实体，实体可以转换为数字，通过数字也可以找到对应的实体。
 * 注意查看{@link com.wjybxx.fastjgame.utils.EnumUtils#mapping(NumericalEntity[])}
 * (注解处理器使用到了该类)
 *
 * @author wjybxx
 * @version 1.0
 * date - 2019/6/4 13:35
 * github - https://github.com/hl845740757
 * @apiNote 子类实现必须是不可变对象
 */
@Immutable
public interface NumericalEntity extends IndexableEntity<Integer> {

    int getNumber();

    /**
     * @deprecated use {@link #getNumber()} instead
     */
    @Nonnull
    @Deprecated
    @Override
    default Integer getIndex() {
        return getNumber();
    }

}
