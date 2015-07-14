package com.almondtools.xrayinterface;

import static com.almondtools.xrayinterface.Converter.convertArguments;
import static com.almondtools.xrayinterface.Converter.convertResult;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Invokes a given method.
 */
public class MethodInvoker implements MethodInvocationHandler {

	private Method method;
	private Method target;

	/**
	 * Invokes the given method
	 * @param method the method to invoke
	 */
	public MethodInvoker(Method method) {
		this.method = method;
		method.setAccessible(true);
	}
	
	/**
	 * Invokes a given method. Beyond {@link #MethodInvoker(Method)} this constructor also converts the method signature
	 * @param method the method to invoke
	 * @param target the target signature (source arguments, target result)
	 * @see Convert
	 */
	public MethodInvoker(Method method, Method target) {
		this(method);
		this.target = target;
	}

	@Override
	public Object invoke(Object object, Object... args) throws Throwable {
		try {
			return r(method.invoke(object, a(args)));
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
