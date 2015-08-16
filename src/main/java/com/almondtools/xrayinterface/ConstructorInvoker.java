package com.almondtools.xrayinterface;

import static com.almondtools.xrayinterface.Converter.convertArguments;
import static com.almondtools.xrayinterface.Converter.convertResult;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Invokes a given constructor.
 */
public class ConstructorInvoker implements MethodInvocationHandler {

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
	public ConstructorInvoker(MethodHandle constructor, Class<?> targetReturnType, Class<?>[] targetParameterTypes) {
		this(constructor);
		this.targetReturnType = targetReturnType;
		this.targetParameterTypes = targetParameterTypes;
	}

	public Class<?> getResultType() {
		return constructor.type().returnType();
	}

	public Class<?>[] getTargetParameterTypes() {
		return targetParameterTypes;
	}
	
	public Class<?> getTargetReturnType() {
		return targetReturnType;
	}

	@Override
	public Object invoke(Object object, Object... args) throws Throwable {
		return r(constructor.invokeWithArguments(a(args)));
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
