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

package com.wjybxx.fastjgame.annotation;

import com.wjybxx.fastjgame.misc.PlayerMessageFunction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 玩家消息订阅者，表示处理玩家发来的该类型消息。<br>
 * <p>
 * 方法必须满足以下要求，否则编译会报错：
 * <li>1. 函数必须是两个参数：第一个必须Player类型参数，第二个参数为具体消息类型参数。 也就是可以转换为{@link PlayerMessageFunction}</li>
 * <li>2. 如果期望订阅多个事件，请使用{@link #subEvents()}声明关注的其它事件。</li>
 * <li>3. 参数不可以带泛型，因为泛型是不具备区分度的，如果存在泛型，那么编译时会报错。</li>
 * <li>4. 参数类型不可以是基本类型，因为发布消息的时候会封装为Object，基本类型会被装箱，会导致问题。</li>
 * <li>5. 方法不能是private - 至少是包级访问权限。 </li>
 * 否则编译时会报错。
 * <pre>{@code
 *      @PlayerMessageSubscribe
 *      public void onMessage(Player player, Object message) {
 *          // do something
 *      }
 * }
 * </pre>
 *
 * @author wjybxx
 * @version 1.0
 * date - 2019/8/25
 * github - https://github.com/hl845740757
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface PlayerMessageSubscribe {

    /**
     * 是否只订阅子事件类型
     * (不为方法参数生成事件注册方法，只为{@link #subEvents()}中的事件生成注册方法)
     */
    boolean onlySubEvents() default false;

    /**
     * 声明需要订阅子事件。
     * 注意：这里声明的类型必须是方法参数的子类型，否则编译错误。
     */
    Class[] subEvents() default {};

}
