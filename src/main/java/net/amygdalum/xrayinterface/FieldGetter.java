package net.amygdalum.xrayinterface;

import static net.amygdalum.xrayinterface.Converter.convertResult;

import java.lang.invoke.MethodHandle;

/**
 * Wraps a field with read (getter) access.
 */
public class FieldGetter implements MethodInvocationHandler {

	private String fieldName;
	private MethodHandle getter;
	private Class<?> target;

	/**
	 * Gets a value on the given field.
	 * 
 	 * @param fieldName the name of the field to set
	 * @param getter the getter method handle for the field to access
	 */
	public FieldGetter(String fieldName, MethodHandle getter) {
		this.fieldName = fieldName;
		this.getter = getter;
	}

	/**
	 * Gets a value on the given field. Beyond {@link #FieldGetter(String,MethodHandle)} this
	 * constructor also converts the result
	 * 
 	 * @param fieldName the name of the field to set
	 * @param getter the getter method handle for the field to access
	 * @param target the target signature (target result)
	 * @see Convert
	 */
	public FieldGetter(String fieldName, MethodHandle getter, Class<?> target) {
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
		return r(getter.invoke(object));
	}

	private Object r(Object result) throws NoSuchMethodException {
		if (target == null) {
			return result;
		}
		return convertResult(target, getter.type().returnType(), result);
	}

}
