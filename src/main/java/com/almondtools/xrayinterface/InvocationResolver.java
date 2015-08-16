package com.almondtools.xrayinterface;

import static com.almondtools.xrayinterface.BindingQualifier.AUTO;
import static com.almondtools.xrayinterface.BindingQualifier.CONSTRUCTOR;
import static com.almondtools.xrayinterface.BindingQualifier.GET;
import static com.almondtools.xrayinterface.BindingQualifier.METHOD;
import static com.almondtools.xrayinterface.BindingQualifier.SET;
import static com.almondtools.xrayinterface.FinalUtil.ensureNonFinal;
import static com.almondtools.xrayinterface.FixedType.fixed;
import static com.almondtools.xrayinterface.SignatureUtil.computeFieldNames;
import static com.almondtools.xrayinterface.SignatureUtil.fieldSignature;
import static com.almondtools.xrayinterface.SignatureUtil.isBooleanGetter;
import static com.almondtools.xrayinterface.SignatureUtil.isGetter;
import static com.almondtools.xrayinterface.SignatureUtil.isSetter;
import static com.almondtools.xrayinterface.SignatureUtil.methodSignature;
import static com.almondtools.xrayinterface.SignatureUtil.propertyOf;
import static com.almondtools.xrayinterface.Type.convertedTypes;
import static com.almondtools.xrayinterface.Type.isWeakMatching;
import static com.almondtools.xrayinterface.Type.matchedTypes;
import static com.almondtools.xrayinterface.Type.matches;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.Arrays.asList;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

public class InvocationResolver {

	private static Map<Class<? extends Annotation>, Function<Annotation, BindingSignature>> signatures = createSignatureMapping();

	private MethodHandles.Lookup lookup;
	private Map<String, Field> fieldCache;
	private Class<?> type;

	public InvocationResolver(Class<?> type) {
		this.type = type;
		this.lookup = MethodHandles.lookup();
		this.fieldCache = new HashMap<String, Field>();
	}

	public Class<?> getType() {
		return type;
	}

	private static Map<Class<? extends Annotation>, Function<Annotation, BindingSignature>> createSignatureMapping() {
		Map<Class<? extends Annotation>, Function<Annotation, BindingSignature>> signatures = new IdentityHashMap<>();
		signatures.put(Construct.class, annotation -> new BindingSignature("", CONSTRUCTOR));
		signatures.put(Delegate.class, annotation -> new BindingSignature(((Delegate) annotation).value(), METHOD));
		signatures.put(SetProperty.class, annotation -> new BindingSignature(((SetProperty) annotation).value(), SET));
		signatures.put(GetProperty.class, annotation -> new BindingSignature(((GetProperty) annotation).value(), GET));
		return signatures;
	}

	protected MethodInvocationHandler findInvocationHandler(Method method) throws NoSuchMethodException, NoSuchFieldException {
		BindingSignature signature = resolveSignature(method);
		switch (signature.qualifier) {
		case SET:
			return createSetterInvocator(signature.name, signature.params[0]);
		case GET:
			return createGetterInvocator(signature.name, signature.result);
		case METHOD:
			return createMethodInvocator(signature.name, signature.result, signature.params, signature.exceptions);
		case CONSTRUCTOR:
			return createConstructorInvocator(signature.result, signature.params, signature.exceptions);
		default:
			throw new NoSuchMethodException("invocation resolver failed resolving: " + method.toGenericString());
		}
	}

	protected BindingSignature resolveSignature(Method method) throws NoSuchMethodException, NoSuchFieldException {
		BindingSignature signature = initSignature(method.getAnnotations());
		signature = completeSignature(signature, method);
		return signature;
	}

	private BindingSignature initSignature(Annotation[] annotations) {
		if (annotations == null) {
			return new BindingSignature();
		} else {
			return Stream.of(annotations)
				.filter(annotation -> signatures.containsKey(annotation.annotationType()))
				.map(annotation -> signatures.get(annotation.annotationType()).apply(annotation))
				.findFirst()
				.orElse(new BindingSignature());
		}
	}

