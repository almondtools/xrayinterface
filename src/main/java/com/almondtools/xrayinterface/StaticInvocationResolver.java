package com.almondtools.xrayinterface;

import static com.almondtools.xrayinterface.Converter.determineNeededConversions;
import static com.almondtools.xrayinterface.Converter.isConverted;
import static com.almondtools.xrayinterface.SignatureUtil.fieldSignature;
import static com.almondtools.xrayinterface.SignatureUtil.findTargetTypeName;
import static com.almondtools.xrayinterface.SignatureUtil.isBooleanGetter;
import static com.almondtools.xrayinterface.SignatureUtil.isCompliant;
import static com.almondtools.xrayinterface.SignatureUtil.isConstructor;
import static com.almondtools.xrayinterface.SignatureUtil.isGetter;
import static com.almondtools.xrayinterface.SignatureUtil.isSetter;
import static com.almondtools.xrayinterface.SignatureUtil.matchesSignature;
import static com.almondtools.xrayinterface.SignatureUtil.methodSignature;
import static com.almondtools.xrayinterface.SignatureUtil.propertyAnnotationsOf;
import static com.almondtools.xrayinterface.SignatureUtil.propertyOf;
import static com.almondtools.xrayinterface.SignatureUtil.propertyTypeOf;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StaticInvocationResolver {

	private Map<String, Field> fieldCache;
	private Class<?> type;

	public StaticInvocationResolver(Class<?> type) {
		this.type = type;
		this.fieldCache = new HashMap<String, Field>();
	}
	
	public Class<?> getType() {
		return type;
	}

	protected StaticMethodInvocationHandler findInvocationHandler(Method method) throws NoSuchMethodException {
		try {
			return createMethodInvocator(method);
		} catch (NoSuchMethodException e) {
			try {
				if (isConstructor(method)) {
					return createConstructorInvocator(method);
				} else if (isSetter(method)) {
					return createSetterInvocator(method);
				} else if (isGetter(method) || isBooleanGetter(method)) {
					return createGetterInvocator(method);
				} else {
					throw e;
				}
			} catch (NoSuchFieldException e2) {
				throw e;
			}
		}
	}

	protected StaticMethodInvocationHandler createConstructorInvocator(Method method) throws NoSuchMethodException {
		return new ConstructorInvoker(findConstructor(method, type), findConversionTarget(method));
	}

	private Constructor<?> findConstructor(Method method, Class<?> clazz) throws NoSuchMethodException {
		if (isConverted(method)) {
			return findConvertibleConstructor(method, type);
		} else {
			return findMatchingConstructor(method, type);
		}
	}

	private Constructor<?> findConvertibleConstructor(Method method, Class<?> clazz) throws NoSuchMethodException {
		String[] convertArguments = determineNeededConversions(method.getParameterAnnotations(), method.getParameterTypes());
		String convertResult = findTargetTypeName(method.getAnnotations(), method.getReturnType());
		for (Constructor<?> candidate : clazz.getDeclaredConstructors()) {
			if (matchesSignature(method, candidate, convertArguments, convertResult)) {
				return candidate;
			}
		}
		throw new NoSuchMethodException(clazz.getSimpleName() + Arrays.asList(method.getParameterTypes()));
	}

	private Constructor<?> findMatchingConstructor(Method method, Class<?> clazz) throws NoSuchMethodException {
		for (Constructor<?> candidate : clazz.getDeclaredConstructors()) {
			if (matchesSignature(method, candidate, null, null)) {
				return candidate;
			}
		}
		throw new NoSuchMethodException(clazz.getSimpleName() + Arrays.asList(method.getParameterTypes()));
	}

	protected StaticMethodInvocationHandler createGetterInvocator(Method method) throws NoSuchFieldException {
		return new StaticGetter(type, findField(method), convertedPropertyTypeOf(method));
	}

	protected StaticMethodInvocationHandler createSetterInvocator(Method method) throws NoSuchFieldException {
		return new StaticSetter(type, findField(method), convertedPropertyTypeOf(method));
	}

	private Class<?> convertedPropertyTypeOf(Method method) {
		if (!isConverted(method)) {
			return null;
		}
		return propertyTypeOf(method);
	}

	private Field findField(Method method) throws NoSuchFieldException {
		if (isConverted(method)) {
			return findField(propertyOf(method), propertyTypeOf(method), propertyAnnotationsOf(method));
		} else {
			return findField(propertyOf(method), propertyTypeOf(method), new Annotation[0]);
		}
	}

	protected Field findField(String fieldPattern, Class<?> type, Annotation[] annotations) throws NoSuchFieldException {
		String convert = findTargetTypeName(annotations, type);
		List<String> fieldNames = SignatureUtil.computeFieldNames(fieldPattern);
		Class<?> currentClass = this.type;
		while (currentClass != Object.class) {
			for (String fieldName : fieldNames) {
				try {
					Field field = fieldCache.get(fieldName);
					if (field == null) {
						field = currentClass.getDeclaredField(fieldName);
						fieldCache.put(fieldName, field);
					}
					if (isCompliant(type, field.getType(), convert)) {
						return field;
					}
				} catch (NoSuchFieldException e) {
				}
			}
			currentClass = currentClass.getSuperclass();
		}
		throw new NoSuchFieldException(fieldSignature(fieldNames, type));
	}

	protected StaticMethodInvocationHandler createMethodInvocator(Method method) throws NoSuchMethodException {
		Class<?> currentClass = type;
		while (currentClass != Object.class) {
			try {
				Method candidate = findMethod(method, currentClass);
				return new StaticMethodInvoker(currentClass, candidate, findConversionTarget(method));
			} catch (NoSuchMethodException e) {
			}
			currentClass = currentClass.getSuperclass();
		}
		throw new NoSuchMethodException(methodSignature(method.getName(), method.getReturnType(), method.getParameterTypes(), method.getExceptionTypes()));
	}

	private Method findMethod(Method method, Class<?> clazz) throws NoSuchMethodException {
		if (isConverted(method)) {
			return findConvertibleMethod(method, clazz);
		} else {
			return findMatchingMethod(method, clazz);
		}
	}

	private Method findConversionTarget(Method method) {
		if (isConverted(method)) {
			return method;
		} else {
			return null;
		}
	}

	private Method findConvertibleMethod(Method method, Class<?> clazz) throws NoSuchMethodException {
		String[] convertArguments = determineNeededConversions(method.getParameterAnnotations(), method.getParameterTypes());
		String convertResult = findTargetTypeName(method.getAnnotations(), method.getReturnType());
		for (Method candidate : clazz.getDeclaredMethods()) {
			if (matchesSignature(method, candidate, convertArguments, convertResult)) {
				return candidate;
			}
		}
		throw new NoSuchMethodException();
	}

	private Method findMatchingMethod(Method method, Class<?> clazz) throws NoSuchMethodException {
		Method candidate = clazz.getDeclaredMethod(method.getName(), method.getParameterTypes());
		if (matchesSignature(method, candidate, null, null)) {
			return candidate;
		}
		throw new NoSuchMethodException();
	}

}
