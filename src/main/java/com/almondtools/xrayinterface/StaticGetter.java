package com.almondtools.xrayinterface;

import static com.almondtools.xrayinterface.Converter.convertResult;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;

/**
 * Wraps a static field with read (getter) access.
 */
public class StaticGetter implements MethodInvocationHandler {

	private String fieldName;
	private MethodHandle getter;
	private Class<?> target;

	/**
	 * Gets a value on the given field.
	 * 
	 * @param fieldName the name of the field to set
	 * @param getter method handle for the field to access
	 */
	public StaticGetter(String fieldName, MethodHandle getter) {
		this.fieldName = fieldName;
		this.getter = getter;
	}

	/**
	 * Gets a value on the given field. Beyond {@link #StaticGetter(Class, Field)} this constructor also converts the result
	 * 
	 * @param fieldName the name of the field to set
	 * @param getter method handle for the field to access
	 * @param target the target signature (target result)
	 * @see Convert 
	 */
	public StaticGetter(String fieldName, MethodHandle getter, Class<?> target) {
		this(fieldName, getter);
		this.target = target;
	}
	
	public String getFieldName() {
		return fieldName;
	}

	public Class<?> getResultType() {
		return getter.type().returnType();
	}

	public Class<?> getTarget() {
		return target;
	}
	
	@Override
	public Object invoke(Object object, Object... args) throws Throwable {
		if (args != null && args.length != 0) {
			throw new IllegalArgumentException("getters can only be invoked with no argument, was " + args.length + " arguments");
		}
		return r(getter.invoke());
	}

	private Object r(Object result) throws NoSuchMethodException {
		if (target == null) {
			return result;
		}
		return convertResult(target, getter.type().returnType(), result);
	}

	public MethodInvocationHandler asMethodInvocationHandler() {
		return (object, args) -> invoke(args);
	}

}
