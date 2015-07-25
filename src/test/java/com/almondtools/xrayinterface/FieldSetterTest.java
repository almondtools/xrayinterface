package com.almondtools.xrayinterface;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;

import org.junit.Before;
import org.junit.Test;

public class FieldSetterTest {

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
	public void testGetName() throws Exception {
		assertThat(new FieldSetter("field", null).getName(), equalTo("field"));
	}

	@Test
	public void testGetResultType() throws Exception {
		assertThat(new FieldSetter("field", setterFor(WithField.class, "field")).getType(), equalTo(String.class));
	}

	@Test
	public void testSetField() throws Throwable {
		WithField object = new WithField();
		Object result = new FieldSetter("field", setterFor(WithField.class, "field")).invoke(object, new Object[] { "hello" });
		assertThat(result, nullValue());
		assertThat(object.field, equalTo("hello"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetFieldFailingSignatureNone() throws Throwable {
		WithField object = new WithField();
		new FieldSetter("field", setterFor(WithField.class, "field")).invoke(object, new Object[0]);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetFieldFailingSignatureNull() throws Throwable {
		WithField object = new WithField();
		new FieldSetter("field", setterFor(WithField.class, "field")).invoke(object, (Object[]) null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetFieldFailingSignature2() throws Throwable {
		WithField object = new WithField();
		new FieldSetter("field", setterFor(WithField.class, "field")).invoke(object, new Object[] { "hello", "world" });
	}

	@Test(expected = ClassCastException.class)
	public void testSetFieldWithoutMatchingType() throws Throwable {
		WithField object = new WithField();
		new FieldSetter("field", setterFor(WithField.class, "field")).invoke(object, new Object[] { Integer.valueOf(1) });
	}

	@Test
	public void testSetFieldFinal() throws Throwable {
		WithFinalField object = new WithFinalField();
		Object result = new FieldSetter("runtime", setterFor(WithFinalField.class, "runtime")).invoke(object, new Object[] { "hello" });
		assertThat(result, nullValue());
		assertThat(object.runtime, equalTo("hello"));
	}

	@Test
	public void testSetFieldCompileTimeFinal() throws Throwable {
		WithFinalField object = new WithFinalField();
		Object voidresult = new FieldSetter("compiletime", setterFor(WithFinalField.class, "compiletime")).invoke(object, new Object[] { "hello" });
		assertThat(voidresult, nullValue());
		assertThat(object.compiletime, equalTo("")); // paradox in source code, effect of inlining (see byte code of this line)
		Object result = new FieldGetter("compiletime", getterFor(WithFinalField.class, "compiletime")).invoke(object, new Object[0]);
		assertThat(result, equalTo((Object) "hello"));
	}

	@Test
	public void testConvertingSetField() throws Throwable {
		ConvertingWithField object = new ConvertingWithField();
		Object result = new FieldSetter("field", setterFor(ConvertingWithField.class, "field"), ConvertingInterface.class).invoke(object, new Object[] { proxy("hello") });
		assertThat(result, nullValue());
		assertThat(object.field.content, equalTo("hello"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConvertingSetFieldFailingSignatureNone() throws Throwable {
		ConvertingWithField object = new ConvertingWithField();
		new FieldSetter("field", setterFor(ConvertingWithField.class, "field"), ConvertingInterface.class).invoke(object, new Object[0]);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConvertingSetFieldFailingSignatureNull() throws Throwable {
		ConvertingWithField object = new ConvertingWithField();
		new FieldSetter("field", setterFor(ConvertingWithField.class, "field"), ConvertingInterface.class).invoke(object, (Object[]) null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConvertingSetFieldFailingSignature2() throws Throwable {
		ConvertingWithField object = new ConvertingWithField();
		new FieldSetter("field", setterFor(ConvertingWithField.class, "field"), ConvertingInterface.class).invoke(object, new Object[] { proxy("hello"), "world" });
	}

	@Test(expected = ClassCastException.class)
	public void testConvertingSetFieldWithoutMatchingType() throws Throwable {
		ConvertingWithField object = new ConvertingWithField();
		new FieldSetter("other", setterFor(ConvertingWithField.class, "other"), String.class).invoke(object, new Object[] { Integer.valueOf(1) });
	}

	@Test
	public void testConvertingSetFieldNonConvertible() throws Throwable {
		ConvertingWithField object = new ConvertingWithField();
		Object result = new FieldSetter("other", setterFor(ConvertingWithField.class, "other"), String.class).invoke(object, new Object[] { "hello" });
		assertThat(result, nullValue());
		assertThat(object.other, equalTo("hello"));
	}

	@Test
	public void testConvertingSetFieldFinal() throws Throwable {
		ConvertingWithFinalField object = new ConvertingWithFinalField();
		Object result = new FieldSetter("finalfield", setterFor(ConvertingWithFinalField.class, "finalfield"), ConvertingInterface.class).invoke(object, new Object[] { proxy("hello") });
		assertThat(result, nullValue());
		assertThat(object.finalfield.content, equalTo("hello"));
	}

	private static ConvertingInterface proxy(final String s) {
		return new ConvertingInterface() {

			@Override
			public String getContent() {
				return s;
			}

			@Override
			public void setContent(String s) {
			}

		};
	}

	interface ConvertingInterface {
		String getContent();

		void setContent(String s);
	}

	private static class ConvertingWithField {

		public String other = "hello";
		public ConvertibleField field = new ConvertibleField();
	}

	private static class ConvertingWithFinalField {

		public final ConvertibleField finalfield = new ConvertibleField();
	}

	private static class ConvertibleField {

		public String content = "world";

		public ConvertibleField() {
		}

	}

	private static class WithField {

		private String field;
	}

	private static class WithFinalField {

		private final String runtime = "".toString();
		private final String compiletime = ""; // yet this line is inlined - in this case changes to it will not effect users of this variable
	}

}
