package com.almondtools.xrayinterface;

import static com.almondtools.xrayinterface.SignatureUtil.findTargetTypeName;
import static com.almondtools.xrayinterface.SignatureUtil.isBooleanGetter;
import static com.almondtools.xrayinterface.SignatureUtil.isGetter;
import static com.almondtools.xrayinterface.SignatureUtil.isSetter;
import static com.almondtools.xrayinterface.SignatureUtil.propertyOf;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Converter {

	private static final int GETTER = 0;
	private static final int SETTER = 1;

	private Map<String, Method[]> properties;

	public Converter() {
		properties = new LinkedHashMap<String, Method[]>();
	}

	public void addGetter(Method method) {
		Method[] methods = fetchMethods(method);
		methods[GETTER] = method;
	}

	public void addSetter(Method method) {
		Method[] methods = fetchMethods(method);
		methods[SETTER] = method;
	}

	private Method[] fetchMethods(Method method) {
		String name = propertyOf(method);
		Method[] methods = properties.get(name);
		if (methods == null) {
			methods = new Method[2];
			properties.put(name, methods);
		}
		return methods;
	}

	public List<Method[]> getReadWritablePropertyPairs() {
		List<Method[]> propertyPairs = new ArrayList<Method[]>();
		for (Method[] pair : properties.values()) {
			if (pair[0] != null && pair[1] != null) {
				propertyPairs.add(pair);
			}
		}
		return propertyPairs;
	}

	public static String[] determineNeededConversions(Annotation[][] parameterAnnotations, Class<?>[] parameterTypes) {
		String[] convert = new String[parameterAnnotations.length];
		for (int i = 0; i < parameterAnnotations.length; i++) {
			convert[i] = findTargetTypeName(parameterAnnotations[i], parameterTypes[i]);
		}
		return convert;
	}

	public static Object[] convertArguments(Class<?>[] targetTypes, Class<?>[] methodTypes, Object... args) throws InstantiationException, IllegalAccessException, NoSuchMethodException,
		IllegalArgumentException, InvocationTargetException, SecurityException {
		if (args == null) {
			args = new Object[0];
		}
		Object[] converted = new Object[args.length];
		Class<?>[] targetArgumentTypes = targetTypes;
		Class<?>[] methodArgumentTypes = methodTypes;
		for (int i = 0; i < args.length; i++) {
			converted[i] = convertArgument(targetArgumentTypes[i], methodArgumentTypes[i], args[i]);
		}
		return converted;
	}

	public static Object convertArgument(Class<?> targetType, Class<?> methodType, Object arg) throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		if (targetType.equals(methodType)) {
			return arg;
		} else {
			return convert(arg, methodType, targetType);
		}
	}

	public static Object convertResult(Class<?> targetType, Class<?> methodType, Object result) throws NoSuchMethodException {
		if (targetType.isAssignableFrom(methodType)) {
			return result;
		} else if (result == null) {
			return null;
		} else {
			return ObjectAccess.xray(result).to(targetType);
		}
	}

	public static boolean isConverted(Method method) {
		if (method.getAnnotation(Convert.class) != null) {
			return true;
		}
		Annotation[][] parameterAnnotations = method.getParameterAnnotations();
		Class<?>[] parameterTypes = method.getParameterTypes();
		for (int i = 0; i < parameterTypes.length; i++) {
			Class<?> parameterType = parameterTypes[i];
			Annotation[] annotations = parameterAnnotations[i];
			if (findTargetTypeName(annotations, parameterType) != null) {
				return true;
			}
		}
		return false;
	}

	public static Object convert(Object object, Class<?> clazz, Class<?> accessibleClass) throws InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException,
		InvocationTargetException, SecurityException {
		if (object instanceof Proxy) {
			InvocationHandler invocationHandler = Proxy.getInvocationHandler((Proxy) object);
			if (invocationHandler instanceof ObjectAccess) {
				return ((ObjectAccess) invocationHandler).getObject();
			}
		}
		Object converted = createBaseObject(clazz, accessibleClass);
		Object accessible = ObjectAccess.xray(converted).to(accessibleClass);
		for (Method[] getSetPair : findProperties(accessibleClass)) {
			Method get = getSetPair[0];
			get.setAccessible(true);
			Method set = getSetPair[1];
			set.setAccessible(true);
			Object value = get.invoke(object);
			set.invoke(accessible, value);
		}
		return converted;
	}

	private static Object createBaseObject(Class<?> clazz, Class<?> accessibleClass) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
		Constructor<?> constructor = clazz.getDeclaredConstructor();
		constructor.setAccessible(true);
		return constructor.newInstance();
	}

	private static List<Method[]> findProperties(Class<?> accessibleClass) {
		Converter converter = new Converter();
		for (Method method : accessibleClass.getDeclaredMethods()) {
			if (isSetter(method)) {
				converter.addSetter(method);
			} else if (isGetter(method) || isBooleanGetter(method)) {
				converter.addGetter(method);
			}
		}
		return converter.getReadWritablePropertyPairs();
	}

}