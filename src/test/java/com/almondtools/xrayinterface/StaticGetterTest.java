package com.almondtools.xrayinterface;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.almondtools.xrayinterface.Convert;
import com.almondtools.xrayinterface.InterfaceMismatchException;
import com.almondtools.xrayinterface.StaticGetter;



public class StaticGetterTest {

	@Test
	public void testGetField() throws Throwable {
		Object result = new StaticGetter(WithField.class, WithField.class.getDeclaredField("field")).invoke(new Object[0]);
		assertThat((String) result, equalTo("world"));
	}

	@Test(expected=IllegalArgumentException.class)
	public void testGetFieldWithFailingSignatureOne() throws Throwable {
		new StaticGetter(WithField.class, WithField.class.getDeclaredField("field")).invoke(new Object[]{1});
	}

	@Test
	public void testGetFieldWithFailingSignatureNull() throws Throwable {
		Object result = new StaticGetter(WithField.class, WithField.class.getDeclaredField("field")).invoke((Object[]) null);
		assertThat((String) result, equalTo("world"));
	}

	@Test
	public void testInvokeWithResultConversion() throws Throwable {
		StaticGetter staticMethod = new StaticGetter(WithConvertedProperty.class, WithConvertedProperty.class.getDeclaredField("converted"), ConvertedInterface.class);
		Object result = staticMethod.invoke();
		assertThat(result, instanceOf(ConvertedInterface.class));
	}
	
	@Test(expected = InterfaceMismatchException.class)
	public void testConvertingGetFieldContravariant() throws Throwable {
		StaticGetter staticMethod = new StaticGetter(WithConvertedProperty.class, WithConvertedProperty.class.getDeclaredField("converted"), ContravariantInterface.class);
		staticMethod.invoke();
	}

	@Test
	public void testConvertingGetFieldConvertedContravariant() throws Throwable {
		StaticGetter staticMethod = new StaticGetter(WithConvertedProperty.class, WithConvertedProperty.class.getDeclaredField("converted"), ConvertedContravariantInterface.class);
		Object result = staticMethod.invoke();
		assertThat(result, instanceOf(ConvertedContravariantInterface.class));
		assertThat(((ConvertedContravariantInterface) result).getOther().toString(), equalTo("other"));
	}

	interface Properties {
		@Convert("WithConvertedProperty") ConvertedInterface getConverted();
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

		@Convert CharSequence getOther();
	}
}

