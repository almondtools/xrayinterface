package com.almondtools.xrayinterface;

import static com.almondtools.xrayinterface.Converter.convertArguments;
import static com.almondtools.xrayinterface.Converter.convertResult;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Invokes a given method.
 */
public class MethodInvoker implements MethodInvocationHandler {

	private MethodHandle method;
	private Class<?>[] targetParameterTypes;
	private Class<?> targetReturnType;

	/**
	 * Invokes the given method
	 * @param method the method to invoke
	 */
	public MethodInvoker(MethodHandle method) {
		this.method = method;
	}

	/**
	 * Invokes a given method. Beyond {@link #MethodInvoker(Method)} this constructor also converts the method signature
	 * @param method the method to invoke
	 * @param target the target signature (source arguments, target result)
	 * @see Convert
	 */
	public MethodInvoker(MethodHandle method, Class<?> targetReturnType, Class<?>[] targetParameterTypes) {
		this(method);
		this.targetReturnType = targetReturnType;
		this.targetParameterTypes = targetParameterTypes;
	}

	@Override
	public Object invoke(Object object, Object... args) throws Throwable {
		try {
			MethodHandle unimorphMethod = method.bindTo(object);
			return r(unimorphMethod.invokeWithArguments(a(args)));
		} catch (InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

	private Object[] a(Object[] args) throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		if (targetParameterTypes == null || targetParameterTypes.length == 0) {
			return args;
		}
		return convertArguments(targetParameterTypes, method.type().dropParameterTypes(0, 1).parameterArray(), args);
	}

	private Object r(Object result) throws NoSuchMethodException {
		if (targetReturnType == null) {
			return result;
		}
		return convertResult(targetReturnType, method.type().returnType(), result);
	}

}
