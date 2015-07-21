package com.almondtools.xrayinterface;

import static com.almondtools.xrayinterface.ConvertedType.converted;
import static com.almondtools.xrayinterface.FixedType.VOID;
import static com.almondtools.xrayinterface.FixedType.fixed;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.stream.Stream;

import org.junit.Test;

import com.almondtools.xrayinterface.InstanceInvocationResolverTest.ConvertibleInterface;

@SuppressWarnings("unused")
public class StaticInvocationResolverTest {

	@Test
	public void testGetType() throws Exception {
		StaticInvocationResolver resolver = new StaticInvocationResolver(TestSubClass.class);
		assertThat(resolver.getType(), sameInstance((Object) TestSubClass.class));
	}

	@Test
	public void testFindField() throws Exception {
		StaticInvocationResolver resolver = new StaticInvocationResolver(TestSubClass.class);
		assertThat(resolver.findField("bo", fixed(boolean.class)), notNullValue());
		assertThat(resolver.findField("st", fixed(String.class)), notNullValue());
		assertThat(resolver.findField("IN", fixed(int.class)), notNullValue());
	}

	@Test(expected = NoSuchFieldException.class)
	public void testFindFieldNonExisting() throws Exception {
		StaticInvocationResolver resolver = new StaticInvocationResolver(TestSubClass.class);
		resolver.findField("a", fixed(boolean.class));
	}

	@Test(expected = NoSuchFieldException.class)
	public void testFindSuperFieldWronglyTyped() throws Exception {
		StaticInvocationResolver resolver = new StaticInvocationResolver(TestSubClass.class);
		resolver.findField("st", fixed(boolean.class));
	}

	@Test(expected = NoSuchFieldException.class)
	public void testFindFieldWronglyTyped() throws Exception {
		StaticInvocationResolver resolver = new StaticInvocationResolver(TestSubClass.class);
		resolver.findField("bo", fixed(String.class));
	}

	@Test
	public void testCreateSetterInvocator() throws Exception {
		StaticInvocationResolver resolver = new StaticInvocationResolver(TestSubClass.class);
		assertThat(resolver.createSetterInvocator("st", fixed(String.class)), notNullValue());
		assertThat(resolver.createSetterInvocator("IN", fixed(int.class)), notNullValue());
		assertThat(resolver.createSetterInvocator("bo", fixed(boolean.class)), notNullValue());
	}

	@Test
	public void testCreateSetterInvocatorConverted() throws Exception {
		StaticInvocationResolver resolver = new StaticInvocationResolver(ConvertibleTestClass.class);
		assertThat(resolver.createSetterInvocator("convertible", ConvertedType.converted(ConvertibleObject.class, ConvertibleInterface.class)), notNullValue());
	}

	@Test(expected = NoSuchFieldException.class)
	public void testCreateSetterInvocatorFails() throws Exception {
		StaticInvocationResolver resolver = new StaticInvocationResolver(TestSubClass.class);
		resolver.createSetterInvocator("a", fixed(boolean.class));
	}

	@Test
	public void testCreateGetterInvocator() throws Exception {
		StaticInvocationResolver resolver = new StaticInvocationResolver(TestSubClass.class);
		assertThat(resolver.createGetterInvocator("st", fixed(String.class)), notNullValue());
		assertThat(resolver.createGetterInvocator("IN", fixed(int.class)), notNullValue());
		assertThat(resolver.createGetterInvocator("bo", fixed(boolean.class)), notNullValue());
	}

	@Test
	public void testCreateGetterInvocatorConverted() throws Exception {
		StaticInvocationResolver resolver = new StaticInvocationResolver(ConvertibleTestClass.class);
		assertThat(resolver.createGetterInvocator("convertible", converted(ConvertibleObject.class, ConvertibleInterface.class)), notNullValue());
	}

	@Test(expected = NoSuchFieldException.class)
	public void testCreateGetterInvocatorFails() throws Exception {
		StaticInvocationResolver resolver = new StaticInvocationResolver(TestSubClass.class);
		resolver.createGetterInvocator("a", fixed(String.class));
	}

