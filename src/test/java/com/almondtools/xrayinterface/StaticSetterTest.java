package com.almondtools.xrayinterface;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.almondtools.xrayinterface.Convert;
import com.almondtools.xrayinterface.StaticGetter;
import com.almondtools.xrayinterface.StaticSetter;

public class StaticSetterTest {
	
	@Test
	public void testSetField() throws Throwable {
		Object result = new StaticSetter(WithField.class, WithField.class.getDeclaredField("field")).invoke(new Object[] { "hello" });
		assertThat(result, nullValue());
		assertThat(WithField.field, equalTo("hello"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetFieldFailingSignatureNone() throws Throwable {
		new StaticSetter(WithField.class, WithField.class.getDeclaredField("field")).invoke(new Object[0]);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetFieldFailingSignatureNull() throws Throwable {
		new StaticSetter(WithField.class, WithField.class.getDeclaredField("field")).invoke((Object[]) null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetFieldFailingSignature2() throws Throwable {
		new StaticSetter(WithField.class, WithField.class.getDeclaredField("field")).invoke(new Object[] { "hello", "world" });
	}

	@Test(expected = ClassCastException.class)
	public void testSetFieldWithoutMatchingType() throws Throwable {
		new StaticSetter(WithField.class, WithField.class.getDeclaredField("field")).invoke(new Object[] { Integer.valueOf(1) });
	}

	@Test
	public void testSetStaticFinalField() throws Throwable {
		Object result = new StaticSetter(WithField.class, WithStaticFinalField.class.getDeclaredField("RUNTIME")).invoke(new Object[] { "hello" });
		assertThat(result, nullValue());
		assertThat(WithStaticFinalField.RUNTIME, equalTo("hello"));
	}

	@Test
	public void testSetStaticFinalFieldCompileTime() throws Throwable {
		Object voidresult = new StaticSetter(WithField.class, WithStaticFinalField.class.getDeclaredField("COMPILETIME")).invoke(new Object[] { "hello" });
		assertThat(voidresult, nullValue());
		assertThat(WithStaticFinalField.COMPILETIME, equalTo("ABC"));// paradox in source code, effect of inlining (see byte code of this line)
		Object result = new StaticGetter(WithField.class, WithStaticFinalField.class.getDeclaredField("COMPILETIME")).invoke(new Object[0]);
		assertThat(result, equalTo((Object) "hello"));
	}

	@Test
	public void testInvokeWithArgumentConversion() throws Throwable {
		StaticSetter staticMethod = new StaticSetter(WithConvertedProperty.class, WithConvertedProperty.class.getDeclaredField("converted"), ConvertedInterface.class);
		staticMethod.invoke(new ConvertedInterface() {
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
