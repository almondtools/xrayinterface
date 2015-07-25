package com.almondtools.xrayinterface;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;

import org.junit.Before;
import org.junit.Test;

public class StaticGetterTest {

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

	@Test
	public void testGetName() throws Exception {
		assertThat(new StaticGetter("field", null).getName(), equalTo("field"));
	}

	@Test
	public void testGetResultType() throws Exception {
		assertThat(new StaticGetter("field", getterFor(WithField.class, "field")).getResultType(), equalTo(String.class));
	}

	@Test
	public void testGetField() throws Throwable {
		Object result = new StaticGetter("field", getterFor(WithField.class, "field")).invoke(null, new Object[0]);
		assertThat((String) result, equalTo("world"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetFieldWithFailingSignatureOne() throws Throwable {
		new StaticGetter("field", getterFor(WithField.class, "field")).invoke(null, new Object[] { 1 });
	}

	@Test
	public void testGetFieldWithFailingSignatureNull() throws Throwable {
		Object result = new StaticGetter("field", getterFor(WithField.class, "field")).invoke(null, (Object[]) null);
		assertThat((String) result, equalTo("world"));
	}

	@Test
	public void testInvokeWithResultConversion() throws Throwable {
		StaticGetter staticMethod = new StaticGetter("converted", getterFor(WithConvertedProperty.class, "converted"), ConvertedInterface.class);
		Object result = staticMethod.invoke(null);
		assertThat(result, instanceOf(ConvertedInterface.class));
	}

	@Test(expected = InterfaceMismatchException.class)
	public void testConvertingGetFieldContravariant() throws Throwable {
		StaticGetter staticMethod = new StaticGetter("converted", getterFor(WithConvertedProperty.class, "converted"), ContravariantInterface.class);
		staticMethod.invoke(null);
	}

	@Test
	public void testConvertingGetFieldConvertedContravariant() throws Throwable {
		StaticGetter staticMethod = new StaticGetter("converted", getterFor(WithConvertedProperty.class, "converted"), ConvertedContravariantInterface.class);
		Object result = staticMethod.invoke(null);
		assertThat(result, instanceOf(ConvertedContravariantInterface.class));
		assertThat(((ConvertedContravariantInterface) result).getOther().toString(), equalTo("other"));
	}

	interface Properties {
		@Convert("WithConvertedProperty")
		ConvertedInterface getConverted();
	}

	@SuppressWarnings("unused")
	private static class WithField {

		private static String field = "world";
	}

	@SuppressWarnings("unused")
	private static class WithConvertedProperty {

		private static WithConvertedProperty converted = new WithConvertedProperty();
		private static String other = "other";

	}

	interface ConvertedInterface {
	}

	interface ContravariantInterface {

		CharSequence getOther();
	}

	interface ConvertedContravariantInterface {

		@Convert
		CharSequence getOther();
	}
}
