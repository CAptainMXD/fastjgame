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

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import com.wjybxx.fastjgame.apt.core.MyAbstractProcessor;
import com.wjybxx.fastjgame.apt.utils.AutoUtils;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;
import javax.tools.Diagnostic;
import java.util.*;

/**
 * 由于类文件结构比较稳定，API都是基于访问者模式的，完全不能直接获取数据，难受的一比。
 * <p>
 * 注意：使用class对象有限制，只可以使用JDK自带的class 和 该jar包（该项目）中所有的class (包括依赖的jar包)
 * <p>
 * 不在本包中的类，只能使用{@link Element}) 和 {@link javax.lang.model.type.TypeMirror}。
 * 因为需要使用的类可能还没编译，也就不存在对应的class文件，加载不到。
 * 这也是注解处理器必须要打成jar包的原因。不然注解处理器可能都还未被编译。。。。
 * <p>
 *
 * @author wjybxx
 * @version 1.0
 * date - 2019/8/19
 * github - https://github.com/hl845740757
 */
@AutoService(Processor.class)
public class RpcServiceProcessor extends MyAbstractProcessor {

    private static final String METHOD_HANDLE_CANONICAL_NAME = "com.wjybxx.fastjgame.net.misc.RpcMethodHandle";
    private static final String DEFAULT_METHOD_HANDLE_CANONICAL_NAME = "com.wjybxx.fastjgame.net.misc.DefaultRpcMethodHandle";

    private static final String RPC_SERVICE_CANONICAL_NAME = "com.wjybxx.fastjgame.net.annotation.RpcService";
    private static final String RPC_METHOD_CANONICAL_NAME = "com.wjybxx.fastjgame.net.annotation.RpcMethod";

    static final String LAZY_SERIALIZABLE_CANONICAL_NAME = "com.wjybxx.fastjgame.net.annotation.LazySerializable";
    static final String PRE_DESERIALIZE_CANONICAL_NAME = "com.wjybxx.fastjgame.net.annotation.PreDeserializable";

    private static final String REGISTRY_CANONICAL_NAME = "com.wjybxx.fastjgame.net.misc.RpcFunctionRegistry";
    private static final String SESSION_CANONICAL_NAME = "com.wjybxx.fastjgame.net.session.Session";
    private static final String CHANNEL_CANONICAL_NAME = "com.wjybxx.fastjgame.net.common.RpcResponseChannel";

    private static final String EXCEPTION_UTILS_CANONICAL_NAME = "org.apache.commons.lang3.exception.ExceptionUtils";

    private static final String SERVICE_ID_METHOD_NAME = "serviceId";
    private static final String METHOD_ID_METHOD_NAME = "methodId";

    /**
     * 所有的serviceId集合，判断重复。
     * 编译是分模块编译的，每一个模块都是一个新的processor，因此只能检测当前模块的重复。
     * 能在编译期间发现错误就在编译期间发现错误。
     */
    private final Set<Short> serviceIdSet = new HashSet<>(64);

    WildcardType wildcardType;

    private DeclaredType responseChannelDeclaredType;
    private DeclaredType sessionDeclaredType;

    TypeElement methodHandleElement;
    TypeName defaultMethodHandleRawTypeName;

    DeclaredType lazySerializableDeclaredType;
    DeclaredType preDeserializeDeclaredType;

    private TypeElement rpcServiceElement;
    private DeclaredType rpcServiceDeclaredType;
    private DeclaredType rpcMethodDeclaredType;

    ClassName registryTypeName;
    ClassName exceptionUtilsTypeName;

    private TypeMirror mapTypeMirror;
    private TypeMirror linkedHashMapTypeMirror;

