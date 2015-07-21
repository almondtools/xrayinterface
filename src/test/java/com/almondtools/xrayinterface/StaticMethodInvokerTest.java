package com.almondtools.xrayinterface;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;

import com.almondtools.xrayinterface.Convert;
import com.almondtools.xrayinterface.StaticMethodInvoker;

@SuppressWarnings("unused")
public class StaticMethodInvokerTest {

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
		Object invoke = new StaticMethodInvoker(methodOf(WithStaticMethod.class,"staticMethod", int.class)).invoke(null, new Object[] { 1 });
		assertThat((String) invoke, equalTo("1"));
	}

	@Test(expected = ClassCastException.class)
	public void testInvokeFailingSignature() throws Throwable {
		new StaticMethodInvoker(methodOf(WithStaticMethod.class,"staticMethod", int.class)).invoke(null, new Object[] { "1" });
	}

	@Test(expected = IOException.class)
	public void testInvokeCheckedException() throws Throwable {
		new StaticMethodInvoker(methodOf(WithStaticMethod.class,"staticException", int.class)).invoke(null, new Object[] { 2 });
	}

	@Test(expected = NullPointerException.class)
	public void testInvokeUncheckedException() throws Throwable {
		new StaticMethodInvoker(methodOf(WithStaticMethod.class,"staticException", int.class)).invoke(null, new Object[] { 1 });
	}

	@Test
	public void testInvokeWithArgumentConversion() throws Throwable {
		Method method = Methods.class.getDeclaredMethod("converted", ConvertedInterface.class);
		StaticMethodInvoker staticMethod = new StaticMethodInvoker(methodOf(WithConvertedMethods.class,"converted", WithConvertedMethods.class), method.getReturnType(), method.getParameterTypes());
		Object result = staticMethod.invoke(null, new ConvertedInterface() {
		});
		assertThat(result, equalTo((Object) Integer.valueOf(-1)));
	}
	
	@Test
	public void testInvokeWithResultConversion() throws Throwable {
		Method method = Methods.class.getDeclaredMethod("converted");
		StaticMethodInvoker staticMethod = new StaticMethodInvoker(methodOf(WithConvertedMethods.class,"converted"), method.getReturnType(), method.getParameterTypes());
		Object result = staticMethod.invoke(null);
		assertThat(result, instanceOf(ConvertedInterface.class));
	}
	
	interface Methods {
		@Convert("WithConvertedMethods") ConvertedInterface converted();
		int converted(@Convert("WithConvertedMethods") ConvertedInterface i);
	}
	
	private static class WithStaticMethod {
		private static String staticMethod(int i) {
			return String.valueOf(i);
		}

		private static String staticException(int i) throws IOException {
			if (i == 1) {
				throw new NullPointerException();
			} else {
				throw new IOException();
			}
		}
	}

	private static class WithConvertedMethods {
		
		public static WithConvertedMethods converted() {
			return new WithConvertedMethods();
		}

		public static int converted (WithConvertedMethods e) {
			return -1;
		}

	}
	
	interface ConvertedInterface {
	}
}
