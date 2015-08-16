package com.almondtools.xrayinterface;

import static com.almondtools.xrayinterface.Converter.convertArgument;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

/**
 * Wraps a field with modification (setter) access.
 */
public class FieldSetter implements MethodInvocationHandler {

	private String fieldName;
	private MethodHandle setter;
	private Class<?> target;

	/**
	 * Sets a value on the given field.
	 * 
	 * @param fieldName the name of the field to set
	 * @param setter the setter method handle for the field to access
	 */
	public FieldSetter(String fieldName, MethodHandle setter) {
		this.fieldName = fieldName;
		this.setter = setter;
	}
	
	/**
	 * Sets a value on the given field. Beyond {@link #FieldSetter(Field)} this constructor also converts the argument
	 * 
	 * @param fieldName the name of the field to set
	 * @param setter the setter method handle for the field to access
	 * @param target the target signature (source arguments)
	 * @see Convert 
	 */
	public FieldSetter(String fieldName, MethodHandle setter, Class<?> target) {
		this(fieldName, setter);
		this.target = target;
	}
	
	public String getFieldName() {
		return fieldName;
	}

	public Class<?> getType() {
		return setter.type().parameterType(1);
	}

	public Class<?> getTarget() {
		return target;
	}
	
	@Override
	public Object invoke(Object object, Object... args) throws Throwable {
		if (args == null || args.length != 1) {
			throw new IllegalArgumentException("setters can only be invoked with exactly one argument, was " + (args == null ? "null" : String.valueOf(args.length)) + " arguments");
		}
		Object arg = a(args[0]);
		if (arg != null && !BoxingUtil.getBoxed(setter.type().parameterType(1)).isInstance(arg)) {
			throw new ClassCastException("defined type of field is " + arg.getClass().getSimpleName() + ", but assigned type was " + setter.type().parameterType(1).getSimpleName());
		}
		setter.invoke(object, arg);
		return null;
	}

	private Object a(Object arg) throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		if (target == null) {
			return arg;
		}
		return convertArgument(target, setter.type().parameterType(1), arg);
	}

}
