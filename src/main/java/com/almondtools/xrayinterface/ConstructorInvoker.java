package com.almondtools.xrayinterface;

import static com.almondtools.xrayinterface.Converter.convertArguments;
import static com.almondtools.xrayinterface.Converter.convertResult;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Invokes a given constructor.
 */
public class ConstructorInvoker implements StaticMethodInvocationHandler {

	private Constructor<?> constructor;
	private Method target;

	/**
	 * Invokes the given constructor
	 * @param constructor the constructor to invoke
	 */
	public ConstructorInvoker(Constructor<?> constructor) {
		this.constructor = constructor;
		constructor.setAccessible(true);
	}

	/**
	 * Invokes the given constructor. Beyond {@link #ConstructorInvoker(Constructor)} this constructor also converts the constructor signature
	 * @param target the target signature (source arguments, target result)
	 * @see Convert 
	 */
	public ConstructorInvoker(Constructor<?> constructor, Method target) {
		this(constructor);
		this.target = target;
	}

	/**
	 * Invokes the default constructor (with no arguments)
	 * @param constructor the constructor to invoke
	 */
	public ConstructorInvoker(Class<?> clazz) throws NoSuchMethodException {
		this(defaultConstructor(clazz));
	}

	private static Constructor<?> defaultConstructor(Class<?> clazz) throws NoSuchMethodException {
		return clazz.getDeclaredConstructor(new Class[0]);
	}

	@Override
	public Object invoke(Object... args) throws Throwable {
		try {
			return r(constructor.newInstance(a(args)));
		} catch (InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

	private Object[] a(Object[] args) throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		if (target == null) {
			return args;
		}
		return convertArguments(target.getParameterTypes(), constructor.getParameterTypes(), args);
	}

	private Object r(Object result) throws NoSuchMethodException {
		if (target == null) {
			return result;
		}
		return convertResult(target.getReturnType(), constructor.getDeclaringClass(), result);
	}

}
