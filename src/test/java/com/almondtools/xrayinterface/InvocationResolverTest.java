package com.almondtools.xrayinterface;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.junit.Test;

import com.almondtools.xrayinterface.Convert;
import com.almondtools.xrayinterface.InvocationResolver;

@SuppressWarnings("unused")
public class InvocationResolverTest {

	@Test
	public void testFindField() throws Exception {
		InvocationResolver resolver = new InvocationResolver(TestSubClass.class);
		assertThat(resolver.findField("b", boolean.class, new Annotation[0]), notNullValue());
		assertThat(resolver.findField("s", String.class, new Annotation[0]), notNullValue());
		assertThat(resolver.findField("i", int.class, new Annotation[0]), notNullValue());
	}

	@Test
	public void testCreateGetterInvocator() throws Exception {
		InvocationResolver resolver = new InvocationResolver(TestSubClass.class);
		Method[] getters = Getters.class.getDeclaredMethods();
		assertThat(resolver.createGetterInvocator(getters[0]), notNullValue());
		assertThat(resolver.createGetterInvocator(getters[1]), notNullValue());
		assertThat(resolver.createGetterInvocator(getters[2]), notNullValue());
	}

	@Test
	public void testCreateGetterInvocatorConverted() throws Exception {
		InvocationResolver resolver = new InvocationResolver(ConvertibleTestClass.class);
		Method[] getters = ConvertibleGetters.class.getDeclaredMethods();
		assertThat(resolver.createGetterInvocator(getters[0]), notNullValue());
	}

	@Test
	public void testCreateSetterInvocator() throws Exception {
		InvocationResolver resolver = new InvocationResolver(TestSubClass.class);
		Method[] setters = Setters.class.getDeclaredMethods();
		assertThat(resolver.createSetterInvocator(setters[0]), notNullValue());
		assertThat(resolver.createSetterInvocator(setters[1]), notNullValue());
		assertThat(resolver.createSetterInvocator(setters[2]), notNullValue());
	}

	@Test
	public void testCreateSetterInvocatorConverted() throws Exception {
		InvocationResolver resolver = new InvocationResolver(ConvertibleTestClass.class);
		Method[] convsetters = ConvertibleSetters.class.getDeclaredMethods();
		assertThat(resolver.createSetterInvocator(convsetters[0]), notNullValue());
	}

	@Test(expected = NoSuchFieldException.class)
	public void testCreateGetterInvocatorFails() throws Exception {
		InvocationResolver resolver = new InvocationResolver(TestSubClass.class);
		Method[] getters = BadGetters.class.getDeclaredMethods();
		resolver.createGetterInvocator(getters[0]);
	}

	@Test(expected = NoSuchFieldException.class)
	public void testCreateSetterInvocatorFails() throws Exception {
		InvocationResolver resolver = new InvocationResolver(TestSubClass.class);
		Method[] setters = BadSetters.class.getDeclaredMethods();
		resolver.createSetterInvocator(setters[0]);
	}

	@Test(expected = NoSuchFieldException.class)
	public void testFindFieldNonExisting() throws Exception {
		InvocationResolver resolver = new InvocationResolver(TestSubClass.class);
		assertThat(resolver.findField("a", boolean.class, new Annotation[0]), notNullValue());
	}

	@Test(expected = NoSuchFieldException.class)
	public void testFindSuperFieldWronglyTyped() throws Exception {
		InvocationResolver resolver = new InvocationResolver(TestSubClass.class);
		assertThat(resolver.findField("s", boolean.class, new Annotation[0]), notNullValue());
	}

	@Test(expected = NoSuchFieldException.class)
	public void testFindFieldWronglyTyped() throws Exception {
		InvocationResolver resolver = new InvocationResolver(TestSubClass.class);
		assertThat(resolver.findField("b", String.class, new Annotation[0]), notNullValue());
	}

	@Test
	public void testCreateMethodInvocatorWithoutConversion() throws Exception {
		InvocationResolver resolver = new InvocationResolver(TestSubClass.class);
		Method[] methods = Methods.class.getDeclaredMethods();
		assertThat(methods.length, equalTo(5));
		assertThat(resolver.createMethodInvocator(methods[0]), notNullValue());
		assertThat(resolver.createMethodInvocator(methods[1]), notNullValue());
		assertThat(resolver.createMethodInvocator(methods[2]), notNullValue());
		assertThat(resolver.createMethodInvocator(methods[3]), notNullValue());
		assertThat(resolver.createMethodInvocator(methods[4]), notNullValue());
	}

