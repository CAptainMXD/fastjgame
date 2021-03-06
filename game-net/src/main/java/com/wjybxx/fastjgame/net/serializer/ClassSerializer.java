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

package com.wjybxx.fastjgame.net.serializer;

import com.wjybxx.fastjgame.net.binary.EntityInputStream;
import com.wjybxx.fastjgame.net.binary.EntityOutputStream;
import com.wjybxx.fastjgame.net.binary.EntitySerializer;

/**
 * {@link Class}对象序列化工具类
 *
 * @author wjybxx
 * @version 1.0
 * date - 2020/2/18
 * github - https://github.com/hl845740757
 */
@SuppressWarnings("unused")
public class ClassSerializer implements EntitySerializer<Class> {

    @Override
    public Class<Class> getEntityClass() {
        return Class.class;
    }

    @Override
    public Class readObject(EntityInputStream inputStream) throws Exception {
        final String canonicalName = inputStream.readString();
        return ClassSerializer.class.getClassLoader().loadClass(canonicalName);
    }

    @Override
    public void writeObject(Class instance, EntityOutputStream outputStream) throws Exception {
        outputStream.writeString(instance.getCanonicalName());
    }

}
