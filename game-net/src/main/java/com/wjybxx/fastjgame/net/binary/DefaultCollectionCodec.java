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

package com.wjybxx.fastjgame.net.binary;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;

/**
 * 集合编解码器。
 * 这里仅仅保证有序存储读取的数据，如果出现转型异常或有更具体的序列化需求，请将集合对象放入bean中，
 * 并使用{@link com.wjybxx.fastjgame.db.annotation.Impl}注解提供信息。
 *
 * @author wjybxx
 * @version 1.0
 * date - 2020/2/17
 */
class DefaultCollectionCodec implements BinaryCodec<Collection<?>> {

    private BinaryProtocolCodec binaryProtocolCodec;

    DefaultCollectionCodec(BinaryProtocolCodec binaryProtocolCodec) {
        this.binaryProtocolCodec = binaryProtocolCodec;
    }

    @Override
    public boolean isSupport(Class<?> runtimeType) {
        return Collection.class.isAssignableFrom(runtimeType);
    }

    @Override
    public void writeData(CodedOutputStream outputStream, @Nonnull Collection<?> instance) throws Exception {
        binaryProtocolCodec.writeCollectionImp(outputStream, instance);
    }

    @Nonnull
    @Override
    public Collection<?> readData(CodedInputStream inputStream) throws Exception {
        return binaryProtocolCodec.readCollectionImp(inputStream, ArrayList::new);
    }

    @Override
    public byte getWireType() {
        return WireType.COLLECTION;
    }
}