	@Test
	public void testCreateMethodInvocatorWithConversion() throws Exception {
		InvocationResolver resolver = new InvocationResolver(ConvertibleTestClass.class);
		Method[] methods = ConvertibleMethods.class.getDeclaredMethods();
		assertThat(methods.length, equalTo(2));
		assertThat(resolver.createMethodInvocator(methods[0]), notNullValue());
		assertThat(resolver.createMethodInvocator(methods[1]), notNullValue());
	}

	@Test(expected = NoSuchMethodException.class)
	public void testCreateMethodInvocatorNonExisting() throws Exception {
		InvocationResolver resolver = new InvocationResolver(TestSubClass.class);
		Method[] badmethods = BadMethods.class.getDeclaredMethods();
		resolver.createMethodInvocator(badmethods[0]);
	}

	@Test(expected = NoSuchMethodException.class)
	public void testCreateMethodInvocatorWronglyReturnType() throws Exception {
		InvocationResolver resolver = new InvocationResolver(TestSubClass.class);
		Method[] badmethods = BadMethods.class.getDeclaredMethods();
		resolver.createMethodInvocator(badmethods[1]);
	}

	@Test(expected = NoSuchMethodException.class)
	public void testCreateMethodInvocatorWronglySignature() throws Exception {
		InvocationResolver resolver = new InvocationResolver(TestSubClass.class);
		Method[] badmethods = BadMethods.class.getDeclaredMethods();
		resolver.createMethodInvocator(badmethods[2]);
	}

	@Test(expected = NoSuchMethodException.class)
	public void testCreateMethodInvocatorWronglyTyped() throws Exception {
		InvocationResolver resolver = new InvocationResolver(TestSubClass.class);
		Method[] badmethods = BadMethods.class.getDeclaredMethods();
		resolver.createMethodInvocator(badmethods[3]);
	}

	@Test(expected = NoSuchMethodException.class)
	public void testCreateMethodInvocatorWronglyExceptionTyped() throws Exception {
		InvocationResolver resolver = new InvocationResolver(TestSubClass.class);
		Method[] badmethods = BadMethods.class.getDeclaredMethods();
		resolver.createMethodInvocator(badmethods[4]);
	}

	@Test(expected = NoSuchMethodException.class)
	public void testCreateMethodInvocatorWronglyExceptionTypedInSuperclass() throws Exception {
		InvocationResolver resolver = new InvocationResolver(TestSubClass.class);
		Method[] badmethods = BadMethods.class.getDeclaredMethods();
		resolver.createMethodInvocator(badmethods[5]);
	}

	@Test(expected = NoSuchMethodException.class)
	public void testCreateMethodInvocatorNotConvertibleArguments() throws Exception {
		InvocationResolver resolver = new InvocationResolver(ConvertibleTestClass.class);
		Method[] badmethods = NonConvertibleMethods.class.getDeclaredMethods();
		resolver.createMethodInvocator(badmethods[0]);
	}

	@Test(expected = NoSuchMethodException.class)
	public void testCreateMethodInvocatorNotConvertibleResult() throws Exception {
		InvocationResolver resolver = new InvocationResolver(ConvertibleTestClass.class);
		Method[] badmethods = NonConvertibleMethods.class.getDeclaredMethods();
		resolver.createMethodInvocator(badmethods[1]);
	}

	@Test(expected = NoSuchMethodException.class)
	public void testCreateMethodInvocatorFailedConvertibleArguments() throws Exception {
		InvocationResolver resolver = new InvocationResolver(ConvertibleTestClass.class);
		Method[] badmethods = FailedConvertibleMethods.class.getDeclaredMethods();
		resolver.createMethodInvocator(badmethods[0]);
	}

	@Test(expected = NoSuchMethodException.class)
	public void testCreateMethodInvocatorFailedConvertibleResult() throws Exception {
		InvocationResolver resolver = new InvocationResolver(ConvertibleTestClass.class);
		Method[] badmethods = FailedConvertibleMethods.class.getDeclaredMethods();
		resolver.createMethodInvocator(badmethods[1]);
	}

	interface Getters {
		String getS();

		int getI();

		boolean getB();
	}

	interface Setters {
		void setS(String s);

		void setI(int i);

		void setB(boolean b);
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

		String methoda();

		void methodb();

		void methodb(int i);

		void methodb(String s) throws Exception;

		String methode(String s) throws IOException;
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
