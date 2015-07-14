package com.almondtools.xrayinterface;

import static com.almondtools.xrayinterface.Converter.convertResult;

import java.lang.reflect.Field;

/**
 * Wraps a field with read (getter) access.
 */
public class FieldGetter implements MethodInvocationHandler {

	private Field field;
	private Class<?> target;

	/**
	 * Gets a value on the given field.
	 * @param field the field to access
	 */
	public FieldGetter(Field field) {
		this.field = field;
		field.setAccessible(true);
	}

	/**
	 * Gets a value on the given field. Beyond {@link #FieldGetter(Field)} this constructor also converts the result
	 * @param field the field to access
	 * @param target the target signature (target result)
	 * @see Convert 
	 */
	public FieldGetter(Field field, Class<?> target) {
		this(field);
		this.target = target;
	}

	@Override
	public Object invoke(Object object, Object... args) throws Throwable {
		if (args != null && args.length != 0) {
			throw new IllegalArgumentException("getters can only be invoked with no argument, was " + args.length + " arguments");
		}
		return r(field.get(object));
	}

	private Object r(Object result) throws NoSuchMethodException {
		if (target == null) {
			return result;
		}
		return convertResult(target, field.getType(), result);
	}

}