	private BindingSignature completeSignature(BindingSignature signature, Method method) throws NoSuchMethodException, NoSuchFieldException {
		Exception exception = null;
		for (BindingQualifier type : signature.types()) {
			try {
				return completeSignature(type, signature, method);
			} catch (NoSuchFieldException | NoSuchMethodException e) {
				if (exception == null) {
					exception = e;
				}
			}
		}
		if (exception instanceof NoSuchFieldException){
			throw (NoSuchFieldException) exception;
		} else if (exception instanceof NoSuchMethodException){
			throw (NoSuchMethodException) exception;
		} else {
			return new BindingSignature(method.getName());
		}
	}

	private BindingSignature completeSignature(BindingQualifier type, BindingSignature signature, Method method) throws NoSuchMethodException, NoSuchFieldException {
		switch (type) {
		case SET:
			return completeSetter(signature, method);
		case GET:
			return completeGetter(signature, method);
		case METHOD:
			return completeMethod(signature, method);
		case CONSTRUCTOR:
			return completeConstructor(signature, method);
		default:
			throw new NoSuchMethodException();
		}
	}

	private BindingSignature completeSetter(BindingSignature signature, Method method) throws NoSuchFieldException {
		if (!signature.hasName() && !isSetter(method)) {
			throw new NoSuchFieldException();
		}
		Type type = setterType(method);
		List<String> names = signature.hasName() ? asList(signature.name) : computeFieldNames(propertyOf(method));
		for (String name : names) {
			try {
				Field field = findField(name, type);
				signature.params = new Type[] { type.matching(field.getType()) };
				signature.name = name;
				signature.qualifier = SET;
				return signature;
			} catch (NoSuchFieldException e) {
			}
		}
		throw new NoSuchFieldException(fieldSignature(names, type.matchedType()));
	}

	private Type setterType(Method method) {
		Parameter[] parameters = method.getParameters();
		if (parameters.length == 1) {
			Parameter value = parameters[0];
			Class<?> valueClass = value.getType();
			Convert converted = value.getAnnotation(Convert.class);
			if (converted != null) {
				return new MatchType(name(converted, valueClass), valueClass);
			} else {
				return fixed(valueClass);
			}
		} else {
			return null;
		}
	}

	private BindingSignature completeGetter(BindingSignature signature, Method method) throws NoSuchFieldException {
		if (!signature.hasName() && !(isGetter(method) || isBooleanGetter(method))) {
			throw new NoSuchFieldException();
		}
		Type type = getterType(method);
		List<String> names = signature.hasName() ? asList(signature.name) : computeFieldNames(propertyOf(method));
		for (String name : names) {
			try {
				Field field = findField(name, type);
				signature.result = type.matching(field.getType());
				signature.name = name;
				signature.qualifier = GET;
				return signature;
			} catch (NoSuchFieldException e) {
			}
		}
		throw new NoSuchFieldException(fieldSignature(names, type.matchedType()));
	}

	private Type getterType(Method method) {
		Class<?> valueClass = method.getReturnType();
		Convert converted = method.getAnnotation(Convert.class);
		if (converted != null) {
			return new MatchType(name(converted, valueClass), valueClass);
		} else {
			return fixed(valueClass);
		}
	}

	private String name(Convert converted, Class<?> valueClass) {
		String name = converted.value();
		if ("".equals(name)) {
			name = valueClass.getSimpleName();
		}
		return name;
	}

	private BindingSignature completeMethod(BindingSignature signature, Method method) throws NoSuchMethodException {
		String name = signature.hasName() ? signature.name : method.getName();
		Type result = resultType(method);
		Type[] params = paramTypes(method);
		Type[] exceptions = exceptionTypes(method);
		Method targetMethod = findMethod(name, result, params, exceptions);
		signature.result = result.matching(targetMethod.getReturnType());
		signature.params = matching(params, targetMethod.getParameterTypes());
		signature.exceptions = matching(exceptions, targetMethod.getExceptionTypes());
		signature.name = name;
		signature.qualifier = METHOD;
		return signature;
	}

