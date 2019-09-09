// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: p_common.proto

package com.wjybxx.fastjgame.protobuffer;

public final class p_common {
    private p_common() {
    }

    public static void registerAllExtensions(
            com.google.protobuf.ExtensionRegistryLite registry) {
    }

    public static void registerAllExtensions(
            com.google.protobuf.ExtensionRegistry registry) {
        registerAllExtensions(
                (com.google.protobuf.ExtensionRegistryLite) registry);
    }

    public interface p_player_dataOrBuilder extends
            // @@protoc_insertion_point(interface_extends:fastjgame.p_player_data)
            com.google.protobuf.MessageOrBuilder {

        /**
         * <code>int64 playerGuid = 1;</code>
         */
        long getPlayerGuid();

        /**
         * <pre>
         * 所属的平台号
         * </pre>
         *
         * <code>int32 platformNumber = 2;</code>
         */
        int getPlatformNumber();

        /**
         * <pre>
         * 逻辑服，注册时的服务器
         * </pre>
         *
         * <code>int32 logicServerId = 3;</code>
         */
        int getLogicServerId();
    }

    /**
     * <pre>
     * 玩家需要序列化的数据
     * </pre>
     * <p>
     * Protobuf type {@code fastjgame.p_player_data}
     */
    public static final class p_player_data extends
            com.google.protobuf.GeneratedMessageV3 implements
            // @@protoc_insertion_point(message_implements:fastjgame.p_player_data)
            p_player_dataOrBuilder {
        private static final long serialVersionUID = 0L;

        // Use p_player_data.newBuilder() to construct.
        private p_player_data(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
            super(builder);
        }

        private p_player_data() {
        }

        @java.lang.Override
        public final com.google.protobuf.UnknownFieldSet
        getUnknownFields() {
            return this.unknownFields;
        }

        private p_player_data(
                com.google.protobuf.CodedInputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            this();
            if (extensionRegistry == null) {
                throw new java.lang.NullPointerException();
            }
            int mutable_bitField0_ = 0;
            com.google.protobuf.UnknownFieldSet.Builder unknownFields =
                    com.google.protobuf.UnknownFieldSet.newBuilder();
            try {
                boolean done = false;
                while (!done) {
                    int tag = input.readTag();
                    switch (tag) {
                        case 0:
                            done = true;
                            break;
                        case 8: {

                            playerGuid_ = input.readInt64();
                            break;
                        }
                        case 16: {

                            platformNumber_ = input.readInt32();
                            break;
                        }
                        case 24: {

                            logicServerId_ = input.readInt32();
                            break;
                        }
                        default: {
                            if (!parseUnknownField(
                                    input, unknownFields, extensionRegistry, tag)) {
                                done = true;
                            }
                            break;
                        }
                    }
                }
            } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                throw e.setUnfinishedMessage(this);
            } catch (java.io.IOException e) {
                throw new com.google.protobuf.InvalidProtocolBufferException(
                        e).setUnfinishedMessage(this);
            } finally {
                this.unknownFields = unknownFields.build();
                makeExtensionsImmutable();
            }
        }

