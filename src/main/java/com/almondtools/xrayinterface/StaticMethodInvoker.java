package com.almondtools.xrayinterface;

import static com.almondtools.xrayinterface.Converter.convertArguments;
import static com.almondtools.xrayinterface.Converter.convertResult;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Invokes a given static method.
 */
public class StaticMethodInvoker implements StaticMethodInvocationHandler {

	private Class<?> type;
	private Method method;
	private Method target;

	/**
	 * Invokes the given method
	 * @param type the static type to invoke the method on
	 * @param method the method to invoke
	 */
	public StaticMethodInvoker(Class<?> type, Method method) {
		this.type = type;
		this.method = method;
		method.setAccessible(true);
	}

	/**
	 * Invokes a given method. Beyond {@link #StaticMethodInvoker(Class, Method)} this constructor also converts the method signature
	 * @param type the static type to invoke the method on
	 * @param method the method to invoke
	 * @param target the target signature (source arguments, target result)
	 * @see Convert
	 */
	public StaticMethodInvoker(Class<?> type, Method method, Method target) {
		this(type, method);
		this.target = target;
	}

	@Override
	public Object invoke(Object... args) throws Throwable {
		try {
			return r(method.invoke(type, a(args)));
		} catch (InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

	private Object[] a(Object[] args) throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		if (target == null) {
			return args;
		}
		return convertArguments(target.getParameterTypes(), method.getParameterTypes(), args);
	}

	private Object r(Object result) throws NoSuchMethodException {
		if (target == null) {
			return result;
		}
		return convertResult(target.getReturnType(), method.getReturnType(), result);
	}

}
