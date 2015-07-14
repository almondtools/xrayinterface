package com.almondtools.xrayinterface;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.junit.Test;

import com.almondtools.xrayinterface.InvocationResolverTest.ConvertibleInterface;
import com.almondtools.xrayinterface.Convert;
import com.almondtools.xrayinterface.StaticInvocationResolver;

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
		assertThat(resolver.findField("bo", boolean.class, new Annotation[0]), notNullValue());
		assertThat(resolver.findField("st", String.class, new Annotation[0]), notNullValue());
		assertThat(resolver.findField("IN", int.class, new Annotation[0]), notNullValue());
	}

	@Test(expected = NoSuchFieldException.class)
	public void testFindFieldNonExisting() throws Exception {
		StaticInvocationResolver resolver = new StaticInvocationResolver(TestSubClass.class);
		resolver.findField("a", boolean.class, new Annotation[0]);
	}

	@Test(expected = NoSuchFieldException.class)
	public void testFindSuperFieldWronglyTyped() throws Exception {
		StaticInvocationResolver resolver = new StaticInvocationResolver(TestSubClass.class);
		resolver.findField("st", boolean.class, new Annotation[0]);
	}

	@Test(expected = NoSuchFieldException.class)
	public void testFindFieldWronglyTyped() throws Exception {
		StaticInvocationResolver resolver = new StaticInvocationResolver(TestSubClass.class);
		resolver.findField("bo", String.class, new Annotation[0]);
	}

	@Test
	public void testCreateSetterInvocator() throws Exception {
		StaticInvocationResolver resolver = new StaticInvocationResolver(TestSubClass.class);
		Method[] setters = Setters.class.getDeclaredMethods();
		assertThat(resolver.createSetterInvocator(setters[0]), notNullValue());
		assertThat(resolver.createSetterInvocator(setters[1]), notNullValue());
		assertThat(resolver.createSetterInvocator(setters[2]), notNullValue());
	}

	@Test
	public void testCreateSetterInvocatorConverted() throws Exception {
		StaticInvocationResolver resolver = new StaticInvocationResolver(ConvertibleTestClass.class);
		Method[] setters = ConvertibleSetters.class.getDeclaredMethods();
		assertThat(resolver.createSetterInvocator(setters[0]), notNullValue());
	}

	@Test(expected = NoSuchFieldException.class)
	public void testCreateSetterInvocatorFails() throws Exception {
		StaticInvocationResolver resolver = new StaticInvocationResolver(TestSubClass.class);
		Method[] setters = BadSetters.class.getDeclaredMethods();
		resolver.createSetterInvocator(setters[0]);
	}

	@Test
	public void testCreateGetterInvocator() throws Exception {
		StaticInvocationResolver resolver = new StaticInvocationResolver(TestSubClass.class);
		Method[] getters = Getters.class.getDeclaredMethods();
		assertThat(resolver.createGetterInvocator(getters[0]), notNullValue());
		assertThat(resolver.createGetterInvocator(getters[1]), notNullValue());
		assertThat(resolver.createGetterInvocator(getters[2]), notNullValue());
	}

	@Test
	public void testCreateGetterInvocatorConverted() throws Exception {
		StaticInvocationResolver resolver = new StaticInvocationResolver(ConvertibleTestClass.class);
		Method[] getters = ConvertibleGetters.class.getDeclaredMethods();
		assertThat(resolver.createGetterInvocator(getters[0]), notNullValue());
	}

	@Test(expected = NoSuchFieldException.class)
	public void testCreateGetterInvocatorFails() throws Exception {
		StaticInvocationResolver resolver = new StaticInvocationResolver(TestSubClass.class);
		Method[] getters = BadGetters.class.getDeclaredMethods();
		resolver.createGetterInvocator(getters[0]);
	}

	@Test
	public void testCreateMethodInvocator() throws Exception {
		StaticInvocationResolver resolver = new StaticInvocationResolver(TestSubClass.class);
		Method[] methods = Methods.class.getDeclaredMethods();
		assertThat(methods.length, equalTo(5));
		assertThat(resolver.createMethodInvocator(methods[0]), notNullValue());
		assertThat(resolver.createMethodInvocator(methods[1]), notNullValue());
		assertThat(resolver.createMethodInvocator(methods[2]), notNullValue());
		assertThat(resolver.createMethodInvocator(methods[3]), notNullValue());
		assertThat(resolver.createMethodInvocator(methods[4]), notNullValue());

	}

	@Test
	public void testCreateMethodInvocatorConverted() throws Exception {
		StaticInvocationResolver resolver = new StaticInvocationResolver(ConvertibleTestClass.class);
		Method[] methods = ConvertibleMethods.class.getDeclaredMethods();
		assertThat(methods.length, equalTo(2));
		assertThat(resolver.createMethodInvocator(methods[0]), notNullValue());
		assertThat(resolver.createMethodInvocator(methods[1]), notNullValue());
	}

	@Test(expected = NoSuchMethodException.class)
	public void testCreateMethodInvocatorNonExisting() throws Exception {
		StaticInvocationResolver resolver = new StaticInvocationResolver(TestSubClass.class);
		Method[] badmethods = BadMethods.class.getDeclaredMethods();
		resolver.createMethodInvocator(badmethods[0]);
	}

	@Test(expected = NoSuchMethodException.class)
	public void testCreateMethodInvocatorWronglySignature() throws Exception {
		StaticInvocationResolver resolver = new StaticInvocationResolver(TestSubClass.class);
		Method[] badmethods = BadMethods.class.getDeclaredMethods();
		resolver.createMethodInvocator(badmethods[1]);
	}

	@Test(expected = NoSuchMethodException.class)
	public void testCreateMethodInvocatorWronglyTyped() throws Exception {
		StaticInvocationResolver resolver = new StaticInvocationResolver(TestSubClass.class);
		Method[] badmethods = BadMethods.class.getDeclaredMethods();
		resolver.createMethodInvocator(badmethods[2]);
	}

	@Test(expected = NoSuchMethodException.class)
	public void testCreateMethodInvocatorWronglyExceptionTyped() throws Exception {
		StaticInvocationResolver resolver = new StaticInvocationResolver(TestSubClass.class);
		Method[] badmethods = BadMethods.class.getDeclaredMethods();
		resolver.createMethodInvocator(badmethods[3]);
	}

	@Test(expected = NoSuchMethodException.class)
	public void testCreateMethodInvocatorWronglyExceptionTypedInSuperclass() throws Exception {
		StaticInvocationResolver resolver = new StaticInvocationResolver(TestSubClass.class);
		Method[] badmethods = BadMethods.class.getDeclaredMethods();
		resolver.createMethodInvocator(badmethods[4]);
	}

	@Test(expected = NoSuchMethodException.class)
	public void testCreateMethodInvocatorWrongResultType() throws Exception {
		StaticInvocationResolver resolver = new StaticInvocationResolver(TestSubClass.class);
		Method[] badmethods = BadMethods.class.getDeclaredMethods();
		resolver.createMethodInvocator(badmethods[5]);
	}

	@Test(expected = NoSuchMethodException.class)
	public void testCreateMethodInvocatorNotConvertibleArguments() throws Exception {
		StaticInvocationResolver resolver = new StaticInvocationResolver(ConvertibleTestClass.class);
		Method[] badmethods = NonConvertibleMethods.class.getDeclaredMethods();
		resolver.createMethodInvocator(badmethods[0]);
	}

	@Test(expected = NoSuchMethodException.class)
	public void testCreateMethodInvocatorNotConvertibleResult() throws Exception {
		StaticInvocationResolver resolver = new StaticInvocationResolver(ConvertibleTestClass.class);
		Method[] badmethods = NonConvertibleMethods.class.getDeclaredMethods();
		resolver.createMethodInvocator(badmethods[1]);
	}

	@Test(expected = NoSuchMethodException.class)
	public void testCreateMethodInvocatorFailedConvertibleArguments() throws Exception {
		StaticInvocationResolver resolver = new StaticInvocationResolver(ConvertibleTestClass.class);
		Method[] badmethods = FailedConvertibleMethods.class.getDeclaredMethods();
		resolver.createMethodInvocator(badmethods[0]);
	}

	@Test(expected = NoSuchMethodException.class)
	public void testCreateMethodInvocatorFailedConvertibleResult() throws Exception {
		StaticInvocationResolver resolver = new StaticInvocationResolver(ConvertibleTestClass.class);
		Method[] badmethods = FailedConvertibleMethods.class.getDeclaredMethods();
		resolver.createMethodInvocator(badmethods[1]);
	}

	@Test
	public void testCreateConstructorInvocator() throws Exception {
		StaticInvocationResolver resolver = new StaticInvocationResolver(TestSubClass.class);
		Method[] constructors = Constructors.class.getDeclaredMethods();
		assertThat(constructors.length, equalTo(3));
		assertThat(resolver.createConstructorInvocator(constructors[0]), notNullValue());
		assertThat(resolver.createConstructorInvocator(constructors[1]), notNullValue());
		assertThat(resolver.createConstructorInvocator(constructors[2]), notNullValue());
	}

	@Test
	public void testCreateConstructorInvocatorConverted() throws Exception {
		StaticInvocationResolver resolver = new StaticInvocationResolver(ConvertibleTestClass.class);
		Method[] constructors = ConvertibleConstructors.class.getDeclaredMethods();
		assertThat(constructors.length, equalTo(2));
		assertThat(resolver.createConstructorInvocator(constructors[0]), notNullValue());
		assertThat(resolver.createConstructorInvocator(constructors[1]), notNullValue());
	}

	@Test(expected = NoSuchMethodException.class)
	public void testCreateConstructorInvocatorConversionFails() throws Exception {
		StaticInvocationResolver resolver = new StaticInvocationResolver(ConvertibleTestClass.class);
		Method[] constructors = NonConvertibleConstructors.class.getDeclaredMethods();
		resolver.createConstructorInvocator(constructors[0]);
	}

	@Test(expected = NoSuchMethodException.class)
	public void testCreateConstructorInvocatorWronglySignature() throws Exception {
		StaticInvocationResolver resolver = new StaticInvocationResolver(TestSubClass.class);
		Method[] constructors = BadConstructors.class.getDeclaredMethods();
		resolver.createConstructorInvocator(constructors[0]);
	}

	@Test(expected = NoSuchMethodException.class)
	public void testCreateConstructorInvocatorWronglyTyped() throws Exception {
		StaticInvocationResolver resolver = new StaticInvocationResolver(TestSubClass.class);
		Method[] constructors = BadConstructors.class.getDeclaredMethods();
		resolver.createConstructorInvocator(constructors[1]);
	}

	@Test(expected = NoSuchMethodException.class)
	public void testCreateConstructorInvocatorWronglyExceptionTyped() throws Exception {
		StaticInvocationResolver resolver = new StaticInvocationResolver(TestSubClass.class);
		Method[] constructors = BadConstructors.class.getDeclaredMethods();
		resolver.createConstructorInvocator(constructors[2]);
	}

	interface Getters {
		String getSt();

		int getIN();

		boolean getBo();
	}

	interface Setters {
		void setSt(String s);

		void setIN(int i);

		void setBo(boolean b);
	}

	interface ConvertibleSetters {
		void setConvertible(@Convert("ConvertibleObject") ConvertibleInterface o);
	}

	interface ConvertibleGetters {
		@Convert("ConvertibleObject")
		ConvertibleInterface getConvertible();
	}

	interface BadGetters {
		String getA();
	}

	interface BadSetters {
		void setA(boolean b);
	}

	interface Methods {
		void methoda();

		void methodb(String s);

		void methodc(String s) throws Exception;

		String methodd(String s) throws Exception;

		String methode(String s) throws Exception;
	}

	interface BadMethods {
		String methodz();

		void methodb();

		void methodb(int i);

		void methodb(String s) throws Exception;

		String methode(String s) throws IOException;

		String methoda();
	}

	interface ConvertibleMethods {

		void methoda(@Convert("ConvertibleObject") ConvertibleInterface o);

		@Convert("ConvertibleObject")
		ConvertibleInterface methodb();
	}

	interface NonConvertibleMethods {

		void methoda(ConvertibleInterface o);

		ConvertibleInterface methodb();
	}

	interface FailedConvertibleMethods {

		void methoda(@Convert ConvertibleInterface o);

		@Convert
		ConvertibleInterface methodb();
	}

	interface Constructors {
		TestSubClass create();

		TestSubClass create(String s);

		TestSubClass create(int i) throws Exception;
	}

	interface BadConstructors {
		TestSubClass create(String s, boolean b);

		TestSubClass create(boolean b);

		TestSubClass create(int i) throws IOException;
	}

	interface ConvertibleConstructors {
		@Convert("ConvertibleTestClass") ConvertibleInterface create();

		@Convert("ConvertibleTestClass") ConvertibleInterface create(@Convert("ConvertibleTestClass") ConvertibleInterface o);
	}

	interface NonConvertibleConstructors {
		@Convert ConvertibleInterface create(@Convert ConvertibleInterface i);
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
