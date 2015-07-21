package com.almondtools.xrayinterface;

import java.lang.reflect.Method;

public class InstanceInvocationResolver extends InvocationResolver {

	public InstanceInvocationResolver(Class<?> type) {
		super(type);
	}

	protected MethodInvocationHandler findInvocationHandler(Method method) throws NoSuchMethodException, NoSuchFieldException {
		BindingSignature signature = resolveSignature(method);
		switch (signature.type) {
		case SET:
			return createSetterInvocator(signature.name, signature.params[0]);
		case GET:
			return createGetterInvocator(signature.name, signature.result);
		case METHOD:
			return createMethodInvocator(signature.name, signature.result, signature.params, signature.exceptions);
		default:
			throw new NoSuchMethodException("invocation resolver failed resolving: " + method.toGenericString());
		}
	}

}
