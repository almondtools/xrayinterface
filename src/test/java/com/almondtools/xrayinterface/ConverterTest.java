package com.almondtools.xrayinterface;

import static com.almondtools.xrayinterface.Converter.convertArguments;
import static com.almondtools.xrayinterface.Converter.convertResult;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Method;

import org.junit.Test;

import com.almondtools.xrayinterface.ObjectAccess;


public class ConverterTest {

	@Test
	public void testConvertArgumentsNull() throws Exception {
		Method method = staticLongMethod();
		Method target = interfaceLongMethod();
		assertThat(convertArguments(target.getParameterTypes(), method.getParameterTypes(), (Object[]) null), equalTo(new Object[0]));
	}

	@Test
	public void testConvertArgumentsKeepingValues() throws Exception {
		Method method = staticLongMethod();
		Method target = interfaceLongMethod();
		assertThat(convertArguments(target.getParameterTypes(), method.getParameterTypes(), 1, "3"), equalTo(new Object[] { 1, "3" }));
	}

	@Test
	public void testConvertResultKeepingValues() throws Exception {
		Method method = staticLongMethod();
		Method target = interfaceLongMethod();
		assertThat(convertResult(target.getReturnType(), method.getReturnType(), Integer.valueOf(2)), equalTo((Object) Integer.valueOf(2)));
	}

	@Test
	public void testConvertArgumentsConvertingValues() throws Exception {
		Method method = staticSimpleObjectMethod();
		Method target = interfaceSimpleObjectMethod();
		assertThat(convertArguments(target.getParameterTypes(), method.getParameterTypes(), simpleObjectInterface("value")), equalTo(new Object[] { SimpleObject.build("value") }));
	}

	@Test
	public void testConvertResultConvertingValues() throws Exception {
		Method method = staticSimpleObjectMethod();
		Method target = interfaceSimpleObjectMethod();
		assertThat(convertResult(target.getReturnType(), method.getReturnType(), SimpleObject.build("value")), instanceOf(SimpleObjectInterface.class));
		assertThat(((SimpleObjectInterface) convertResult(target.getReturnType(), method.getReturnType(), SimpleObject.build("value"))).getString(), equalTo("value"));
	}

	@Test
	public void testConvertArgumentsConvertingXRayedValues() throws Exception {
		Method method = staticSimpleObjectMethod();
		Method target = interfaceSimpleObjectMethod();
		SimpleObject val = SimpleObject.build("value");
		assertThat(convertArguments(target.getParameterTypes(), method.getParameterTypes(), ObjectAccess.xray(val).to(SimpleObjectInterface.class)), equalTo(new Object[] { val }));
	}

	private Method staticLongMethod() throws NoSuchMethodException {
		return ForSimpleObject.class.getDeclaredMethod("longMethod", long.class, String.class);
	}

	private Method interfaceLongMethod() throws NoSuchMethodException {
		return InterfaceForSimpleObject.class.getDeclaredMethod("longMethod", long.class, String.class);
	}

	private Method staticSimpleObjectMethod() throws NoSuchMethodException {
		return ForSimpleObject.class.getDeclaredMethod("simpleObjectMethod", SimpleObject.class);
	}

	private Method interfaceSimpleObjectMethod() throws NoSuchMethodException {
		return InterfaceForSimpleObject.class.getDeclaredMethod("simpleObjectMethod", SimpleObjectInterface.class);
	}

	private SimpleObjectInterface simpleObjectInterface(final String s) {
		return new SimpleObjectInterface() {

			@Override
			public String getString() {
				return s;
			}

			@Override
			public void setString(String s) {
			}
			
		};
	}

	@SuppressWarnings("unused")
	private static class ForSimpleObject {

		private Integer longMethod(long arg0, String arg1) {
			return (int) arg0 + Integer.parseInt(arg1);
		}

		private SimpleObject simpleObjectMethod(SimpleObject arg0) {
			return arg0;
		}

	}

	@SuppressWarnings("unused")
	private static class ForSimpleOther {

		private SimpleOtherObject simpleObjectMethod(SimpleOtherObject arg0) {
			return arg0;
		}

	}

	interface InterfaceForSimpleObject {
		Integer longMethod(long arg0, String arg1);

		SimpleObjectInterface simpleObjectMethod(SimpleObjectInterface arg0);
	}

	interface SimpleObjectInterface {
		String getString();
		void setString(String s);
	}

	@SuppressWarnings("unused")
	private static class SimpleObject {
		private String string;
		
		public static SimpleObject build(String s) {
			SimpleObject simpleObject = new SimpleObject();
			simpleObject.string = s;
			return simpleObject;
		}
		
		public String getString() {
			return string;
		}
		
		@Override
		public boolean equals(Object obj) {
			return ((SimpleObject) obj).string.equals(string);
		}
	}

	@SuppressWarnings("unused")
	private static class SimpleOtherObject {
		private String string;
		
		public SimpleOtherObject(String s) {
			this.string = s;
		}
		
		public String getString() {
			return string;
		}
		
		@Override
		public boolean equals(Object obj) {
			return ((SimpleOtherObject) obj).string.equals(string);
		}
	}

}
