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

package com.wjybxx.fastjgame.apt.serializer;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.wjybxx.fastjgame.apt.core.AbstractGenerator;
import com.wjybxx.fastjgame.apt.utils.AutoUtils;
import com.wjybxx.fastjgame.apt.utils.BeanUtils;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import static com.wjybxx.fastjgame.apt.serializer.SerializableClassProcessor.*;

/**
 * @author wjybxx
 * @version 1.0
 * date - 2020/2/18
 */
class IndexableEntitySerializerGenerator extends AbstractGenerator<SerializableClassProcessor> {

    private static final String INDEX_FIELD_NAME = "wireType_index";

    private TypeName instanceRawTypeName;
    private DeclaredType superDeclaredType;

    private TypeSpec.Builder typeBuilder;
    private CodeBlock.Builder staticCodeBlockBuilder;

    IndexableEntitySerializerGenerator(SerializableClassProcessor processor, TypeElement typeElement) {
        super(processor, typeElement);
    }

    @Override
    public void execute() {
        init();

        createIndexField();

        gen();

    }

    private void init() {
        instanceRawTypeName = TypeName.get(typeUtils.erasure(typeElement.asType()));
        superDeclaredType = typeUtils.getDeclaredType(processor.serializerTypeElement, typeUtils.erasure(typeElement.asType()));

        typeBuilder = TypeSpec.classBuilder(getSerializerClassName(typeElement));
        staticCodeBlockBuilder = CodeBlock.builder();
    }

    private void createIndexField() {
        typeBuilder.addField(byte.class, INDEX_FIELD_NAME, Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL);

        final TypeMirror indexTypeMirror = SerializableClassProcessor.getIndexTypeMirror(typeElement);
        // fieldA = WireType.findType(int.class);
        staticCodeBlockBuilder.addStatement("$L = $T.$L($T.class)",
                INDEX_FIELD_NAME, processor.wireTypeTypeName, FINDTYPE_METHOD_NAME, typeUtils.erasure(indexTypeMirror));
    }

    private void gen() {
        // 获取实例方法
        final MethodSpec getEntityMethod = processor.newGetEntityMethod(superDeclaredType);

        // 写入索引即可 outputStream.writeField(wireTye_index, instance.getIndex())
        final MethodSpec.Builder writeMethodBuilder = processor.newWriteMethodBuilder(superDeclaredType);
        writeMethodBuilder.addStatement("outputStream.$L($L, instance.$L())",
                WRITE_FIELD_METHOD_NAME, INDEX_FIELD_NAME, BeanUtils.GET_INDEX_METHOD_NAME);

        // 读取索引即可 return A.forIndex(InputStream.readField(wireTye_index));
        final MethodSpec.Builder readMethodBuilder = processor.newReadObjectMethodBuilder(superDeclaredType);
        readMethodBuilder.addStatement("return $T.$L(inputStream.$L($L))",
                instanceRawTypeName, BeanUtils.FOR_INDEX_METHOD_NAME, READ_FIELD_METHOD_NAME, INDEX_FIELD_NAME);

        typeBuilder.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addAnnotation(AutoUtils.SUPPRESS_UNCHECKED_ANNOTATION)
                .addAnnotation(processorInfoAnnotation)
                .addStaticBlock(staticCodeBlockBuilder.build())
                .addSuperinterface(TypeName.get(superDeclaredType))
                .addMethod(getEntityMethod)
                .addMethod(writeMethodBuilder.build())
                .addMethod(readMethodBuilder.build());

        // 写入文件
        AutoUtils.writeToFile(typeElement, typeBuilder, elementUtils, messager, filer);
    }
}
