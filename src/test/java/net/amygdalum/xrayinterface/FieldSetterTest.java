package net.amygdalum.xrayinterface;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

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
	public void testGetFieldName() throws Exception {
		assertThat(new FieldSetter("field", null).getFieldName(), equalTo("field"));
	}

	@Test
	public void testGetResultType() throws Exception {
		assertThat(new FieldSetter("field", setterFor(WithField.class, "field")).getType(), equalTo(String.class));
	}

	@Test
	public void testGetTarget() throws Exception {
		assertThat(new FieldSetter("field", setterFor(WithField.class, "field")).getTarget(), nullValue());
	}

	@Test
	public void testGetTargetConverted() throws Exception {
		assertThat(new FieldSetter("field", setterFor(WithConvertedField.class, "field"), ConvertedInterface.class).getTarget(), equalTo(ConvertedInterface.class));
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
	public void testConvertedSetField() throws Throwable {
		WithConvertedField object = new WithConvertedField();
		Object result = new FieldSetter("field", setterFor(WithConvertedField.class, "field"), ConvertedInterface.class).invoke(object, new Object[] { proxy("hello") });
		assertThat(result, nullValue());
		assertThat(object.field.content, equalTo("hello"));
	}

	@Test
	public void testConvertedSetFieldNull() throws Throwable {
		WithConvertedField object = new WithConvertedField();
		Object result = new FieldSetter("field", setterFor(WithConvertedField.class, "field"), ConvertedInterface.class).invoke(object, new Object[] { null });
		assertThat(result, nullValue());
		assertThat(object.field, nullValue());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConvertedSetFieldFailingSignatureNone() throws Throwable {
		WithConvertedField object = new WithConvertedField();
		new FieldSetter("field", setterFor(WithConvertedField.class, "field"), ConvertedInterface.class).invoke(object, new Object[0]);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConvertedSetFieldFailingSignatureNull() throws Throwable {
		WithConvertedField object = new WithConvertedField();
		new FieldSetter("field", setterFor(WithConvertedField.class, "field"), ConvertedInterface.class).invoke(object, (Object[]) null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConvertedSetFieldFailingSignature2() throws Throwable {
		WithConvertedField object = new WithConvertedField();
		new FieldSetter("field", setterFor(WithConvertedField.class, "field"), ConvertedInterface.class).invoke(object, new Object[] { proxy("hello"), "world" });
	}

	@Test(expected = ClassCastException.class)
	public void testConvertedSetFieldWithoutMatchingType() throws Throwable {
		WithConvertedField object = new WithConvertedField();
		new FieldSetter("other", setterFor(WithConvertedField.class, "other"), String.class).invoke(object, new Object[] { Integer.valueOf(1) });
	}

	@Test
	public void testConvertedSetFieldNonConvertible() throws Throwable {
		WithConvertedField object = new WithConvertedField();
		Object result = new FieldSetter("other", setterFor(WithConvertedField.class, "other"), String.class).invoke(object, new Object[] { "hello" });
		assertThat(result, nullValue());
		assertThat(object.other, equalTo("hello"));
	}

	@Test
	public void testConvertedSetFieldFinal() throws Throwable {
		WithConvertedFinalField object = new WithConvertedFinalField();
		Object result = new FieldSetter("finalfield", setterFor(WithConvertedFinalField.class, "finalfield"), ConvertedInterface.class).invoke(object, new Object[] { proxy("hello") });
		assertThat(result, nullValue());
		assertThat(object.finalfield.content, equalTo("hello"));
	}

	private static ConvertedInterface proxy(final String s) {
		return new ConvertedInterface() {

			@Override
			public String getContent() {
				return s;
			}

			@Override
			public void setContent(String s) {
			}

		};
	}

	interface ConvertedInterface {
		String getContent();

		void setContent(String s);
	}

	private static class WithConvertedField {

		public String other = "hello";
		public ConvertedField field = new ConvertedField();
	}

	private static class WithConvertedFinalField {

		public final ConvertedField finalfield = new ConvertedField();
	}

	private static class ConvertedField {

		public String content = "world";

		public ConvertedField() {
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
