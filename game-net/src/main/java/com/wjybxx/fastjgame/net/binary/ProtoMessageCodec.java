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

import com.google.protobuf.*;
import com.wjybxx.fastjgame.net.misc.MessageMapper;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Map;

/**
 * protoBuf消息编解码支持
 * <p>
 * messageId 使用大端模式写入，和json序列化方式一致，也方便客户端解析
 *
 * @author wjybxx
 * @version 1.0
 * date - 2020/2/17
 */
class ProtoMessageCodec implements BinaryCodec<AbstractMessage> {

    private final MessageMapper messageMapper;
    /**
     * proto message 解析方法
     */
    private final Map<Class<?>, Parser<?>> parserMap;

    ProtoMessageCodec(MessageMapper messageMapper, Map<Class<?>, Parser<?>> parserMap) {
        this.messageMapper = messageMapper;
        this.parserMap = parserMap;
    }

    @Override
    public boolean isSupport(Class<?> runtimeType) {
        return parserMap.containsKey(runtimeType);
    }

    @Override
    public void writeDataNoTag(CodedOutputStream outputStream, @Nonnull AbstractMessage instance) throws Exception {
        int messageId = messageMapper.getMessageId(instance.getClass());

        writeMessageId(outputStream, messageId);

        outputStream.writeMessageNoTag(instance);
    }

    // 大端模式写入一个int
    private static void writeMessageId(CodedOutputStream outputStream, int messageId) throws IOException {
        outputStream.writeRawByte((messageId >>> 24));
        outputStream.writeRawByte((messageId >>> 16));
        outputStream.writeRawByte((messageId >>> 8));
        outputStream.writeRawByte(messageId);
    }

    private static int readMessageId(CodedInputStream inputStream) throws IOException {
        return (inputStream.readRawByte() & 0xFF) << 24
                | (inputStream.readRawByte() & 0xFF) << 16
                | (inputStream.readRawByte() & 0xFF) << 8
                | inputStream.readRawByte() & 0xFF;
    }

    @Nonnull
    @Override
    public AbstractMessage readData(CodedInputStream inputStream) throws Exception {
        // 大端模式读取一个int
        final int messageId = readMessageId(inputStream);
        final Class<?> messageClazz = messageMapper.getMessageClass(messageId);
        @SuppressWarnings("unchecked") final Parser<AbstractMessage> parser = (Parser<AbstractMessage>) parserMap.get(messageClazz);
        return inputStream.readMessage(parser, ExtensionRegistryLite.getEmptyRegistry());
    }

    @Override
    public byte getWireType() {
        return WireType.PROTO_MESSAGE;
    }

}
