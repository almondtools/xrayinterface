package com.almondtools.xrayinterface;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;


@SuppressWarnings("unused")
public class ConstructorInvokerTest {

	private Lookup lookup;

	@Before
	public void before() throws Exception {
		this.lookup = MethodHandles.lookup();
	}

	private MethodHandle constructorOf(Class<?> clazz, Class<?>... parameters) throws IllegalAccessException, NoSuchMethodException {
		Constructor<?> constructor = clazz.getDeclaredConstructor(parameters);
		return constructorOf(constructor);
	}

	private MethodHandle constructorOf(Constructor<?> constructor) throws IllegalAccessException {
		constructor.setAccessible(true);
		return lookup.unreflectConstructor(constructor);
	}

	@Test
	public void testInvokeWithoutProblems() throws Throwable {
		Object result = new ConstructorInvoker(constructorOf(WithConstructor.class)).invoke(null, new Object[0]);
		assertThat(result, instanceOf(WithConstructor.class));
	}
	
	@Test
	public void testInvokeWithImplicitConstructor() throws Throwable {
		Object resultOnClass = new ConstructorInvoker(constructorOf(WithImplicitConstructor.class)).invoke(null, new Object[0]);
		assertThat(resultOnClass, instanceOf(WithImplicitConstructor.class));

		Object resultOnConstructor = new ConstructorInvoker(constructorOf(WithImplicitConstructor.class)).invoke(null, new Object[0]);
		assertThat(resultOnConstructor, instanceOf(WithImplicitConstructor.class));
	}
	
	@Test(expected=NullPointerException.class)
	public void testInvokeWithNPEConstructor() throws Throwable {
		Object result = new ConstructorInvoker(constructorOf(WithConstructor.class, boolean.class)).invoke(null, new Object[]{Boolean.FALSE});
	}
	
	@Test(expected=IOException.class)
	public void testInvokeWithIOConstructor() throws Throwable {
		Object result = new ConstructorInvoker(constructorOf(WithConstructor.class, boolean.class)).invoke(null, new Object[]{Boolean.TRUE});
	}
	
	@Test
	public void testInvokeWithArgumentConversion() throws Throwable {
		Method method = Constructors.class.getDeclaredMethod("create");
		ConstructorInvoker constructor = new ConstructorInvoker(constructorOf(WithConvertedConstructor.class), method.getReturnType(), method.getParameterTypes());
		Object result = constructor.invoke(null, new Object[0]);
		assertThat(result, instanceOf(ConvertedInterface.class));
	}
	
	@Test
	public void testInvokeWithResultConversion() throws Throwable {
		Method method = Constructors.class.getDeclaredMethod("create", ConvertedInterface.class);
		ConstructorInvoker constructor = new ConstructorInvoker(constructorOf(WithConvertedConstructor.class, WithConvertedConstructor.class), method.getReturnType(), method.getParameterTypes());
		Object result = constructor.invoke(null, new ConvertedInterface() {
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
