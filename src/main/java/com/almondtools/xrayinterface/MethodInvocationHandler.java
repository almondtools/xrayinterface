package com.almondtools.xrayinterface;


public interface MethodInvocationHandler {

	MethodInvocationHandler NULL = new MethodInvocationHandler() {
		
		@Override
		public Object invoke(Object object, Object... args) throws Throwable {
			return null;
		}
	};

	Object invoke(Object object, Object... args) throws Throwable;
}
