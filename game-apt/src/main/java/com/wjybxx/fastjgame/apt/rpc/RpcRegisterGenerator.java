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

package com.wjybxx.fastjgame.apt.rpc;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.wjybxx.fastjgame.apt.core.AbstractGenerator;
import com.wjybxx.fastjgame.apt.utils.AutoUtils;
import com.wjybxx.fastjgame.apt.utils.BeanUtils;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import java.util.ArrayList;
import java.util.List;

/**
 * rpc服务端注册类生成器
 *
 * @author wjybxx
 * @version 1.0
 * date - 2020/2/22
 */
class RpcRegisterGenerator extends AbstractGenerator<RpcServiceProcessor> {

    private static final String registry = "registry";
    private static final String instance = "instance";

    private static final String session = "session";
    private static final String methodParams = "methodParams";
    private static final String responseChannel = "responseChannel";

    private final short serviceId;
    private final List<ExecutableElement> rpcMethods;

    RpcRegisterGenerator(RpcServiceProcessor processor, TypeElement typeElement, short serviceId, List<ExecutableElement> rpcMethods) {
        super(processor, typeElement);
        this.serviceId = serviceId;
        this.rpcMethods = rpcMethods;
    }

    @Override
    public void execute() {
        final List<MethodSpec> serverMethodProxyList = new ArrayList<>(rpcMethods.size());
        // 生成代理方法
        for (final ExecutableElement method : rpcMethods) {
            serverMethodProxyList.add(genServerMethodProxy(typeElement, serviceId, method));
        }

        final TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(getServerProxyClassName(typeElement))
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addAnnotation(AutoUtils.SUPPRESS_UNCHECKED_ANNOTATION)
                .addAnnotation(processorInfoAnnotation);

        typeBuilder.addMethods(serverMethodProxyList);

        // 生成注册方法
        typeBuilder.addMethod(genRegisterMethod(typeElement, serverMethodProxyList));

        // 写入文件
        AutoUtils.writeToFile(typeElement, typeBuilder, elementUtils, messager, filer);
    }

    private String getServerProxyClassName(TypeElement typeElement) {
        return typeElement.getSimpleName().toString() + "RpcRegister";
    }

    /**
     * 生成注册方法
     * {@code
     * public static void register(RpcFunctionRegistry registry, T instance) {
     * registerGetMethod1(registry, instance);
     * registerGetMethod2(registry, instance);
     * }
     * }
     *
     * @param typeElement           类信息
     * @param serverProxyMethodList 被代理的服务器方法
     */
    private MethodSpec genRegisterMethod(TypeElement typeElement, List<MethodSpec> serverProxyMethodList) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("register")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(TypeName.VOID)
                .addParameter(processor.registryTypeName, registry)
                .addParameter(TypeName.get(typeElement.asType()), instance);

        // 添加调用
        for (MethodSpec method : serverProxyMethodList) {
            builder.addStatement("$L($L, $L)", method.name, registry, instance);
        }

