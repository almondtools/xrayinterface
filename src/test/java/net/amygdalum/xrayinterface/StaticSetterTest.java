package net.amygdalum.xrayinterface;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class StaticSetterTest {

	private Lookup lookup;

	@BeforeEach
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
		assertThat(new StaticSetter("field", null).getFieldName(), equalTo("field"));
	}

	@Test
	public void testGetResultType() throws Exception {
		assertThat(new StaticSetter("field", setterFor(WithField.class, "field")).getType(), equalTo(String.class));
	}

	@Test
	public void testGetTarget() throws Exception {
		assertThat(new StaticSetter("field", setterFor(WithField.class, "field")).getTarget(), nullValue());
	}

	@Test
	public void testGetTargetConverted() throws Exception {
		assertThat(new StaticSetter("field", setterFor(WithConvertedField.class, "field"), ConvertedInterface.class).getTarget(), equalTo(ConvertedInterface.class));
	}

	@Test
	public void testSetField() throws Throwable {
		Object result = new StaticSetter("field", setterFor(WithField.class, "field")).invoke(null, new Object[] { "hello" });
		assertThat(result, nullValue());
		assertThat(WithField.field, equalTo("hello"));
	}

	@Test
	public void testSetFieldFailingSignatureNone() throws Throwable {
		assertThrows(IllegalArgumentException.class, () -> {
			new StaticSetter("field", setterFor(WithField.class, "field")).invoke(null, new Object[0]);
		});
	}

	@Test
	public void testSetFieldFailingSignatureNull() throws Throwable {
		assertThrows(IllegalArgumentException.class, () -> {
			new StaticSetter("field", setterFor(WithField.class, "field")).invoke(null, (Object[]) null);
		});
	}

	@Test
	public void testSetFieldFailingSignature2() throws Throwable {
		assertThrows(IllegalArgumentException.class, () -> {
			new StaticSetter("field", setterFor(WithField.class, "field")).invoke(null, new Object[] { "hello", "world" });
		});
	}

	@Test
	public void testSetFieldWithoutMatchingType() throws Throwable {
		assertThrows(ClassCastException.class, () -> {
			new StaticSetter("field", setterFor(WithField.class, "field")).invoke(null, new Object[] { Integer.valueOf(1) });
		});
	}

	@Test
	public void testSetStaticFinalFieldIsUnsupported() throws Throwable {
		assertThrows(IllegalAccessException.class, () -> {
			setterFor(WithStaticFinalField.class, "RUNTIME");
		});
	}

	@Test
	public void testSetStaticFinalFieldCompileTimeIsUnsupported() throws Throwable {
		assertThrows(IllegalAccessException.class, () -> {
			setterFor(WithStaticFinalField.class, "COMPILETIME");
		});
	}

	@Test
	public void testGetStaticFinalField() throws Throwable {
		Object result = new StaticGetter("RUNTIME", getterFor(WithStaticFinalField.class, "RUNTIME")).invoke(null, new Object[0]);

		assertThat(result, equalTo((Object) "ABC"));
	}

	@Test
	public void testInvokeWithArgumentConversion() throws Throwable {
		StaticSetter staticMethod = new StaticSetter("field", setterFor(WithConvertedField.class, "field"), ConvertedInterface.class);
		staticMethod.invoke(null, new ConvertedInterface() {
		});
		assertThat(WithConvertedField.field, notNullValue());
	}

	private static class WithField {

		private static String field;
	}

	private static class WithStaticFinalField {

		@SuppressWarnings("unused")
		static final String RUNTIME = "ABC".toString();
		@SuppressWarnings("unused")
		static final String COMPILETIME = "ABC";
	}

	private static class WithConvertedField {

		private static WithConvertedField field = null;

	}

	interface ConvertedInterface {
	}
}