	private Type resultType(Method method) {
		Class<?> valueClass = method.getReturnType();
		Convert converted = method.getAnnotation(Convert.class);
		if (converted != null) {
			return new MatchType(name(converted, valueClass), valueClass);
		} else {
			return fixed(valueClass);
		}
	}

	private Type[] paramTypes(Method method) {
		Parameter[] parameters = method.getParameters();
		Type[] types = new Type[parameters.length];
		for (int i = 0; i < parameters.length; i++) {
			Parameter value = parameters[i];
			Class<?> valueClass = value.getType();
			Convert converted = value.getAnnotation(Convert.class);
			if (converted != null) {
				types[i] = new MatchType(name(converted, valueClass), valueClass);
			} else {
				types[i] = fixed(valueClass);
			}
		}
		return types;
	}

	private Type[] exceptionTypes(Method method) {
		Class<?>[] exceptions = method.getExceptionTypes();
		Type[] types = new Type[exceptions.length];
		for (int i = 0; i < exceptions.length; i++) {
			Class<?> valueClass = exceptions[i];
			types[i] = fixed(valueClass);
		}
		return types;
	}

	private BindingSignature completeConstructor(BindingSignature signature, Method method) throws NoSuchMethodException {
		if (signature.qualifier == AUTO && !SignatureUtil.isConstructor(method)) {
			throw new NoSuchMethodException();
		}
		Type result = resultType(method);
		Type[] params = paramTypes(method);
		Type[] exceptions = exceptionTypes(method);
		Constructor<?> targetMethod = findConstructor(result, params, exceptions);
		signature.result = result.matching(targetMethod.getDeclaringClass());
		signature.params = matching(params, targetMethod.getParameterTypes());
		signature.exceptions = matching(exceptions, targetMethod.getExceptionTypes());
		signature.name = "";
		signature.qualifier = CONSTRUCTOR;
		return signature;
	}

	private Type[] matching(Type[] types, Class<?>[] matchingTypes) {
		for (int i = 0; i < types.length; i++) {
			types[i] = types[i].matching(matchingTypes[i]);
		}
		return types;
	}

	protected MethodInvocationHandler createSetterInvocator(String name, Type param) throws NoSuchFieldException {
		Field field = findField(name, param);
		ensureNonFinal(field);
		return createSetterInvocator(field, param.convertedType());
	}

	private MethodInvocationHandler createSetterInvocator(Field field, Class<?> convertedPropertyType) throws NoSuchFieldException {
		try {
			MethodHandle getter = lookup.unreflectSetter(field);
			if (isStatic(field.getModifiers())) {
				return new StaticSetter(field.getName(), getter, convertedPropertyType);
			} else {
				return new FieldSetter(field.getName(), getter, convertedPropertyType);
			}
		} catch (IllegalAccessException e) {
			throw new NoSuchFieldException(field.getName() + " is not accessible. Check your security manager.");
		}
	}

	protected MethodInvocationHandler createGetterInvocator(String name, Type result) throws NoSuchFieldException {
		Field field = findField(name, result);
		return createGetterInvocator(field, result.convertedType());
	}

	private MethodInvocationHandler createGetterInvocator(Field field, Class<?> convertedPropertyType) throws NoSuchFieldException {
		try {
			MethodHandle getter = lookup.unreflectGetter(field);
			if (isStatic(field.getModifiers())) {
				return new StaticGetter(field.getName(), getter, convertedPropertyType);
			} else {
				return new FieldGetter(field.getName(), getter, convertedPropertyType);
			}
		} catch (IllegalAccessException e) {
			throw new NoSuchFieldException(field.getName() + " is not accessible. Check your security manager.");
		}
	}

	protected Field findField(String name, Type type) throws NoSuchFieldException {
		Class<?> currentClass = this.type;
		while (currentClass != Object.class) {
			try {
				Field field = fieldCache.get(name);
				if (field == null) {
					field = currentClass.getDeclaredField(name);
					field.setAccessible(true);
					fieldCache.put(name, field);
				}
				if (type.matches(field.getType())) {
					return field;
				}
			} catch (NoSuchFieldException e) {
			}
			currentClass = currentClass.getSuperclass();
		}
		Class<?> fieldType = type.matchedType();
		throw new NoSuchFieldException(fieldSignature(name, fieldType));
	}

