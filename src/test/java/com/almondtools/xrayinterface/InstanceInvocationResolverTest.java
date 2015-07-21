package com.almondtools.xrayinterface;

import static com.almondtools.xrayinterface.ConvertedType.converted;
import static com.almondtools.xrayinterface.FixedType.VOID;
import static com.almondtools.xrayinterface.FixedType.fixed;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.stream.Stream;

import org.junit.Test;

import com.almondtools.xrayinterface.Convert;
import com.almondtools.xrayinterface.InstanceInvocationResolver;

@SuppressWarnings("unused")
public class InstanceInvocationResolverTest {

	@Test
	public void testFindField() throws Exception {
		InstanceInvocationResolver resolver = new InstanceInvocationResolver(TestSubClass.class);
		assertThat(resolver.findField("b", fixed(boolean.class)), notNullValue());
		assertThat(resolver.findField("s", fixed(String.class)), notNullValue());
		assertThat(resolver.findField("i", fixed(int.class)), notNullValue());
	}

	@Test
	public void testCreateMethodInvocatorGetterInvocator() throws Exception {
		InstanceInvocationResolver resolver = new InstanceInvocationResolver(TestSubClass.class);
		assertThat(resolver.createGetterInvocator("s", fixed(String.class)), notNullValue());
		assertThat(resolver.createGetterInvocator("i", fixed(int.class)), notNullValue());
		assertThat(resolver.createGetterInvocator("b", fixed(boolean.class)), notNullValue());
	}

	@Test
	public void testCreateMethodInvocatorGetterInvocatorConverted() throws Exception {
		InstanceInvocationResolver resolver = new InstanceInvocationResolver(ConvertibleTestClass.class);
		assertThat(resolver.createGetterInvocator("convertible", converted(ConvertibleObject.class, ConvertibleInterface.class)), notNullValue());
	}

	@Test
	public void testCreateMethodInvocatorSetterInvocator() throws Exception {
		InstanceInvocationResolver resolver = new InstanceInvocationResolver(TestSubClass.class);
		assertThat(resolver.createSetterInvocator("s", fixed(String.class)), notNullValue());
		assertThat(resolver.createSetterInvocator("i", fixed(int.class)), notNullValue());
		assertThat(resolver.createSetterInvocator("b", fixed(boolean.class)), notNullValue());
	}

	@Test
	public void testCreateMethodInvocatorSetterInvocatorConverted() throws Exception {
		InstanceInvocationResolver resolver = new InstanceInvocationResolver(ConvertibleTestClass.class);
		assertThat(resolver.createSetterInvocator("convertible", converted(ConvertibleObject.class, ConvertibleInterface.class)), notNullValue());
	}

	@Test(expected = NoSuchFieldException.class)
	public void testCreateMethodInvocatorGetterInvocatorFails() throws Exception {
		InstanceInvocationResolver resolver = new InstanceInvocationResolver(TestSubClass.class);
		resolver.createGetterInvocator("a", fixed(boolean.class));
	}

	@Test(expected = NoSuchFieldException.class)
	public void testCreateMethodInvocatorSetterInvocatorFails() throws Exception {
		InstanceInvocationResolver resolver = new InstanceInvocationResolver(TestSubClass.class);
		resolver.createSetterInvocator("a", fixed(boolean.class));
	}

	@Test(expected = NoSuchFieldException.class)
	public void testFindFieldNonExisting() throws Exception {
		InstanceInvocationResolver resolver = new InstanceInvocationResolver(TestSubClass.class);
		assertThat(resolver.findField("a", fixed(boolean.class)), notNullValue());
	}

	@Test(expected = NoSuchFieldException.class)
	public void testFindSuperFieldWronglyTyped() throws Exception {
		InstanceInvocationResolver resolver = new InstanceInvocationResolver(TestSubClass.class);
		assertThat(resolver.findField("s", fixed(boolean.class)), notNullValue());
	}

	@Test(expected = NoSuchFieldException.class)
	public void testFindFieldWronglyTyped() throws Exception {
		InstanceInvocationResolver resolver = new InstanceInvocationResolver(TestSubClass.class);
		assertThat(resolver.findField("b", fixed(String.class)), notNullValue());
	}

	@Test
	public void testCreateMethodInvocatorMethodInvocatorWithoutConversion() throws Exception {
		InstanceInvocationResolver resolver = new InstanceInvocationResolver(TestSubClass.class);

		assertThat(resolver.createMethodInvocator("methoda", VOID, types(), types()), notNullValue());
		assertThat(resolver.createMethodInvocator("methodb", VOID, types(String.class), types()), notNullValue());
		assertThat(resolver.createMethodInvocator("methodc", VOID, types(String.class), types(Exception.class)), notNullValue());
		assertThat(resolver.createMethodInvocator("methodd", fixed(String.class), types(String.class), types(Exception.class)), notNullValue());
		assertThat(resolver.createMethodInvocator("methode", fixed(String.class), types(String.class), types(Exception.class)), notNullValue());
	}

