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

package com.wjybxx.fastjgame.log.imp;

import com.wjybxx.fastjgame.log.core.LogDirector;
import com.wjybxx.fastjgame.utils.JsonUtils;

import javax.annotation.Nonnull;

/**
 * {@link LogDirector}的简单实现，配合{@link DefaultLogBuilder}工作。
 * 该实现将日志转换为json字符串传输，仅仅是为了提高可读性，实际应用中还需要考虑传输量的问题。
 *
 * @author wjybxx
 * @version 1.0
 * date - 2019/12/15
 * github - https://github.com/hl845740757
 */
public class DefaultLogDirector implements LogDirector<DefaultLogBuilder, DefaultLogRecord> {

    @Nonnull
    @Override
    public DefaultLogRecord build(DefaultLogBuilder builder) {
        final String data = JsonUtils.writeAsJson(builder.getDataMap());
        return new DefaultLogRecord(builder.getTopic(), data);
    }

}
