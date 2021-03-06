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

package com.wjybxx.fastjgame.utils.misc;

import com.wjybxx.fastjgame.utils.MathUtils;

/**
 * short值对
 *
 * @author wjybxx
 * @version 1.0
 * date - 2019/11/2
 * github - https://github.com/hl845740757
 */
public class ShortPair {

    private final short first;
    private final short second;

    public ShortPair(short first, short second) {
        this.first = first;
        this.second = second;
    }

    public short getFirst() {
        return first;
    }

    public short getSecond() {
        return second;
    }

    public int composeToInt() {
        return MathUtils.composeShortToInt(first, second);
    }

    @Override
    public String toString() {
        return "ShortPair{" +
                "first=" + first +
                ", second=" + second +
                '}';
    }
}