    private TypeMirror collectionTypeMirror;
    private TypeMirror arrayListTypeMirror;

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(RPC_SERVICE_CANONICAL_NAME);
    }

    @Override
    protected void ensureInited() {
        if (rpcServiceElement != null) {
            // 已初始化
            return;
        }
        wildcardType = typeUtils.getWildcardType(null, null);

        rpcServiceElement = elementUtils.getTypeElement(RPC_SERVICE_CANONICAL_NAME);
        rpcServiceDeclaredType = typeUtils.getDeclaredType(rpcServiceElement);
        rpcMethodDeclaredType = typeUtils.getDeclaredType(elementUtils.getTypeElement(RPC_METHOD_CANONICAL_NAME));

        registryTypeName = ClassName.get(elementUtils.getTypeElement(REGISTRY_CANONICAL_NAME));
        sessionDeclaredType = typeUtils.getDeclaredType(elementUtils.getTypeElement(SESSION_CANONICAL_NAME));
        responseChannelDeclaredType = typeUtils.getDeclaredType(elementUtils.getTypeElement(CHANNEL_CANONICAL_NAME));

        exceptionUtilsTypeName = ClassName.get(elementUtils.getTypeElement(EXCEPTION_UTILS_CANONICAL_NAME));

        methodHandleElement = elementUtils.getTypeElement(METHOD_HANDLE_CANONICAL_NAME);
        defaultMethodHandleRawTypeName = TypeName.get(typeUtils.getDeclaredType(elementUtils.getTypeElement(DEFAULT_METHOD_HANDLE_CANONICAL_NAME)));

        lazySerializableDeclaredType = typeUtils.getDeclaredType(elementUtils.getTypeElement(LAZY_SERIALIZABLE_CANONICAL_NAME));
        preDeserializeDeclaredType = typeUtils.getDeclaredType(elementUtils.getTypeElement(PRE_DESERIALIZE_CANONICAL_NAME));

        mapTypeMirror = elementUtils.getTypeElement(Map.class.getCanonicalName()).asType();
        linkedHashMapTypeMirror = elementUtils.getTypeElement(LinkedHashMap.class.getCanonicalName()).asType();

        collectionTypeMirror = elementUtils.getTypeElement(Collection.class.getCanonicalName()).asType();
        arrayListTypeMirror = elementUtils.getTypeElement(ArrayList.class.getCanonicalName()).asType();
    }

    @Override
    protected boolean doProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // 该注解只有类才可以使用
        @SuppressWarnings("unchecked")
        Set<TypeElement> typeElementSet = (Set<TypeElement>) roundEnv.getElementsAnnotatedWith(rpcServiceElement);
        for (TypeElement typeElement : typeElementSet) {
            try {
                genProxyClass(typeElement);
            } catch (Throwable e) {
                messager.printMessage(Diagnostic.Kind.ERROR, e.toString(), typeElement);
            }
        }
        return true;
    }

    private void genProxyClass(TypeElement typeElement) {
        final Optional<? extends AnnotationMirror> serviceAnnotationOption = AutoUtils.findFirstAnnotationWithoutInheritance(typeUtils, typeElement, rpcServiceDeclaredType);
        assert serviceAnnotationOption.isPresent();
        // 基本类型会被包装，Object不能直接转short
        final Short serviceId = AutoUtils.getAnnotationValueValueNotDefault(serviceAnnotationOption.get(), SERVICE_ID_METHOD_NAME);
        assert null != serviceId;
        if (serviceId <= 0) {
            // serviceId非法
            messager.printMessage(Diagnostic.Kind.ERROR, " serviceId " + serviceId + " must greater than 0!", typeElement);
            return;
        }

        if (!serviceIdSet.add(serviceId)) {
            // serviceId重复
            messager.printMessage(Diagnostic.Kind.ERROR, " serviceId " + serviceId + " is duplicate!", typeElement);
            return;
        }

        // rpcMethods.size() == 0 也必须重新生成文件
        final List<ExecutableElement> rpcMethods = collectRpcMethods(typeElement);

        // 客户端代理
        genClientProxy(typeElement, serviceId, rpcMethods);

        // 服务器代理
        genServerProxy(typeElement, serviceId, rpcMethods);
    }

    /**
     * 搜集rpc方法
     *
     * @param typeElement rpcService类
     * @return 所有合法rpc方法
     */
    private List<ExecutableElement> collectRpcMethods(TypeElement typeElement) {
        final List<ExecutableElement> result = new ArrayList<>();
        final Set<Short> methodIdSet = new HashSet<>();

        for (final Element element : typeElement.getEnclosedElements()) {
            if (element.getKind() != ElementKind.METHOD) {
                continue;
            }

            final ExecutableElement method = (ExecutableElement) element;
            final Optional<? extends AnnotationMirror> rpcMethodAnnotation = AutoUtils.findFirstAnnotationWithoutInheritance(typeUtils, method, rpcMethodDeclaredType);
            if (rpcMethodAnnotation.isEmpty()) {
                // 不是rpc方法，跳过
                continue;
            }

            if (method.getModifiers().contains(Modifier.STATIC)) {
                // 不可以是静态的
                messager.printMessage(Diagnostic.Kind.ERROR, "RpcMethod method can't be static！", method);
                continue;
            }

            if (method.getModifiers().contains(Modifier.PRIVATE)) {
                // 访问权限不可以是private - 由于生成的类和该类属于同一个包，因此不必public，只要不是private即可
                messager.printMessage(Diagnostic.Kind.ERROR, "RpcMethod method can't be private！", method);
                continue;
            }

            // 方法id，基本类型会被封装为包装类型，Object并不能直接转换到基本类型
            final Short methodId = AutoUtils.getAnnotationValueValueNotDefault(rpcMethodAnnotation.get(), METHOD_ID_METHOD_NAME);
            assert null != methodId;
            if (methodId < 0 || methodId > 9999) {
                // 方法id非法
                messager.printMessage(Diagnostic.Kind.ERROR, " methodId " + methodId + " must between [0,9999]!", method);
                continue;
            }

            if (!methodIdSet.add(methodId)) {
                // 同一个类中的方法id不可以重复 - 它保证了本模块中方法id不会重复
                messager.printMessage(Diagnostic.Kind.ERROR, " methodId " + methodId + " is duplicate!", method);
                continue;
            }

            result.add(method);
        }

        return result;
    }

    Short getMethodId(ExecutableElement method) {
        // 方法id，基本类型会被封装为包装类型，Object并不能直接转换到基本类型
        return (Short) AutoUtils
                .findFirstAnnotationWithoutInheritance(typeUtils, method, rpcMethodDeclaredType)
                .map(annotationMirror -> AutoUtils.getAnnotationValueValueNotDefault(annotationMirror, METHOD_ID_METHOD_NAME))
                .get();
    }

    /**
     * 为客户端生成代理文件
     * XXXRpcProxy
     */
    private void genClientProxy(final TypeElement typeElement, final Short serviceId, final List<ExecutableElement> rpcMethods) {
        new RpcProxyGenerator(this, typeElement, serviceId, rpcMethods)
                .execute();
    }

    /**
     * 为服务器生成代理文件
     * XXXRpcRegister
     */
    private void genServerProxy(TypeElement typeElement, Short serviceId, List<ExecutableElement> rpcMethods) {
        new RpcRegisterGenerator(this, typeElement, serviceId, rpcMethods)
                .execute();
    }

    boolean isMap(VariableElement variableElement) {
        return AutoUtils.isSubTypeIgnoreTypeParameter(typeUtils, variableElement.asType(), mapTypeMirror);
    }

    boolean isAssignableFromLinkedHashMap(VariableElement variableElement) {
        return AutoUtils.isSubTypeIgnoreTypeParameter(typeUtils, linkedHashMapTypeMirror, variableElement.asType());
    }

    boolean isCollection(VariableElement variableElement) {
        return AutoUtils.isSubTypeIgnoreTypeParameter(typeUtils, variableElement.asType(), collectionTypeMirror);
    }

    boolean isAssignableFormArrayList(VariableElement variableElement) {
        return AutoUtils.isSubTypeIgnoreTypeParameter(typeUtils, arrayListTypeMirror, variableElement.asType());
    }

    boolean isResponseChannel(VariableElement variableElement) {
        return AutoUtils.isSameTypeIgnoreTypeParameter(typeUtils, variableElement.asType(), responseChannelDeclaredType);
    }

    boolean isSession(VariableElement variableElement) {
        return AutoUtils.isSameTypeIgnoreTypeParameter(typeUtils, variableElement.asType(), sessionDeclaredType);
    }


}
