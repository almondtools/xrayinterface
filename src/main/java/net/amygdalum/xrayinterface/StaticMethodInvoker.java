package net.amygdalum.xrayinterface;

import static net.amygdalum.xrayinterface.Converter.convertArguments;
import static net.amygdalum.xrayinterface.Converter.convertResult;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.InvocationTargetException;

/**
 * Invokes a given static method.
 */
public class StaticMethodInvoker implements MethodInvocationHandler {

	private String name;
	private MethodHandle method;
	private Class<?>[] targetParameterTypes;
	private Class<?> targetReturnType;

	public StaticMethodInvoker(String name, MethodHandle method) {
		this.name = name;
		this.method = method;
	}

	/**
	 * Invokes a given method. Beyond {@link #StaticMethodInvoker(String,MethodHandle)} this constructor also converts the method signature
	 * @param name the name of the method
	 * @param method the method to invoke
	 * @param targetReturnType the return type the result of the actual invocation should be converted to
	 * @param targetParameterTypes the types the parameters should be converted from
	 * @see Convert
	 */
	public StaticMethodInvoker(String name, MethodHandle method, Class<?> targetReturnType, Class<?>[] targetParameterTypes) {
		this(name, method);
		this.targetReturnType = targetReturnType;
		this.targetParameterTypes = targetParameterTypes;
	}
	
	public String getName() {
		return name;
	}
	
	public Class<?>[] getTargetParameterTypes() {
		return targetParameterTypes;
	}
	
	public Class<?> getTargetReturnType() {
		return targetReturnType;
	}

	@Override
	public Object invoke(Object object, Object... args) throws Throwable {
		return r(method.invokeWithArguments(a(args)));
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