	protected MethodInvocationHandler createMethodInvocator(String name, Type result, Type[] params, Type[] exceptions) throws NoSuchMethodException {
		Method method = findMethod(name, result, params, exceptions);
		return createMethodInvocator(method, result, params);
	}

	private MethodInvocationHandler createMethodInvocator(Method method, Type result, Type[] params) throws NoSuchMethodException {
		try {
			MethodHandle methodHandle = lookup.unreflect(method);
			if (isStatic(method.getModifiers())) {
				return new StaticMethodInvoker(method.getName(), methodHandle, result.convertedType(), convertedTypes(params));
			} else {
				return new MethodInvoker(method.getName(), methodHandle, result.convertedType(), convertedTypes(params));
			}
		} catch (IllegalAccessException e) {
			throw new NoSuchMethodException(method.getName() + " is not accessible. Check your security manager.");
		}
	}

	protected Method findMethod(String name, Type result, Type[] params, Type[] exceptions) throws NoSuchMethodException {
		boolean weakMatching = isWeakMatching(params);
		Class<?>[] paramTypes = matchedTypes(params);
		Class<?>[] exceptionTypes = matchedTypes(exceptions);
		Class<?> currentClass = this.type;
		while (currentClass != Object.class) {
			try {
				Method candidate = weakMatching ? matchMethod(currentClass, name, params) : matchStrong(currentClass, name, paramTypes);
				if (result.matches(candidate.getReturnType())
					&& Arrays.equals(exceptionTypes, candidate.getExceptionTypes())) {
					candidate.setAccessible(true);
					return candidate;
				}
			} catch (NoSuchMethodException e) {
			}
			currentClass = currentClass.getSuperclass();
		}
		throw new NoSuchMethodException(methodSignature(name, result.matchedType(), paramTypes, exceptionTypes));
	}

	private Method matchMethod(Class<?> currentClass, String name, Type[] params) throws NoSuchMethodException {
		return Stream.of(currentClass.getDeclaredMethods())
			.filter(method -> name.equals(method.getName()))
			.filter(method -> matches(params, method.getParameterTypes()))
			.findFirst()
			.orElseThrow(() -> new NoSuchMethodException());
	}

	private Method matchStrong(Class<?> currentClass, String name, Class<?>[] paramTypes) throws NoSuchMethodException {
		return currentClass.getDeclaredMethod(name, paramTypes);
	}

	protected MethodInvocationHandler createConstructorInvocator(Type result, Type[] params, Type[] exceptions) throws NoSuchMethodException {
		Constructor<?> constructor = findConstructor(result, params, exceptions);
		return createConstructorInvocator(constructor, result, params);
	}

	private MethodInvocationHandler createConstructorInvocator(Constructor<?> constructor, Type result, Type[] params) throws NoSuchMethodException {
		try {
			return new ConstructorInvoker(lookup.unreflectConstructor(constructor), result.convertedType(), convertedTypes(params));
		} catch (IllegalAccessException e) {
			throw new NoSuchMethodException(constructor.getName() + " is not accessible. Check your security manager.");
		}
	}

	protected Constructor<?> findConstructor(Type result, Type[] params, Type[] exceptions) throws NoSuchMethodException {
		Class<?>[] paramTypes = matchedTypes(params);
		Class<?>[] exceptionTypes = matchedTypes(exceptions);
		Class<?> currentClass = this.type;
		while (currentClass != Object.class) {
			try {
				Constructor<?> candidate = currentClass.getDeclaredConstructor(paramTypes);
				if (result.matches(type)
					&& Arrays.equals(exceptionTypes, candidate.getExceptionTypes())) {
					candidate.setAccessible(true);
					return candidate;
				}
			} catch (NoSuchMethodException e) {
			}
			currentClass = currentClass.getSuperclass();
		}
		throw new NoSuchMethodException(SignatureUtil.methodSignature("<init>", type, paramTypes, exceptionTypes));
	}

}