	@Test
	public void testCreateMethodInvocator() throws Exception {
		StaticInvocationResolver resolver = new StaticInvocationResolver(TestSubClass.class);
		assertThat(resolver.createMethodInvocator("methoda", VOID, types(), types()), notNullValue());
		assertThat(resolver.createMethodInvocator("methodb", VOID, types(String.class), types()), notNullValue());
		assertThat(resolver.createMethodInvocator("methodc", VOID, types(String.class), types(Exception.class)), notNullValue());
		assertThat(resolver.createMethodInvocator("methodd", fixed(String.class), types(String.class), types(Exception.class)), notNullValue());
		assertThat(resolver.createMethodInvocator("methode", fixed(String.class), types(String.class), types(Exception.class)), notNullValue());
	}

	@Test
	public void testCreateMethodInvocatorConverted() throws Exception {
		StaticInvocationResolver resolver = new StaticInvocationResolver(ConvertibleTestClass.class);
		assertThat(resolver.createMethodInvocator("methoda", VOID, types(converted(ConvertibleObject.class, ConvertibleInterface.class)), types()), notNullValue());
		assertThat(resolver.createMethodInvocator("methodb", converted(ConvertibleObject.class, ConvertibleInterface.class), types(), types()), notNullValue());
	}

	@Test(expected = NoSuchMethodException.class)
	public void testCreateMethodInvocatorNonExisting() throws Exception {
		StaticInvocationResolver resolver = new StaticInvocationResolver(TestSubClass.class);
		resolver.createMethodInvocator("methodz", fixed(String.class), types(), types());
	}

	@Test(expected = NoSuchMethodException.class)
	public void testCreateMethodInvocatorWronglySignature() throws Exception {
		StaticInvocationResolver resolver = new StaticInvocationResolver(TestSubClass.class);
		resolver.createMethodInvocator("methodb", VOID, types(), types());
	}

	@Test(expected = NoSuchMethodException.class)
	public void testCreateMethodInvocatorWronglyTyped() throws Exception {
		StaticInvocationResolver resolver = new StaticInvocationResolver(TestSubClass.class);
		resolver.createMethodInvocator("methodb", VOID, types(int.class), types());
	}

	@Test(expected = NoSuchMethodException.class)
	public void testCreateMethodInvocatorWronglyExceptionTyped() throws Exception {
		StaticInvocationResolver resolver = new StaticInvocationResolver(TestSubClass.class);
		resolver.createMethodInvocator("methodb", VOID, types(String.class), types(Exception.class));
	}

	@Test(expected = NoSuchMethodException.class)
	public void testCreateMethodInvocatorWronglyExceptionTypedInSuperclass() throws Exception {
		StaticInvocationResolver resolver = new StaticInvocationResolver(TestSubClass.class);
		resolver.createMethodInvocator("methode", fixed(String.class), types(String.class), types(IOException.class));
	}

	@Test(expected = NoSuchMethodException.class)
	public void testCreateMethodInvocatorWrongResultType() throws Exception {
		StaticInvocationResolver resolver = new StaticInvocationResolver(TestSubClass.class);
		resolver.createMethodInvocator("methoda", fixed(String.class), types(), types());
	}

	@Test(expected = NoSuchMethodException.class)
	public void testCreateMethodInvocatorNotConvertibleArguments() throws Exception {
		StaticInvocationResolver resolver = new StaticInvocationResolver(ConvertibleTestClass.class);
		resolver.createMethodInvocator("methoda", VOID, types(ConvertibleInterface.class), types());
	}

	@Test(expected = NoSuchMethodException.class)
	public void testCreateMethodInvocatorNotConvertibleResult() throws Exception {
		StaticInvocationResolver resolver = new StaticInvocationResolver(ConvertibleTestClass.class);
		resolver.createMethodInvocator("methodb", fixed(ConvertibleInterface.class), types(), types());
	}

	@Test(expected = NoSuchMethodException.class)
	public void testCreateMethodInvocatorFailedConvertibleArguments() throws Exception {
		StaticInvocationResolver resolver = new StaticInvocationResolver(ConvertibleTestClass.class);
		resolver.createMethodInvocator("methoda", VOID, types(converted(ConvertibleInterface.class, ConvertibleInterface.class)), types());
	}

