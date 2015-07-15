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

@SuppressWarnings("unused")
public class FieldGetterTest {

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
	public void testGetField() throws Throwable {
		Object result = new FieldGetter(getterFor(WithField.class, "field")).invoke(new WithField(), new Object[0]);
		assertThat((String) result, equalTo("world"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetFieldWithFailingSignatureOne() throws Throwable {
		new FieldGetter(getterFor(WithField.class, "field")).invoke(new WithField(), new Object[] { 1 });
	}

	@Test
	public void testGetFieldWithFailingSignatureNull() throws Throwable {
		Object result = new FieldGetter(getterFor(WithField.class, "field")).invoke(new WithField(), (Object[]) null);
		assertThat((String) result, equalTo("world"));
	}

	@Test
	public void testConvertingGetField() throws Throwable {
		Object result = new FieldGetter(getterFor(ConvertibleWithField.class, "field"), ConvertingInterface.class).invoke(new ConvertibleWithField(), new Object[0]);
		assertThat(result, instanceOf(ConvertingInterface.class));
		assertThat(((ConvertingInterface) result).getContent(), equalTo("world"));
	}

	@Test(expected = InterfaceMismatchException.class)
	public void testConvertingGetFieldContravariant() throws Throwable {
		new FieldGetter(getterFor(ConvertibleWithField.class, "field"), ContravariantInterface.class).invoke(new ConvertibleWithField(), new Object[0]);
	}

	@Test
	public void testConvertingGetFieldConvertedContravariant() throws Throwable {
		Object result = new FieldGetter(getterFor(ConvertibleWithField.class, "field"), ConvertedContravariantInterface.class).invoke(new ConvertibleWithField(), new Object[0]);
		assertThat(result, instanceOf(ConvertedContravariantInterface.class));
		assertThat(((ConvertedContravariantInterface) result).getContent().toString(), equalTo("world"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConvertingGetFieldWithFailingSignatureOne() throws Throwable {
		new FieldGetter(getterFor(ConvertibleWithField.class, "field"), ConvertingInterface.class).invoke(new ConvertibleWithField(), new Object[] { 1 });
	}

	@Test
	public void testConvertingGetFieldWithFailingSignatureNull() throws Throwable {
		Object result = new FieldGetter(getterFor(ConvertibleWithField.class, "field"), ConvertingInterface.class).invoke(new ConvertibleWithField(), (Object[]) null);
		assertThat(result, instanceOf(ConvertingInterface.class));
		assertThat(((ConvertingInterface) result).getContent(), equalTo("world"));
	}

	@Test
	public void testConvertingGetFieldNonConvertible() throws Throwable {
		Object result = new FieldGetter(getterFor(ConvertibleWithField.class, "other"), String.class).invoke(new ConvertibleWithField(), new Object[0]);
		assertThat(result, equalTo((Object) "hello"));
	}

	interface ConvertingInterface {
		String getContent();
	}

	interface ContravariantInterface {
		CharSequence getContent();
	}

	interface ConvertedContravariantInterface {
		@Convert
		CharSequence getContent();
	}

	private static class ConvertibleWithField {

		private String other = "hello";
		private ConvertibleField field = new ConvertibleField("world");
	}

	private static class ConvertibleField {

		private String content;

		public ConvertibleField(String content) {
			this.content = content;
		}

	}

	private static class WithField {

		private String field = "world";
	}

}
