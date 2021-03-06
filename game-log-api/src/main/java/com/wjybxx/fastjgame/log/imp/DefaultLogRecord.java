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

package com.wjybxx.fastjgame.log.imp;

/**
 * 默认日志存储格式。
 * 该格式较为通用，可以考虑使用该对象进行中转。
 *
 * @author wjybxx
 * @version 1.0
 * date - 2020/2/11
 * github - https://github.com/hl845740757
 */
public class DefaultLogRecord {

    /**
     * 日志主题 - 主类型
     */
    private final String topic;
    /**
     * 日志内容
     * Q: 为什么是String?
     * A: 我们希望仓库中存储的日志是可读的。
     */
    private final String data;

    public DefaultLogRecord(String topic, String data) {
        this.topic = topic;
        this.data = data;
    }

    public String topic() {
        return topic;
    }

    public String data() {
        return data;
    }
}