        public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
            return com.wjybxx.fastjgame.protobuffer.p_common.internal_static_fastjgame_p_player_data_descriptor;
        }

        @java.lang.Override
        protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
            return com.wjybxx.fastjgame.protobuffer.p_common.internal_static_fastjgame_p_player_data_fieldAccessorTable
                    .ensureFieldAccessorsInitialized(
                            com.wjybxx.fastjgame.protobuffer.p_common.p_player_data.class, com.wjybxx.fastjgame.protobuffer.p_common.p_player_data.Builder.class);
        }

        public static final int PLAYERGUID_FIELD_NUMBER = 1;
        private long playerGuid_;

        /**
         * <code>int64 playerGuid = 1;</code>
         */
        public long getPlayerGuid() {
            return playerGuid_;
        }

        public static final int PLATFORMNUMBER_FIELD_NUMBER = 2;
        private int platformNumber_;

        /**
         * <pre>
         * 所属的平台号
         * </pre>
         *
         * <code>int32 platformNumber = 2;</code>
         */
        public int getPlatformNumber() {
            return platformNumber_;
        }

        public static final int LOGICSERVERID_FIELD_NUMBER = 3;
        private int logicServerId_;

        /**
         * <pre>
         * 逻辑服，注册时的服务器
         * </pre>
         *
         * <code>int32 logicServerId = 3;</code>
         */
        public int getLogicServerId() {
            return logicServerId_;
        }

        private byte memoizedIsInitialized = -1;

        @java.lang.Override
        public final boolean isInitialized() {
            byte isInitialized = memoizedIsInitialized;
            if (isInitialized == 1) return true;
            if (isInitialized == 0) return false;

            memoizedIsInitialized = 1;
            return true;
        }

        @java.lang.Override
        public void writeTo(com.google.protobuf.CodedOutputStream output)
                throws java.io.IOException {
            if (playerGuid_ != 0L) {
                output.writeInt64(1, playerGuid_);
            }
            if (platformNumber_ != 0) {
                output.writeInt32(2, platformNumber_);
            }
            if (logicServerId_ != 0) {
                output.writeInt32(3, logicServerId_);
            }
            unknownFields.writeTo(output);
        }

        @java.lang.Override
        public int getSerializedSize() {
            int size = memoizedSize;
            if (size != -1) return size;

            size = 0;
            if (playerGuid_ != 0L) {
                size += com.google.protobuf.CodedOutputStream
                        .computeInt64Size(1, playerGuid_);
            }
            if (platformNumber_ != 0) {
                size += com.google.protobuf.CodedOutputStream
                        .computeInt32Size(2, platformNumber_);
            }
            if (logicServerId_ != 0) {
                size += com.google.protobuf.CodedOutputStream
                        .computeInt32Size(3, logicServerId_);
            }
            size += unknownFields.getSerializedSize();
            memoizedSize = size;
            return size;
        }

        @java.lang.Override
        public boolean equals(final java.lang.Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof com.wjybxx.fastjgame.protobuffer.p_common.p_player_data)) {
                return super.equals(obj);
            }
            com.wjybxx.fastjgame.protobuffer.p_common.p_player_data other = (com.wjybxx.fastjgame.protobuffer.p_common.p_player_data) obj;

            if (getPlayerGuid()
                    != other.getPlayerGuid()) return false;
            if (getPlatformNumber()
                    != other.getPlatformNumber()) return false;
            if (getLogicServerId()
                    != other.getLogicServerId()) return false;
            if (!unknownFields.equals(other.unknownFields)) return false;
            return true;
        }

        @java.lang.Override
        public int hashCode() {
            if (memoizedHashCode != 0) {
                return memoizedHashCode;
            }
            int hash = 41;
            hash = (19 * hash) + getDescriptor().hashCode();
            hash = (37 * hash) + PLAYERGUID_FIELD_NUMBER;
            hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
                    getPlayerGuid());
            hash = (37 * hash) + PLATFORMNUMBER_FIELD_NUMBER;
            hash = (53 * hash) + getPlatformNumber();
            hash = (37 * hash) + LOGICSERVERID_FIELD_NUMBER;
            hash = (53 * hash) + getLogicServerId();
            hash = (29 * hash) + unknownFields.hashCode();
            memoizedHashCode = hash;
            return hash;
        }

        public static com.wjybxx.fastjgame.protobuffer.p_common.p_player_data parseFrom(
                java.nio.ByteBuffer data)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static com.wjybxx.fastjgame.protobuffer.p_common.p_player_data parseFrom(
                java.nio.ByteBuffer data,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static com.wjybxx.fastjgame.protobuffer.p_common.p_player_data parseFrom(
                com.google.protobuf.ByteString data)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static com.wjybxx.fastjgame.protobuffer.p_common.p_player_data parseFrom(
                com.google.protobuf.ByteString data,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static com.wjybxx.fastjgame.protobuffer.p_common.p_player_data parseFrom(byte[] data)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data);
        }

        public static com.wjybxx.fastjgame.protobuffer.p_common.p_player_data parseFrom(
                byte[] data,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return PARSER.parseFrom(data, extensionRegistry);
        }

        public static com.wjybxx.fastjgame.protobuffer.p_common.p_player_data parseFrom(java.io.InputStream input)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseWithIOException(PARSER, input);
        }

        public static com.wjybxx.fastjgame.protobuffer.p_common.p_player_data parseFrom(
                java.io.InputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseWithIOException(PARSER, input, extensionRegistry);
        }

        public static com.wjybxx.fastjgame.protobuffer.p_common.p_player_data parseDelimitedFrom(java.io.InputStream input)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseDelimitedWithIOException(PARSER, input);
        }

        public static com.wjybxx.fastjgame.protobuffer.p_common.p_player_data parseDelimitedFrom(
                java.io.InputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
        }

        public static com.wjybxx.fastjgame.protobuffer.p_common.p_player_data parseFrom(
                com.google.protobuf.CodedInputStream input)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseWithIOException(PARSER, input);
        }

        public static com.wjybxx.fastjgame.protobuffer.p_common.p_player_data parseFrom(
                com.google.protobuf.CodedInputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws java.io.IOException {
            return com.google.protobuf.GeneratedMessageV3
                    .parseWithIOException(PARSER, input, extensionRegistry);
        }

        @java.lang.Override
        public Builder newBuilderForType() {
            return newBuilder();
        }

        public static Builder newBuilder() {
            return DEFAULT_INSTANCE.toBuilder();
        }

        public static Builder newBuilder(com.wjybxx.fastjgame.protobuffer.p_common.p_player_data prototype) {
            return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
        }

        @java.lang.Override
        public Builder toBuilder() {
            return this == DEFAULT_INSTANCE
                    ? new Builder() : new Builder().mergeFrom(this);
        }

        @java.lang.Override
        protected Builder newBuilderForType(
                com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
            Builder builder = new Builder(parent);
            return builder;
        }

        /**
         * <pre>
         * 玩家需要序列化的数据
         * </pre>
         * <p>
         * Protobuf type {@code fastjgame.p_player_data}
         */
        public static final class Builder extends
                com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
                // @@protoc_insertion_point(builder_implements:fastjgame.p_player_data)
                com.wjybxx.fastjgame.protobuffer.p_common.p_player_dataOrBuilder {
            public static final com.google.protobuf.Descriptors.Descriptor
            getDescriptor() {
                return com.wjybxx.fastjgame.protobuffer.p_common.internal_static_fastjgame_p_player_data_descriptor;
            }

            @java.lang.Override
            protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
            internalGetFieldAccessorTable() {
                return com.wjybxx.fastjgame.protobuffer.p_common.internal_static_fastjgame_p_player_data_fieldAccessorTable
                        .ensureFieldAccessorsInitialized(
                                com.wjybxx.fastjgame.protobuffer.p_common.p_player_data.class, com.wjybxx.fastjgame.protobuffer.p_common.p_player_data.Builder.class);
            }

            // Construct using com.wjybxx.fastjgame.protobuffer.p_common.p_player_data.newBuilder()
            private Builder() {
                maybeForceBuilderInitialization();
            }

            private Builder(
                    com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
                super(parent);
                maybeForceBuilderInitialization();
            }

            private void maybeForceBuilderInitialization() {
                if (com.google.protobuf.GeneratedMessageV3
                        .alwaysUseFieldBuilders) {
                }
            }

            @java.lang.Override
            public Builder clear() {
                super.clear();
                playerGuid_ = 0L;

                platformNumber_ = 0;

                logicServerId_ = 0;

                return this;
            }

            @java.lang.Override
            public com.google.protobuf.Descriptors.Descriptor
            getDescriptorForType() {
                return com.wjybxx.fastjgame.protobuffer.p_common.internal_static_fastjgame_p_player_data_descriptor;
            }

            @java.lang.Override
            public com.wjybxx.fastjgame.protobuffer.p_common.p_player_data getDefaultInstanceForType() {
                return com.wjybxx.fastjgame.protobuffer.p_common.p_player_data.getDefaultInstance();
            }

            @java.lang.Override
            public com.wjybxx.fastjgame.protobuffer.p_common.p_player_data build() {
                com.wjybxx.fastjgame.protobuffer.p_common.p_player_data result = buildPartial();
                if (!result.isInitialized()) {
                    throw newUninitializedMessageException(result);
                }
                return result;
            }

            @java.lang.Override
            public com.wjybxx.fastjgame.protobuffer.p_common.p_player_data buildPartial() {
                com.wjybxx.fastjgame.protobuffer.p_common.p_player_data result = new com.wjybxx.fastjgame.protobuffer.p_common.p_player_data(this);
                result.playerGuid_ = playerGuid_;
                result.platformNumber_ = platformNumber_;
                result.logicServerId_ = logicServerId_;
                onBuilt();
                return result;
            }

            @java.lang.Override
            public Builder clone() {
                return super.clone();
            }

            @java.lang.Override
            public Builder setField(
                    com.google.protobuf.Descriptors.FieldDescriptor field,
                    java.lang.Object value) {
                return super.setField(field, value);
            }

            @java.lang.Override
            public Builder clearField(
                    com.google.protobuf.Descriptors.FieldDescriptor field) {
                return super.clearField(field);
            }

            @java.lang.Override
            public Builder clearOneof(
                    com.google.protobuf.Descriptors.OneofDescriptor oneof) {
                return super.clearOneof(oneof);
            }

            @java.lang.Override
            public Builder setRepeatedField(
                    com.google.protobuf.Descriptors.FieldDescriptor field,
                    int index, java.lang.Object value) {
                return super.setRepeatedField(field, index, value);
            }

            @java.lang.Override
            public Builder addRepeatedField(
                    com.google.protobuf.Descriptors.FieldDescriptor field,
                    java.lang.Object value) {
                return super.addRepeatedField(field, value);
            }

            @java.lang.Override
            public Builder mergeFrom(com.google.protobuf.Message other) {
                if (other instanceof com.wjybxx.fastjgame.protobuffer.p_common.p_player_data) {
                    return mergeFrom((com.wjybxx.fastjgame.protobuffer.p_common.p_player_data) other);
                } else {
                    super.mergeFrom(other);
                    return this;
                }
            }

            public Builder mergeFrom(com.wjybxx.fastjgame.protobuffer.p_common.p_player_data other) {
                if (other == com.wjybxx.fastjgame.protobuffer.p_common.p_player_data.getDefaultInstance()) return this;
                if (other.getPlayerGuid() != 0L) {
                    setPlayerGuid(other.getPlayerGuid());
                }
                if (other.getPlatformNumber() != 0) {
                    setPlatformNumber(other.getPlatformNumber());
                }
                if (other.getLogicServerId() != 0) {
                    setLogicServerId(other.getLogicServerId());
                }
                this.mergeUnknownFields(other.unknownFields);
                onChanged();
                return this;
            }

            @java.lang.Override
            public final boolean isInitialized() {
                return true;
            }

            @java.lang.Override
            public Builder mergeFrom(
                    com.google.protobuf.CodedInputStream input,
                    com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                    throws java.io.IOException {
                com.wjybxx.fastjgame.protobuffer.p_common.p_player_data parsedMessage = null;
                try {
                    parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
                } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                    parsedMessage = (com.wjybxx.fastjgame.protobuffer.p_common.p_player_data) e.getUnfinishedMessage();
                    throw e.unwrapIOException();
                } finally {
                    if (parsedMessage != null) {
                        mergeFrom(parsedMessage);
                    }
                }
                return this;
            }

            private long playerGuid_;

            /**
             * <code>int64 playerGuid = 1;</code>
             */
            public long getPlayerGuid() {
                return playerGuid_;
            }

            /**
             * <code>int64 playerGuid = 1;</code>
             */
            public Builder setPlayerGuid(long value) {

                playerGuid_ = value;
                onChanged();
                return this;
            }

            /**
             * <code>int64 playerGuid = 1;</code>
             */
            public Builder clearPlayerGuid() {

                playerGuid_ = 0L;
                onChanged();
                return this;
            }

            private int platformNumber_;

            /**
             * <pre>
             * 所属的平台号
             * </pre>
             *
             * <code>int32 platformNumber = 2;</code>
             */
            public int getPlatformNumber() {
                return platformNumber_;
            }

            /**
             * <pre>
             * 所属的平台号
             * </pre>
             *
             * <code>int32 platformNumber = 2;</code>
             */
            public Builder setPlatformNumber(int value) {

                platformNumber_ = value;
                onChanged();
                return this;
            }

            /**
             * <pre>
             * 所属的平台号
             * </pre>
             *
             * <code>int32 platformNumber = 2;</code>
             */
            public Builder clearPlatformNumber() {

                platformNumber_ = 0;
                onChanged();
                return this;
            }

            private int logicServerId_;

            /**
             * <pre>
             * 逻辑服，注册时的服务器
             * </pre>
             *
             * <code>int32 logicServerId = 3;</code>
             */
            public int getLogicServerId() {
                return logicServerId_;
            }

            /**
             * <pre>
             * 逻辑服，注册时的服务器
             * </pre>
             *
             * <code>int32 logicServerId = 3;</code>
             */
            public Builder setLogicServerId(int value) {

                logicServerId_ = value;
                onChanged();
                return this;
            }

            /**
             * <pre>
             * 逻辑服，注册时的服务器
             * </pre>
             *
             * <code>int32 logicServerId = 3;</code>
             */
            public Builder clearLogicServerId() {

                logicServerId_ = 0;
                onChanged();
                return this;
            }

            @java.lang.Override
            public final Builder setUnknownFields(
                    final com.google.protobuf.UnknownFieldSet unknownFields) {
                return super.setUnknownFields(unknownFields);
            }

            @java.lang.Override
            public final Builder mergeUnknownFields(
                    final com.google.protobuf.UnknownFieldSet unknownFields) {
                return super.mergeUnknownFields(unknownFields);
            }


            // @@protoc_insertion_point(builder_scope:fastjgame.p_player_data)
        }

        // @@protoc_insertion_point(class_scope:fastjgame.p_player_data)
        private static final com.wjybxx.fastjgame.protobuffer.p_common.p_player_data DEFAULT_INSTANCE;

        static {
            DEFAULT_INSTANCE = new com.wjybxx.fastjgame.protobuffer.p_common.p_player_data();
        }

        public static com.wjybxx.fastjgame.protobuffer.p_common.p_player_data getDefaultInstance() {
            return DEFAULT_INSTANCE;
        }

        private static final com.google.protobuf.Parser<p_player_data>
                PARSER = new com.google.protobuf.AbstractParser<p_player_data>() {
            @java.lang.Override
            public p_player_data parsePartialFrom(
                    com.google.protobuf.CodedInputStream input,
                    com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                    throws com.google.protobuf.InvalidProtocolBufferException {
                return new p_player_data(input, extensionRegistry);
            }
        };

        public static com.google.protobuf.Parser<p_player_data> parser() {
            return PARSER;
        }

        @java.lang.Override
        public com.google.protobuf.Parser<p_player_data> getParserForType() {
            return PARSER;
        }

        @java.lang.Override
        public com.wjybxx.fastjgame.protobuffer.p_common.p_player_data getDefaultInstanceForType() {
            return DEFAULT_INSTANCE;
        }

    }

    private static final com.google.protobuf.Descriptors.Descriptor
            internal_static_fastjgame_p_player_data_descriptor;
    private static final
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
            internal_static_fastjgame_p_player_data_fieldAccessorTable;

    public static com.google.protobuf.Descriptors.FileDescriptor
    getDescriptor() {
        return descriptor;
    }

    private static com.google.protobuf.Descriptors.FileDescriptor
            descriptor;

    static {
        java.lang.String[] descriptorData = {
                "\n\016p_common.proto\022\tfastjgame\"R\n\rp_player_" +
                        "data\022\022\n\nplayerGuid\030\001 \001(\003\022\026\n\016platformNumb" +
                        "er\030\002 \001(\005\022\025\n\rlogicServerId\030\003 \001(\005B.\n com.w" +
                        "jybxx.fastjgame.protobufferB\010p_commonH\001b" +
                        "\006proto3"
        };
        com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
                new com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner() {
                    public com.google.protobuf.ExtensionRegistry assignDescriptors(
                            com.google.protobuf.Descriptors.FileDescriptor root) {
                        descriptor = root;
                        return null;
                    }
                };
        com.google.protobuf.Descriptors.FileDescriptor
                .internalBuildGeneratedFileFrom(descriptorData,
                        new com.google.protobuf.Descriptors.FileDescriptor[]{
                        }, assigner);
        internal_static_fastjgame_p_player_data_descriptor =
                getDescriptor().getMessageTypes().get(0);
        internal_static_fastjgame_p_player_data_fieldAccessorTable = new
                com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
                internal_static_fastjgame_p_player_data_descriptor,
                new java.lang.String[]{"PlayerGuid", "PlatformNumber", "LogicServerId",});
    }

    // @@protoc_insertion_point(outer_class_scope)
}