	@Test
	public void testCreateMethodInvocatorMethodInvocatorWithConversion() throws Exception {
		InstanceInvocationResolver resolver = new InstanceInvocationResolver(ConvertibleTestClass.class);
		
		assertThat(resolver.createMethodInvocator("methoda", VOID, types(converted(ConvertibleObject.class,ConvertibleInterface.class)), types()), notNullValue());
		assertThat(resolver.createMethodInvocator("methodb", converted(ConvertibleObject.class, ConvertibleInterface.class), types(), types()), notNullValue());
	}

	@Test(expected = NoSuchMethodException.class)
	public void testCreateMethodInvocatorMethodInvocatorNonExisting() throws Exception {
		InstanceInvocationResolver resolver = new InstanceInvocationResolver(TestSubClass.class);
		resolver.createMethodInvocator("methodz", fixed(String.class), types(), types());
	}

	@Test(expected = NoSuchMethodException.class)
	public void testCreateMethodInvocatorMethodInvocatorWronglyReturnType() throws Exception {
		InstanceInvocationResolver resolver = new InstanceInvocationResolver(TestSubClass.class);
		resolver.createMethodInvocator("methoda", fixed(String.class), types(), types());
	}

	@Test(expected = NoSuchMethodException.class)
	public void testCreateMethodInvocatorMethodInvocatorWronglySignature() throws Exception {
		InstanceInvocationResolver resolver = new InstanceInvocationResolver(TestSubClass.class);
		resolver.createMethodInvocator("methodb", VOID, types(), types());
	}

	@Test(expected = NoSuchMethodException.class)
	public void testCreateMethodInvocatorMethodInvocatorWronglyTyped() throws Exception {
		InstanceInvocationResolver resolver = new InstanceInvocationResolver(TestSubClass.class);
		resolver.createMethodInvocator("methodb", VOID, types(int.class), types());
	}

	@Test(expected = NoSuchMethodException.class)
	public void testCreateMethodInvocatorMethodInvocatorWronglyExceptionTyped() throws Exception {
		InstanceInvocationResolver resolver = new InstanceInvocationResolver(TestSubClass.class);
		resolver.createMethodInvocator("methodb", VOID, types(String.class), types(Exception.class));
	}

	@Test(expected = NoSuchMethodException.class)
	public void testCreateMethodInvocatorMethodInvocatorWronglyExceptionTypedInSuperclass() throws Exception {
		InstanceInvocationResolver resolver = new InstanceInvocationResolver(TestSubClass.class);
		resolver.createMethodInvocator("methode", fixed(String.class), types(String.class), types(IOException.class));
	}

	@Test(expected = NoSuchMethodException.class)
	public void testCreateMethodInvocatorMethodInvocatorNotConvertibleArguments() throws Exception {
		InstanceInvocationResolver resolver = new InstanceInvocationResolver(ConvertibleTestClass.class);
		resolver.createMethodInvocator("methoda", VOID, types(ConvertibleInterface.class), types());
	}

	@Test(expected = NoSuchMethodException.class)
	public void testCreateMethodInvocatorMethodInvocatorNotConvertibleResult() throws Exception {
		InstanceInvocationResolver resolver = new InstanceInvocationResolver(ConvertibleTestClass.class);
		resolver.createMethodInvocator("methodb", fixed(ConvertibleInterface.class), types(), types());
	}

	@Test(expected = NoSuchMethodException.class)
	public void testCreateMethodInvocatorMethodInvocatorFailedConvertibleArguments() throws Exception {
		InstanceInvocationResolver resolver = new InstanceInvocationResolver(ConvertibleTestClass.class);
		resolver.createMethodInvocator("methoda", VOID, types(converted(ConvertibleInterface.class, ConvertibleInterface.class)), types());
	}

	@Test(expected = NoSuchMethodException.class)
	public void testCreateMethodInvocatorMethodInvocatorFailedConvertibleResult() throws Exception {
		InstanceInvocationResolver resolver = new InstanceInvocationResolver(ConvertibleTestClass.class);
		resolver.createMethodInvocator("methodb", converted(ConvertibleInterface.class, ConvertibleInterface.class), types(), types());
	}

	private Type[] types(Object... types) {
		return Stream.of(types)
			.map(type -> {
				if (type instanceof Type) {
					return (Type) type;
				} else if (type instanceof Class<?>) {
					return FixedType.fixed((Class<?>) type);
				} else {
					return (Type) null;
				}
			})
			.toArray(l -> new Type[l]);
	}
	
	interface ConvertibleInterface {
		String getContent();
	}

	private static class TestClass {
		private String s;
		private int i;

		private String methode(String b) throws Exception {
			return null;
		}
	}

	private static class TestSubClass extends TestClass {
		private boolean b;

		private String method0() {
			return null;
		}

		private void methoda() {
		}

		private void methodb(String b) {
		}

		private void methodc(String b) throws Exception {
		}

		private String methodd(String b) throws Exception {
			return null;
		}
	}

	private static class ConvertibleTestClass {

		private ConvertibleObject convertible;

		private void methoda(ConvertibleObject o) {
		}

		private ConvertibleObject methodb() {
			return null;
		}
	}

	private static class ConvertibleObject {
		private String content = "content";
	}

}
