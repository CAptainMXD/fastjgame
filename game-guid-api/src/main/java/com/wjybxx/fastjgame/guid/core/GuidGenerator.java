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

package com.wjybxx.fastjgame.guid.core;

import java.io.Closeable;

/**
 * 64位的GUID生成器。
 * GUID，Globally Unique Identifier，全局唯一标识符。
 * <p>
 * 它只要求相同命名空间下{@link #next()}分配的id不重复，而不同命名空间的id是可以重复的。因此一定要慎重的对待命名空间这件事情。
 * Q：为什么需要命名空间？
 * A：因为业务之间的独立性。
 * eg：我们可以为每条日志分配一个唯一id，它不需要占用玩家的guid资源。
 * <p>
 * 具体的策略由自己决定，数据库，Zookeeper，Redis等等都是可以的。
 * <p>
 * 如果没有必要，千万不要维持全局的生成顺序(如redis的incr指令)，那样的guid确实很好，但是在性能上的损失是巨大的。
 * 建议采用预分配的方式，本地缓存一定数量(如100000个)，本地缓存使用完之后再次申请一部分缓存到本地。
 * 如redis的 Incrby 指令: INCRBY guid 100000
 * <p>
 * 缓存越大越安全(对方挂掉的影响越小)，但容易造成资源浪费，缓存过小又降低了缓存的意义；这个全凭自己估量。
 * 并不强求实现为线程安全，根据自己的需要确定为什么级别。
 *
 * @author wjybxx
 * @version 1.0
 * date - 2020/2/12
 * github - https://github.com/hl845740757
 */
public interface GuidGenerator extends Closeable {

    /**
     * 该生成器的名字(或者说命名空间)
     */
    String name();

    /**
     * 分配一个该生成器所属命名空间下唯一的id。
     *
     * @apiNote 它既不保证连续性，也不保证有序性。
     */
    long next();

    /**
     * 关闭它持有的资源
     */
    void close();
}
