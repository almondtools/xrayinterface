package com.almondtools.xrayinterface;

import static com.almondtools.xrayinterface.Converter.determineNeededConversions;
import static com.almondtools.xrayinterface.Converter.isConverted;
import static com.almondtools.xrayinterface.FinalUtil.ensureNonFinal;
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
import static java.lang.reflect.Modifier.isStatic;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StaticInvocationResolver {

	private MethodHandles.Lookup lookup;
	private Map<String, Field> fieldCache;
	private Class<?> type;

	public StaticInvocationResolver(Class<?> type) {
		this.type = type;
		this.lookup = MethodHandles.lookup();
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
		try {
			Constructor<?> constructor = findConstructor(method, type);
			return new ConstructorInvoker(lookup.unreflectConstructor(constructor), findConversionTarget(method));
		} catch (IllegalAccessException e) {
			throw new NoSuchMethodException("constructor " + method.getDeclaringClass().getSimpleName() + " is not accesible.");
		}
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
				candidate.setAccessible(true);
				return candidate;
			}
		}
		throw new NoSuchMethodException(clazz.getSimpleName() + Arrays.asList(method.getParameterTypes()));
	}

	private Constructor<?> findMatchingConstructor(Method method, Class<?> clazz) throws NoSuchMethodException {
		for (Constructor<?> candidate : clazz.getDeclaredConstructors()) {
			if (matchesSignature(method, candidate, null, null)) {
				candidate.setAccessible(true);
				return candidate;
			}
		}
		throw new NoSuchMethodException(clazz.getSimpleName() + Arrays.asList(method.getParameterTypes()));
	}

	protected StaticMethodInvocationHandler createGetterInvocator(Method method) throws NoSuchFieldException {
		Field field = findField(method);
		return createGetterInvocator(field, convertedPropertyTypeOf(method), isStatic(field.getModifiers()));
	}

	private StaticMethodInvocationHandler createGetterInvocator(Field field, Class<?> convertedPropertyType, boolean isStatic) throws NoSuchFieldException {
		try {
			MethodHandle getter = lookup.unreflectGetter(field);
			if (isStatic ) {
				return new StaticGetter(getter, convertedPropertyType);
			} else {
				throw new NoSuchFieldException(field.getName() + " is not static.");
			}
		} catch (IllegalAccessException e) {
			throw new NoSuchFieldException("field " + field.getName() + " is not accessible. Check your security manager.");
		}
	}

	protected StaticMethodInvocationHandler createSetterInvocator(Method method) throws NoSuchFieldException {
		Field field = findField(method);
		ensureNonFinal(field);
		
		return createSetterInvocator(field, convertedPropertyTypeOf(method), isStatic(field.getModifiers()));
	}

	private StaticMethodInvocationHandler createSetterInvocator(Field field, Class<?> convertedPropertyType, boolean isStatic) throws NoSuchFieldException {
		try {
			MethodHandle setter = lookup.unreflectSetter(field);
			if (isStatic ) {
				return new StaticSetter(setter, convertedPropertyType);
			} else {
				throw new NoSuchFieldException(field.getName() + " is not static.");
			}
		} catch (IllegalAccessException e) {
			throw new NoSuchFieldException(field.getName() + " is not accessible. Check your security manager.");
		}
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
						field.setAccessible(true);
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
				return new StaticMethodInvoker(lookup.unreflect(candidate), findConversionTarget(method));
			} catch (NoSuchMethodException | IllegalAccessException e) {
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
				candidate.setAccessible(true);
				return candidate;
			}
		}
		throw new NoSuchMethodException();
	}

	private Method findMatchingMethod(Method method, Class<?> clazz) throws NoSuchMethodException {
		Method candidate = clazz.getDeclaredMethod(method.getName(), method.getParameterTypes());
		if (matchesSignature(method, candidate, null, null)) {
			candidate.setAccessible(true);
			return candidate;
		}
		throw new NoSuchMethodException();
	}

}
