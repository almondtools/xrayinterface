package com.almondtools.xrayinterface;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.almondtools.xrayinterface.FieldGetter;
import com.almondtools.xrayinterface.FieldSetter;

public class FieldSetterTest {

	@Test
	public void testSetField() throws Throwable {
		WithField object = new WithField();
		Object result = new FieldSetter(WithField.class.getDeclaredField("field")).invoke(object, new Object[] { "hello" });
		assertThat(result, nullValue());
		assertThat(object.field, equalTo("hello"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetFieldFailingSignatureNone() throws Throwable {
		WithField object = new WithField();
		new FieldSetter(WithField.class.getDeclaredField("field")).invoke(object, new Object[0]);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetFieldFailingSignatureNull() throws Throwable {
		WithField object = new WithField();
		new FieldSetter(WithField.class.getDeclaredField("field")).invoke(object, (Object[]) null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetFieldFailingSignature2() throws Throwable {
		WithField object = new WithField();
		new FieldSetter(WithField.class.getDeclaredField("field")).invoke(object, new Object[] { "hello", "world" });
	}

	@Test(expected = ClassCastException.class)
	public void testSetFieldWithoutMatchingType() throws Throwable {
		WithField object = new WithField();
		new FieldSetter(WithField.class.getDeclaredField("field")).invoke(object, new Object[] { Integer.valueOf(1) });
	}

	@Test
	public void testSetFieldFinal() throws Throwable {
		WithFinalField object = new WithFinalField();
		Object result = new FieldSetter(WithFinalField.class.getDeclaredField("runtime")).invoke(object, new Object[] { "hello" });
		assertThat(result, nullValue());
		assertThat(object.runtime, equalTo("hello"));
	}

	@Test
	public void testSetFieldCompileTimeFinal() throws Throwable {
		WithFinalField object = new WithFinalField();
		Object voidresult = new FieldSetter(WithFinalField.class.getDeclaredField("compiletime")).invoke(object, new Object[] { "hello" });
		assertThat(voidresult, nullValue());
		assertThat(object.compiletime, equalTo("")); // paradox in source code, effect of inlining (see byte code of this line)
		Object result = new FieldGetter(WithFinalField.class.getDeclaredField("compiletime")).invoke(object, new Object[0]);
		assertThat(result, equalTo((Object) "hello"));
	}

	@Test
	public void testConvertingSetField() throws Throwable {
		ConvertingWithField object = new ConvertingWithField();
		Object result = new FieldSetter(ConvertingWithField.class.getDeclaredField("field"), ConvertingInterface.class).invoke(object, new Object[] { proxy("hello") });
		assertThat(result, nullValue());
		assertThat(object.field.content, equalTo("hello"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConvertingSetFieldFailingSignatureNone() throws Throwable {
		ConvertingWithField object = new ConvertingWithField();
		new FieldSetter(ConvertingWithField.class.getDeclaredField("field"), ConvertingInterface.class).invoke(object, new Object[0]);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConvertingSetFieldFailingSignatureNull() throws Throwable {
		ConvertingWithField object = new ConvertingWithField();
		new FieldSetter(ConvertingWithField.class.getDeclaredField("field"), ConvertingInterface.class).invoke(object, (Object[]) null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConvertingSetFieldFailingSignature2() throws Throwable {
		ConvertingWithField object = new ConvertingWithField();
		new FieldSetter(ConvertingWithField.class.getDeclaredField("field"), ConvertingInterface.class).invoke(object, new Object[] { proxy("hello"), "world" });
	}

	@Test(expected = ClassCastException.class)
	public void testConvertingSetFieldWithoutMatchingType() throws Throwable {
		ConvertingWithField object = new ConvertingWithField();
		new FieldSetter(ConvertingWithField.class.getDeclaredField("other"), String.class).invoke(object, new Object[] { Integer.valueOf(1) });
	}

	@Test
	public void testConvertingSetFieldNonConvertible() throws Throwable {
		ConvertingWithField object = new ConvertingWithField();
		Object result = new FieldSetter(ConvertingWithField.class.getDeclaredField("other"), String.class).invoke(object, new Object[] { "hello" });
		assertThat(result, nullValue());
		assertThat(object.other, equalTo("hello"));
	}

	@Test
	public void testConvertingSetFieldFinal() throws Throwable {
		ConvertingWithFinalField object = new ConvertingWithFinalField();
		Object result = new FieldSetter(ConvertingWithFinalField.class.getDeclaredField("finalfield"), ConvertingInterface.class).invoke(object, new Object[] { proxy("hello") });
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
