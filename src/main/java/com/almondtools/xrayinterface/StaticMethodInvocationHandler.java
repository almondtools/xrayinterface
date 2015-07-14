package com.almondtools.xrayinterface;


public interface StaticMethodInvocationHandler {

	StaticMethodInvocationHandler NULL = new StaticMethodInvocationHandler() {
		
		@Override
		public Object invoke(Object... args) throws Throwable {
			return null;
		}
	};

	Object invoke(Object... args) throws Throwable;
}
