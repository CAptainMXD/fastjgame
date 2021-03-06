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

/**
 * 一个字节就占用一个字节 - 不需要使用int32的格式
 */
class ByteCodec implements BinaryCodec<Byte> {

    @Override
    public boolean isSupport(Class<?> runtimeType) {
        return runtimeType == Byte.class;
    }

    @Override
    public void writeDataNoTag(CodedOutputStream outputStream, @Nonnull Byte instance) throws Exception {
        outputStream.writeRawByte(instance);
    }

    @Nonnull
    @Override
    public Byte readData(CodedInputStream inputStream) throws Exception {
        return inputStream.readRawByte();
    }

    @Override
    public byte getWireType() {
        return WireType.BYTE;
    }

}
