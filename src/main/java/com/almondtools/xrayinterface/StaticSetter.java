package com.almondtools.xrayinterface;

import static com.almondtools.xrayinterface.Converter.convertArgument;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

/**
 * Wraps a static field with modification (setter) access.
 * 
 * unfortunately some java compiler do inline literal constants. This setter may change the constant, but does not change inlined literals, resulting in strange effects.
 * better avoid setting static final variables or make sure, that they cannot be inlined (e.g. by making its value a trivial functional expression)
 */
public class StaticSetter implements StaticMethodInvocationHandler {

	private MethodHandle setter;
	private Class<?> target;

	/**
	 * Sets a value on the given field.
	 * 
	 * @param setter the setter method handle for the the field to access
	 */
	public StaticSetter(MethodHandle setter) {
		this.setter = setter;
	}

	/**
	 * Sets a value on the given field. Beyond {@link #StaticSetter(Class, Field)} this constructor also converts the argument
	 * 
	 * @param setter the setter method handle for the field to access
	 * @param target the target signature (source arguments)
	 * @see Convert
	 */
	public StaticSetter(MethodHandle setter, Class<?> target) {
		this(setter);
		this.target = target;
	}

	@Override
	public Object invoke(Object... args) throws Throwable {
		if (args == null || args.length != 1) {
			throw new IllegalArgumentException("setters can only be invoked with exactly one argument, was " + (args == null ? "null" : String.valueOf(args.length)) + " arguments");
		}
		Object arg = a(args[0]);
		if (arg != null && !BoxingUtil.getBoxed(setter.type().parameterType(0)).isInstance(arg)) {
			throw new ClassCastException("defined type of field is " + arg.getClass().getSimpleName() + ", but assigned type was " + setter.type().parameterType(0).getSimpleName());
		}
		setter.invoke(arg);
		return null;
	}

	private Object a(Object arg) throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		if (target == null) {
			return arg;
		}
		return convertArgument(target, setter.type().parameterType(0), arg);
	}

	public MethodInvocationHandler asMethodInvocationHandler() {
		return (object, args) -> invoke(args);
	}

}
