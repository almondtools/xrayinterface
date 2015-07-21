package com.almondtools.xrayinterface;

import static com.almondtools.xrayinterface.Converter.convertArguments;
import static com.almondtools.xrayinterface.Converter.convertResult;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Invokes a given static method.
 */
public class StaticMethodInvoker implements MethodInvocationHandler {

	private MethodHandle method;
	private Class<?>[] targetParameterTypes;
	private Class<?> targetReturnType;

	/**
	 * Invokes the given method
	 * @param type the static type to invoke the method on
	 * @param method the method to invoke
	 */
	public StaticMethodInvoker(MethodHandle method) {
		this.method = method;
	}

	/**
	 * Invokes a given method. Beyond {@link #StaticMethodInvoker(Class, Method)} this constructor also converts the method signature
	 * @param type the static type to invoke the method on
	 * @param method the method to invoke
	 * @param target the target signature (source arguments, target result)
	 * @see Convert
	 */
	public StaticMethodInvoker(MethodHandle method, Class<?> targetReturnType, Class<?>[] targetParameterTypes) {
		this(method);
		this.targetReturnType = targetReturnType;
		this.targetParameterTypes = targetParameterTypes;
	}

	@Override
	public Object invoke(Object object, Object... args) throws Throwable {
		try {
			return r(method.invokeWithArguments(a(args)));
		} catch (InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

	private Object[] a(Object[] args) throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		if (targetParameterTypes == null || targetParameterTypes.length == 0) {
			return args;
		}
		return convertArguments(targetParameterTypes, method.type().parameterArray(), args);
	}

	private Object r(Object result) throws NoSuchMethodException {
		if (targetReturnType == null) {
			return result;
		}
		return convertResult(targetReturnType, method.type().returnType(), result);
	}

}
