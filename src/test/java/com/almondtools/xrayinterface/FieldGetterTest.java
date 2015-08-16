package com.almondtools.xrayinterface;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;

import org.junit.Before;
import org.junit.Test;

import com.almondtools.xrayinterface.StaticSetterTest.ConvertedInterface;

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
		public void testGetFieldName() throws Exception {
			assertThat(new FieldGetter("field", null).getFieldName(), equalTo("field"));
		}

	@Test
	public void testGetResultType() throws Exception {
		assertThat(new FieldGetter("field", getterFor(WithField.class, "field")).getResultType(), equalTo(String.class));
	}

	@Test
	public void testGetTarget() throws Exception {
		assertThat(new FieldGetter("field", getterFor(WithField.class, "field")).getTarget(), nullValue());
	}

	@Test
	public void testGetTargetConverted() throws Exception {
		assertThat(new FieldGetter("field", getterFor(WithConvertedField.class, "field"), ConvertedInterface.class).getTarget(), equalTo(ConvertedInterface.class));
	}

	@Test
	public void testGetField() throws Throwable {
		Object result = new FieldGetter("field", getterFor(WithField.class, "field")).invoke(new WithField(), new Object[0]);
		assertThat((String) result, equalTo("world"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetFieldWithFailingSignatureOne() throws Throwable {
		new FieldGetter("field", getterFor(WithField.class, "field")).invoke(new WithField(), new Object[] { 1 });
	}

	@Test
	public void testGetFieldWithFailingSignatureNull() throws Throwable {
		Object result = new FieldGetter("field", getterFor(WithField.class, "field")).invoke(new WithField(), (Object[]) null);
		assertThat((String) result, equalTo("world"));
	}

	@Test
	public void testConvertedGetField() throws Throwable {
		Object result = new FieldGetter("field", getterFor(WithConvertedField.class, "field"), ConvertedInterface.class).invoke(new WithConvertedField(), new Object[0]);
		assertThat(result, instanceOf(ConvertedInterface.class));
		assertThat(((ConvertedInterface) result).getContent(), equalTo("world"));
	}

	@Test
	public void testConvertedGetFieldNull() throws Throwable {
		WithConvertedField object = new WithConvertedField();
		object.field = null;
		Object result = new FieldGetter("field", getterFor(WithConvertedField.class, "field"), ConvertedInterface.class).invoke(object, new Object[0]);
		assertThat(result, nullValue());
	}

	@Test(expected = InterfaceMismatchException.class)
	public void testConvertedGetFieldContravariant() throws Throwable {
		new FieldGetter("field", getterFor(WithConvertedField.class, "field"), ContravariantInterface.class).invoke(new WithConvertedField(), new Object[0]);
	}

	@Test
	public void testConvertedGetFieldConvertedContravariant() throws Throwable {
		Object result = new FieldGetter("field", getterFor(WithConvertedField.class, "field"), ConvertedContravariantInterface.class).invoke(new WithConvertedField(), new Object[0]);
		assertThat(result, instanceOf(ConvertedContravariantInterface.class));
		assertThat(((ConvertedContravariantInterface) result).getContent().toString(), equalTo("world"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConvertedGetFieldWithFailingSignatureOne() throws Throwable {
		new FieldGetter("field", getterFor(WithConvertedField.class, "field"), ConvertedInterface.class).invoke(new WithConvertedField(), new Object[] { 1 });
	}

	@Test
	public void testConvertedGetFieldWithFailingSignatureNull() throws Throwable {
		Object result = new FieldGetter("field", getterFor(WithConvertedField.class, "field"), ConvertedInterface.class).invoke(new WithConvertedField(), (Object[]) null);
		assertThat(result, instanceOf(ConvertedInterface.class));
		assertThat(((ConvertedInterface) result).getContent(), equalTo("world"));
	}

	@Test
	public void testConvertedGetFieldNonConvertible() throws Throwable {
		Object result = new FieldGetter("field", getterFor(WithConvertedField.class, "other"), String.class).invoke(new WithConvertedField(), new Object[0]);
		assertThat(result, equalTo((Object) "hello"));
	}

	interface ConvertedInterface {
		String getContent();
	}

	interface ContravariantInterface {
		CharSequence getContent();
	}

	interface ConvertedContravariantInterface {
		@Convert
		CharSequence getContent();
	}

	private static class WithConvertedField {

		public String other = "hello";
		public ConvertedField field = new ConvertedField("world");
	}

	private static class ConvertedField {

		private String content;

		public ConvertedField(String content) {
			this.content = content;
		}

	}

	private static class WithField {

		private String field = "world";
	}

}
