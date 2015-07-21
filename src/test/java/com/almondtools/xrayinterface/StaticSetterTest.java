package com.almondtools.xrayinterface;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;

import org.junit.Before;
import org.junit.Test;

import com.almondtools.xrayinterface.Convert;
import com.almondtools.xrayinterface.StaticGetter;
import com.almondtools.xrayinterface.StaticSetter;

public class StaticSetterTest {

	private Lookup lookup;

	@Before
	public void before() throws Exception {
		this.lookup = MethodHandles.lookup();
	}

	private MethodHandle getterFor(Class<?> clazz, String field) throws IllegalAccessException, NoSuchFieldException {
		Field declaredField = clazz.getDeclaredField(field);
		declaredField.setAccessible(true);
		return lookup.unreflectGetter(declaredField);
	}

	private MethodHandle setterFor(Class<?> clazz, String field) throws IllegalAccessException, NoSuchFieldException {
		Field declaredField = clazz.getDeclaredField(field);
		declaredField.setAccessible(true);
		return lookup.unreflectSetter(declaredField);
	}

	@Test
	public void testSetField() throws Throwable {
		Object result = new StaticSetter(setterFor(WithField.class,"field")).invoke(null, new Object[] { "hello" });
		assertThat(result, nullValue());
		assertThat(WithField.field, equalTo("hello"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetFieldFailingSignatureNone() throws Throwable {
		new StaticSetter(setterFor(WithField.class,"field")).invoke(null, new Object[0]);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetFieldFailingSignatureNull() throws Throwable {
		new StaticSetter(setterFor(WithField.class,"field")).invoke(null, (Object[]) null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetFieldFailingSignature2() throws Throwable {
		new StaticSetter(setterFor(WithField.class,"field")).invoke(null, new Object[] { "hello", "world" });
	}

	@Test(expected = ClassCastException.class)
	public void testSetFieldWithoutMatchingType() throws Throwable {
		new StaticSetter(setterFor(WithField.class,"field")).invoke(null, new Object[] { Integer.valueOf(1) });
	}

	@Test
	public void testSetStaticFinalField() throws Throwable {
		Object result = new StaticSetter(setterFor(WithStaticFinalField.class,"RUNTIME")).invoke(null, new Object[] { "hello" });
		assertThat(result, nullValue());
		assertThat(WithStaticFinalField.RUNTIME, equalTo("hello"));
	}

	@Test
	public void testSetStaticFinalFieldCompileTime() throws Throwable {
		Object voidresult = new StaticSetter(setterFor(WithStaticFinalField.class,"COMPILETIME")).invoke(null, new Object[] { "hello" });
		assertThat(voidresult, nullValue());
		assertThat(WithStaticFinalField.COMPILETIME, equalTo("ABC"));// paradox in source code, effect of inlining (see byte code of this line)
		Object result = new StaticGetter(getterFor(WithStaticFinalField.class, "COMPILETIME")).invoke(null, new Object[0]);
		assertThat(result, equalTo((Object) "hello"));
	}

	@Test
	public void testInvokeWithArgumentConversion() throws Throwable {
		StaticSetter staticMethod = new StaticSetter(setterFor(WithConvertedProperty.class,"converted"), ConvertedInterface.class);
		staticMethod.invoke(null, new ConvertedInterface() {
		});
		assertThat(WithConvertedProperty.converted, notNullValue());
	}

	interface Properties {
		void setConverted(@Convert("WithConvertedProperty") ConvertedInterface i);
	}

	private static class WithField {

		private static String field;
	}

	private static class WithStaticFinalField {

		static final String RUNTIME = "ABC".toString();
		static final String COMPILETIME = "ABC";
	}

	private static class WithConvertedProperty {

		private static WithConvertedProperty converted = null;

	}

	interface ConvertedInterface {
	}
}
