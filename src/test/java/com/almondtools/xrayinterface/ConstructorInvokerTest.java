package com.almondtools.xrayinterface;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.lang.reflect.Method;

import org.junit.Test;

import com.almondtools.xrayinterface.ConstructorInvoker;
import com.almondtools.xrayinterface.Convert;


@SuppressWarnings("unused")
public class ConstructorInvokerTest {

	@Test
	public void testInvokeWithoutProblems() throws Throwable {
		Object result = new ConstructorInvoker(WithConstructor.class.getDeclaredConstructor()).invoke(new Object[0]);
		assertThat(result, instanceOf(WithConstructor.class));
	}
	
	@Test
	public void testInvokeWithImplicitConstructor() throws Throwable {
		Object resultOnClass = new ConstructorInvoker(WithImplicitConstructor.class).invoke(new Object[0]);
		assertThat(resultOnClass, instanceOf(WithImplicitConstructor.class));

		Object resultOnConstructor = new ConstructorInvoker(WithImplicitConstructor.class.getDeclaredConstructor()).invoke(new Object[0]);
		assertThat(resultOnConstructor, instanceOf(WithImplicitConstructor.class));
	}
	
	@Test(expected=NullPointerException.class)
	public void testInvokeWithNPEConstructor() throws Throwable {
		Object result = new ConstructorInvoker(WithConstructor.class.getDeclaredConstructor(boolean.class)).invoke(new Object[]{Boolean.FALSE});
	}
	
	@Test(expected=IOException.class)
	public void testInvokeWithIOConstructor() throws Throwable {
		Object result = new ConstructorInvoker(WithConstructor.class.getDeclaredConstructor(boolean.class)).invoke(new Object[]{Boolean.TRUE});
	}
	
	@Test
	public void testInvokeWithArgumentConversion() throws Throwable {
		Method method = Constructors.class.getDeclaredMethod("create");
		ConstructorInvoker constructor = new ConstructorInvoker(WithConvertedConstructor.class.getDeclaredConstructor(), method );
		Object result = constructor.invoke(new Object[0]);
		assertThat(result, instanceOf(ConvertedInterface.class));
	}
	
	@Test
	public void testInvokeWithResultConversion() throws Throwable {
		Method method = Constructors.class.getDeclaredMethod("create", ConvertedInterface.class);
		ConstructorInvoker constructor = new ConstructorInvoker(WithConvertedConstructor.class.getDeclaredConstructor(WithConvertedConstructor.class), method );
		Object result = constructor.invoke(new ConvertedInterface() {
		});
		assertThat(result, instanceOf(WithConvertedConstructor.class));
	}
	
	interface Constructors {
		@Convert("WithConvertedConstructor") ConvertedInterface create();
		WithConvertedConstructor create(@Convert("WithConvertedConstructor") ConvertedInterface i);
	}
	
	private static class WithConstructor {
		
		public WithConstructor() {
		}

		public WithConstructor(boolean checked) throws IOException {
			if (checked) {
				throw new IOException();
			} else {
				throw new NullPointerException();
			}
		}
	}

	private static class WithImplicitConstructor {
	}

	private static class WithConvertedConstructor {
		
		public WithConvertedConstructor() {
		}

		public WithConvertedConstructor(WithConvertedConstructor e) {
		}

	}
	
	interface ConvertedInterface {
	}

}
