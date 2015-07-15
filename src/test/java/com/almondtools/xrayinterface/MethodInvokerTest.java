package com.almondtools.xrayinterface;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;

import com.almondtools.xrayinterface.MethodInvoker;

@SuppressWarnings("unused")
public class MethodInvokerTest {

	private Lookup lookup;

	@Before
	public void before() throws Exception {
		this.lookup = MethodHandles.lookup();
	}

	private MethodHandle methodOf(Class<?> clazz, String method, Class<?>... parameters) throws IllegalAccessException, NoSuchMethodException {
		Method declaredMethod = clazz.getDeclaredMethod(method, parameters);
		return methodOf(declaredMethod);
	}

	private MethodHandle methodOf(Method method) throws IllegalAccessException {
		method.setAccessible(true);
		return lookup.unreflect(method);
	}

	@Test
	public void testInvoke() throws Throwable {
		WithMethod object = new WithMethod();
		Object invoke = new MethodInvoker(methodOf(WithMethod.class,"staticMethod", int.class)).invoke(object, new Object[] { 1 });
		assertThat((String) invoke, equalTo("1"));
	}

	@Test(expected = ClassCastException.class)
	public void testInvokeFailingSignature() throws Throwable {
		WithMethod object = new WithMethod();
		new MethodInvoker(methodOf(WithMethod.class,"staticMethod", int.class)).invoke(object, new Object[] { "1" });
	}

	@Test(expected = IOException.class)
	public void testInvokeCheckedException() throws Throwable {
		WithMethod object = new WithMethod();
		new MethodInvoker(methodOf(WithMethod.class,"staticException", int.class)).invoke(object, new Object[] { 2 });
	}

	@Test(expected = NullPointerException.class)
	public void testInvokeUncheckedException() throws Throwable {
		WithMethod object = new WithMethod();
		new MethodInvoker(methodOf(WithMethod.class,"staticException", int.class)).invoke(object, new Object[] { 1 });
	}

	@Test
	public void testConvertedInvoke() throws Throwable {
		WithMethod object = new WithMethod();
		Object invoke = new MethodInvoker(methodOf(staticMethod()), staticMethod()).invoke(object, new Object[] { 1 });
		assertThat((String) invoke, equalTo("1"));
	}

	@Test(expected = ClassCastException.class)
	public void testConvertedInvokeFailingSignature() throws Throwable {
		WithMethod object = new WithMethod();
		new MethodInvoker(methodOf(staticMethod()), staticMethod()).invoke(object, new Object[] { "1" });
	}

	@Test(expected = IOException.class)
	public void testConvertedInvokeCheckedException() throws Throwable {
		WithMethod object = new WithMethod();
		new MethodInvoker(methodOf(staticException()), staticException()).invoke(object, new Object[] { 2 });
	}

	@Test(expected = NullPointerException.class)
	public void testConvertedInvokeUncheckedException() throws Throwable {
		WithMethod object = new WithMethod();
		new MethodInvoker(methodOf(staticException()), staticException()).invoke(object, new Object[] { 1 });
	}

	@Test
	public void testInvokeWithDifferentMethods() throws Throwable {
		ForSimpleObject object = new ForSimpleObject();
		Object invoke = new MethodInvoker(methodOf(staticLongMethod()), interfaceLongMethod()).invoke(object, new Object[] { 1l, "2" });
		assertThat((Integer) invoke, equalTo(3));
	}

	private Method staticMethod() throws NoSuchMethodException {
		return WithMethod.class.getDeclaredMethod("staticMethod", int.class);
	}

	private Method staticException() throws NoSuchMethodException {
		return WithMethod.class.getDeclaredMethod("staticException", int.class);
	}

	private Method staticLongMethod() throws NoSuchMethodException {
		return ForSimpleObject.class.getDeclaredMethod("longMethod", long.class, String.class);
	}

	private Method interfaceLongMethod() throws NoSuchMethodException {
		return InterfaceForSimpleObject.class.getDeclaredMethod("longMethod", long.class, String.class);
	}

	private Method staticSimpleObjectMethod() throws NoSuchMethodException {
		return ForSimpleObject.class.getDeclaredMethod("simpleObjectMethod", SimpleObject.class);
	}

	private Method interfaceSimpleObjectMethod() throws NoSuchMethodException {
		return InterfaceForSimpleObject.class.getDeclaredMethod("simpleObjectMethod", SimpleObjectInterface.class);
	}

	private SimpleObjectInterface simpleObjectInterface(final String s) {
		return new SimpleObjectInterface() {

			@Override
			public String getString() {
				return s;
			}

			@Override
			public void setString(String s) {
			}
			
		};
	}

	private static class WithMethod {
		private String staticMethod(int i) {
			return String.valueOf(i);
		}

		private String staticException(int i) throws IOException {
			if (i == 1) {
				throw new NullPointerException();
			} else {
				throw new IOException();
			}
		}

	}

	private static class ForSimpleObject {

		private Integer longMethod(long arg0, String arg1) {
			return (int) arg0 + Integer.parseInt(arg1);
		}

		private SimpleObject simpleObjectMethod(SimpleObject arg0) {
			return arg0;
		}

	}

	interface InterfaceForSimpleObject {
		Integer longMethod(long arg0, String arg1);

		SimpleObjectInterface simpleObjectMethod(SimpleObjectInterface arg0);
	}

	interface SimpleObjectInterface {
		String getString();
		void setString(String s);
	}

	private static class SimpleObject {
		private String string;
		
		public static SimpleObject build(String s) {
			SimpleObject simpleObject = new SimpleObject();
			simpleObject.string = s;
			return simpleObject;
		}
		
		public String getString() {
			return string;
		}
		
		@Override
		public boolean equals(Object obj) {
			return ((SimpleObject) obj).string.equals(string);
		}
	}

}