	@Test(expected = NoSuchMethodException.class)
	public void testCreateMethodInvocatorFailedConvertibleResult() throws Exception {
		StaticInvocationResolver resolver = new StaticInvocationResolver(ConvertibleTestClass.class);
		resolver.createMethodInvocator("methodb", converted(ConvertibleInterface.class, ConvertibleInterface.class), types(), types());
	}

	@Test
	public void testCreateConstructorInvocator() throws Exception {
		StaticInvocationResolver resolver = new StaticInvocationResolver(TestSubClass.class);
		assertThat(resolver.createConstructorInvocator(fixed(TestSubClass.class), types(), types()), notNullValue());
		assertThat(resolver.createConstructorInvocator(fixed(TestSubClass.class), types(String.class), types()), notNullValue());
		assertThat(resolver.createConstructorInvocator(fixed(TestSubClass.class), types(int.class), types(Exception.class)), notNullValue());
	}

	@Test
	public void testCreateConstructorInvocatorConverted() throws Exception {
		StaticInvocationResolver resolver = new StaticInvocationResolver(ConvertibleTestClass.class);
		assertThat(resolver.createConstructorInvocator(converted(ConvertibleTestClass.class, ConvertibleInterface.class), types(), types()), notNullValue());
		assertThat(resolver.createConstructorInvocator(converted(ConvertibleTestClass.class, ConvertibleInterface.class), types(converted(ConvertibleTestClass.class, ConvertibleInterface.class)), types()), notNullValue());
	}

	@Test(expected = NoSuchMethodException.class)
	public void testCreateConstructorInvocatorConversionFails() throws Exception {
		StaticInvocationResolver resolver = new StaticInvocationResolver(ConvertibleTestClass.class);
		resolver.createConstructorInvocator(converted(ConvertibleInterface.class, ConvertibleInterface.class), types(converted(ConvertibleInterface.class, ConvertibleInterface.class)), types());
	}

	@Test(expected = NoSuchMethodException.class)
	public void testCreateConstructorInvocatorWronglySignature() throws Exception {
		StaticInvocationResolver resolver = new StaticInvocationResolver(TestSubClass.class);
		resolver.createConstructorInvocator(fixed(TestSubClass.class), types(String.class, boolean.class), types());
	}

	@Test(expected = NoSuchMethodException.class)
	public void testCreateConstructorInvocatorWronglyTyped() throws Exception {
		StaticInvocationResolver resolver = new StaticInvocationResolver(TestSubClass.class);
		resolver.createConstructorInvocator(fixed(TestSubClass.class), types(boolean.class), types());
	}

	@Test(expected = NoSuchMethodException.class)
	public void testCreateConstructorInvocatorWronglyExceptionTyped() throws Exception {
		StaticInvocationResolver resolver = new StaticInvocationResolver(TestSubClass.class);
		resolver.createConstructorInvocator(fixed(TestSubClass.class), types(int.class), types(IOException.class));
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
	
	private static class TestClass {
		private static String st;
		private static final int IN = 0;

		private static String methode(String b) throws Exception {
			return null;
		}
	}

	private static class TestSubClass extends TestClass {
		private static boolean bo;

		private TestSubClass() {
		}

		private TestSubClass(String s) {
		}

		private TestSubClass(int i) throws Exception {
		}

		private static void methoda() {
		}

		private static void methodb(String b) {
		}

		private static void methodc(String b) throws Exception {
		}

		private static String methodd(String b) throws Exception {
			return null;
		}
	}

	private static class ConvertibleTestClass {

		public ConvertibleTestClass() {
		}
		
		public ConvertibleTestClass(ConvertibleTestClass c) {
		}
		
		private static ConvertibleObject convertible;

		private static void methoda(ConvertibleObject o) {
		}

		private static ConvertibleObject methodb() {
			return null;
		}
	}

	private static class ConvertibleObject {
		private String content = "content";
	}

}
