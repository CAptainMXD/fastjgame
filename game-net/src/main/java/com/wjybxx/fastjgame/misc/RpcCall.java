/*
 *  Copyright 2019 wjybxx
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.wjybxx.fastjgame.misc;


import java.util.Collections;
import java.util.List;

/**
 * 较为标准的rpc调用，推荐格式，但是仍然不限制rpc调用的形式！
 *
 * @param <V> 远程调用的返回值类型
 * @author wjybxx
 * @version 1.0
 * date - 2019/8/19
 * github - https://github.com/hl845740757
 */
@SerializableClass
public class RpcCall<V> {

	/**
	 * 调用的远程方法，用于确定一个唯一的方法。不使用类名 + 方法具体参数信息，内容量过于庞大，性能不好。
	 * 不过要做映射需要想一些可靠的方法。
	 */
	@SerializableField(number = 1)
	private final int methodKey;

	/**
	 * 方法参数列表，无参时为{@link Collections#emptyList()}
	 */
	@SerializableField(number = 2)
	private final List<Object> methodParams;

	/**
	 * 是否允许添加回调，不序列化。
	 * 不允许添加回调，也就是单向消息。
	 */
	private final boolean allowCallback;

	// 反射创建对象
	private RpcCall() {
		methodKey = -1;
		methodParams = null;
		allowCallback = false;
	}

	public RpcCall(int methodKey, List<Object> methodParams, boolean allowCallback) {
		this.methodKey = methodKey;
		this.methodParams = methodParams;
		this.allowCallback = allowCallback;
	}

	public int getMethodKey() {
		return methodKey;
	}

	public List<Object> getMethodParams() {
		return methodParams;
	}

	public boolean isAllowCallback() {
		return allowCallback;
	}
}
