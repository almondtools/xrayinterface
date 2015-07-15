package com.almondtools.xrayinterface;

import static com.almondtools.xrayinterface.Converter.convertArguments;
import static com.almondtools.xrayinterface.Converter.convertResult;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Invokes a given constructor.
 */
public class ConstructorInvoker implements StaticMethodInvocationHandler {

	private MethodHandle constructor;
	private Class<?>[] targetParameterTypes;
	private Class<?> targetReturnType;

	/**
	 * Invokes the given constructor
	 * @param constructor the constructor to invoke
	 */
	public ConstructorInvoker(MethodHandle constructor) {
		this.constructor = constructor;
	}

	/**
	 * Invokes the given constructor. Beyond {@link #ConstructorInvoker(Constructor)} this constructor also converts the constructor signature
	 * @param target the target signature (source arguments, target result)
	 * @see Convert 
	 */
	public ConstructorInvoker(MethodHandle constructor, Method target) {
		this(constructor);
		if (target != null) {
			this.targetReturnType = target.getReturnType();
			this.targetParameterTypes = target.getParameterTypes();
		}
	}

	@Override
	public Object invoke(Object... args) throws Throwable {
		try {
			return r(constructor.invokeWithArguments(a(args)));
		} catch (InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

	private Object[] a(Object[] args) throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		if (targetParameterTypes == null || targetParameterTypes.length == 0) {
			return args;
		}
		return convertArguments(targetParameterTypes, constructor.type().parameterArray(), args);
	}

	private Object r(Object result) throws NoSuchMethodException {
		if (targetReturnType == null) {
			return result;
		}
		return convertResult(targetReturnType, constructor.type().returnType(), result);
	}

}