        return builder.build();
    }

    /**
     * 为某个具体方法生成注册方法，方法分为两类
     * 1. 异步方法 -- 异步方法是指需要responseChannel的方法，由应用层代码告知远程执行结果。
     * 2. 同步方法 -- 同步方法是指不需要responseChannel的方法，由生成的代码告知远程执行结果。
     * <p>
     * 1. 异步方法，那么返回结果的职责就完全交给应用层
     * <pre>
     * {@code
     * 		private static void registerGetMethod1(RpcFunctionRegistry registry, T instance) {
     * 		    registry.register(10001, (session, methodParams, responseChannel) -> {
     * 		       instance.method1(methodParams.get(0), methodParams.get(1), responseChannel);
     *            });
     *        }
     * }
     * </pre>
     * 2. 同步方法，同步方法又分为：有结果和无结果类型，有结果的。
     * 2.1 有结果的，返回直接结果
     * <pre>
     * {@code
     * 		private static void registerGetMethod2(RpcFunctionRegistry registry, T instance) {
     * 		    registry.register(10002, (session, methodParams, responseChannel) -> {
     * 		       try {
     * 		     		V result = instance.method2(methodParams.get(0), methodParams.get(1));
     * 		     	    responseChannel.writeSuccess(result);
     *               } catch(Throwable cause){
     *                  responseChannel.writeFailure(cause)
     *                  ConcurrentUtils.rethrow(cause);
     *               }
     *            });
     *        }
     * }
     * </pre>
     * 2.2 无结果的，返回null，表示执行成功
     * <pre>
     * {@code
     * 		private static void registerGetMethod2(RpcFunctionRegistry registry, T instance) {
     * 		    registry.register(10002, (session, methodParams, responseChannel) -> {
     * 		       try {
     * 		       		instance.method1(methodParams.get(0), methodParams.get(1), responseChannel);
     * 		       	    responseChannel.writeSuccess(null);
     *               } catch(Throwable cause) {
     *                   responseChannel.writeFailure(cause)
     *                   ConcurrentUtils.rethrow(cause);
     *               }
     *            });
     *        }
     * }
     * </pre>
     */
    private MethodSpec genServerMethodProxy(TypeElement typeElement, short serviceId, ExecutableElement method) {
        final Short methodId = processor.getMethodId(method);
        final MethodSpec.Builder builder = MethodSpec.methodBuilder(getServerProxyMethodName(methodId, method))
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                .returns(TypeName.VOID)
                .addParameter(processor.registryTypeName, registry)
                .addParameter(TypeName.get(typeElement.asType()), instance);

        // 双方都必须拷贝泛型变量
        AutoUtils.copyTypeVariables(builder, method);

        builder.addCode("$L.register((short)$L, (short)$L, ($L, $L, $L) -> {\n",
                registry,
                serviceId, methodId,
                session, methodParams, responseChannel);

        final InvokeStatement invokeStatement = genInvokeStatement(method);
        if (invokeStatement.hasResponseChannel) {
            // 异步返回值，交给应用层返回结果
            builder.addStatement(invokeStatement.format, invokeStatement.params.toArray());
        } else {
            // 同步返回结果 - 底层返回结果
            builder.addCode("    try {\n");
            builder.addStatement("    " + invokeStatement.format, invokeStatement.params.toArray());
            if (method.getReturnType().getKind() == TypeKind.VOID) {
                builder.addStatement("        $L.writeSuccess(null)", responseChannel);
            } else {
                builder.addStatement("        $L.writeSuccess(result)", responseChannel);
            }
            builder.addCode("    } catch (Throwable cause) {\n");
            builder.addStatement("        $L.writeFailure(cause)", responseChannel);
            builder.addStatement("        $T.rethrow(cause)", processor.exceptionUtilsTypeName);

            builder.addCode("    }\n");
        }
        builder.addStatement("})");
        return builder.build();
    }

    /**
     * 加上methodId防止重复
     *
     * @param methodId rpc方法唯一键
     * @param method   rpc方法
     * @return 注册该rpc方法的
     */
    private String getServerProxyMethodName(short methodId, ExecutableElement method) {
        return "_register" + BeanUtils.firstCharToUpperCase(method.getSimpleName().toString()) + "_" + methodId;
    }


    /**
     * 生成方法调用代码，没有分号和换行符。
     * {@code Object result = instance.rpcMethod(a, b, c)}
     */
    private RpcRegisterGenerator.InvokeStatement genInvokeStatement(ExecutableElement method) {
        // 缩进
        final StringBuilder format = new StringBuilder("    ");
        final List<Object> params = new ArrayList<>(10);

        if (method.getReturnType().getKind() != TypeKind.VOID) {
            // 声明返回值
            final TypeName returnTypeName = ParameterizedTypeName.get(method.getReturnType());
            format.append("$T result = ");
            params.add(returnTypeName);
        }

        // 调用方法
        format.append("$L.$L(");
        params.add(instance);
        params.add(method.getSimpleName().toString());

        boolean hasResponseChannel = false;
        boolean needDelimiter = false;
        int index = 0;
        for (VariableElement variableElement : method.getParameters()) {
            if (needDelimiter) {
                format.append(", ");
            } else {
                needDelimiter = true;
            }

            if (processor.isSession(variableElement)) {
                format.append(session);
            } else if (processor.isResponseChannel(variableElement)) {
                format.append(responseChannel);
                hasResponseChannel = true;
            } else {
                final TypeName parameterTypeName = ParameterizedTypeName.get(variableElement.asType());
                if (parameterTypeName.isPrimitive()) {
                    // 基本类型需要两次转换，否则可能导致重载问题
                    // (int)((Integer)methodParams.get(index))
                    // eg:
                    // getName(int age);
                    // getName(Integer age);
                    format.append("($T)(($T)$L.get($L))");
                    params.add(parameterTypeName);
                    params.add(parameterTypeName.box());
                } else {
                    format.append("($T)$L.get($L)");
                    params.add(parameterTypeName);
                }
                params.add(methodParams);
                params.add(index);
                index++;
            }
        }
        format.append(")");
        return new RpcRegisterGenerator.InvokeStatement(format.toString(), params, hasResponseChannel);
    }


    private static class InvokeStatement {

        private final String format;
        private final List<Object> params;
        private final boolean hasResponseChannel;

        private InvokeStatement(String format, List<Object> params, boolean hasResponseChannel) {
            this.format = format;
            this.params = params;
            this.hasResponseChannel = hasResponseChannel;
        }
    }
}
