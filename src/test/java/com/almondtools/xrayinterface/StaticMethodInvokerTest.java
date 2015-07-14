package com.almondtools.xrayinterface;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.lang.reflect.Method;

import org.junit.Test;

import com.almondtools.xrayinterface.Convert;
import com.almondtools.xrayinterface.StaticMethodInvoker;

@SuppressWarnings("unused")
public class StaticMethodInvokerTest {

	@Test
	public void testInvoke() throws Throwable {
		Object invoke = new StaticMethodInvoker(WithStaticMethod.class, WithStaticMethod.class.getDeclaredMethod("staticMethod", int.class)).invoke(new Object[] { 1 });
		assertThat((String) invoke, equalTo("1"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvokeFailingSignature() throws Throwable {
		new StaticMethodInvoker(WithStaticMethod.class, WithStaticMethod.class.getDeclaredMethod("staticMethod", int.class)).invoke(new Object[] { "1" });
	}

	@Test(expected = IOException.class)
	public void testInvokeCheckedException() throws Throwable {
		new StaticMethodInvoker(WithStaticMethod.class, WithStaticMethod.class.getDeclaredMethod("staticException", int.class)).invoke(new Object[] { 2 });
	}

	@Test(expected = NullPointerException.class)
	public void testInvokeUncheckedException() throws Throwable {
		new StaticMethodInvoker(WithStaticMethod.class, WithStaticMethod.class.getDeclaredMethod("staticException", int.class)).invoke(new Object[] { 1 });
	}

	@Test
	public void testInvokeWithArgumentConversion() throws Throwable {
		Method method = Methods.class.getDeclaredMethod("converted", ConvertedInterface.class);
		StaticMethodInvoker staticMethod = new StaticMethodInvoker(WithConvertedMethods.class, WithConvertedMethods.class.getDeclaredMethod("converted", WithConvertedMethods.class), method );
		Object result = staticMethod.invoke(new ConvertedInterface() {
		});
		assertThat(result, equalTo((Object) Integer.valueOf(-1)));
	}
	
	@Test
	public void testInvokeWithResultConversion() throws Throwable {
		Method method = Methods.class.getDeclaredMethod("converted");
		StaticMethodInvoker staticMethod = new StaticMethodInvoker(WithConvertedMethods.class, WithConvertedMethods.class.getDeclaredMethod("converted"), method );
		Object result = staticMethod.invoke();
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
