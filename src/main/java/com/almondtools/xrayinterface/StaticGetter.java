package com.almondtools.xrayinterface;

import static com.almondtools.xrayinterface.Converter.convertResult;

import java.lang.reflect.Field;

/**
 * Wraps a static field with read (getter) access.
 */
public class StaticGetter implements StaticMethodInvocationHandler {

	private Class<?> type;
	private Field field;
	private Class<?> target;

	/**
	 * Gets a value on the given field.
	 * @param type the static type of the field to access
	 * @param field the field to access
	 */
	public StaticGetter(Class<?> type, Field field) {
		this.type = type;
		this.field = field;
		field.setAccessible(true);
	}

	/**
	 * Gets a value on the given field. Beyond {@link #StaticGetter(Class, Field)} this constructor also converts the result
	 * @param type the static type of the field to access
	 * @param field the field to access
	 * @param target the target signature (target result)
	 * @see Convert 
	 */
	public StaticGetter(Class<?> type, Field field, Class<?> target) {
		this(type, field);
		this.target = target;
	}

	@Override
	public Object invoke(Object... args) throws Throwable {
		if (args != null && args.length != 0) {
			throw new IllegalArgumentException("getters can only be invoked with no argument, was " + args.length + " arguments");
		}
		return r(field.get(type));
	}

	private Object r(Object result) throws NoSuchMethodException {
		if (target == null) {
			return result;
		}
		return convertResult(target, field.getType(), result);
	}

}
