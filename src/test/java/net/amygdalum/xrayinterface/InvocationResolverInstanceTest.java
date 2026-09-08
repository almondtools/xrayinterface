package net.amygdalum.xrayinterface;

import static net.amygdalum.xrayinterface.ConvertedType.converted;
import static net.amygdalum.xrayinterface.FixedType.VOID;
import static net.amygdalum.xrayinterface.FixedType.fixed;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

@SuppressWarnings("unused")
public class InvocationResolverInstanceTest {

	@Test
	public void testFindField() throws Exception {
		InvocationResolver resolver = new InvocationResolver(TestSubClass.class);
		assertThat(resolver.findField("b", fixed(boolean.class)), notNullValue());
		assertThat(resolver.findField("s", fixed(String.class)), notNullValue());
		assertThat(resolver.findField("i", fixed(int.class)), notNullValue());
	}

	@Test
	public void testCreateMethodInvocatorGetterInvocator() throws Exception {
		InvocationResolver resolver = new InvocationResolver(TestSubClass.class);
		assertThat(resolver.createGetterInvocator("s", fixed(String.class)), notNullValue());
		assertThat(resolver.createGetterInvocator("i", fixed(int.class)), notNullValue());
		assertThat(resolver.createGetterInvocator("b", fixed(boolean.class)), notNullValue());
	}

	@Test
	public void testCreateMethodInvocatorGetterInvocatorConverted() throws Exception {
		InvocationResolver resolver = new InvocationResolver(ConvertibleTestClass.class);
		assertThat(resolver.createGetterInvocator("convertible", converted(ConvertibleObject.class, ConvertibleInterface.class)), notNullValue());
	}

	@Test
	public void testCreateMethodInvocatorSetterInvocator() throws Exception {
		InvocationResolver resolver = new InvocationResolver(TestSubClass.class);
		assertThat(resolver.createSetterInvocator("s", fixed(String.class)), notNullValue());
		assertThat(resolver.createSetterInvocator("i", fixed(int.class)), notNullValue());
		assertThat(resolver.createSetterInvocator("b", fixed(boolean.class)), notNullValue());
	}

	@Test
	public void testCreateMethodInvocatorSetterInvocatorConverted() throws Exception {
		InvocationResolver resolver = new InvocationResolver(ConvertibleTestClass.class);
		assertThat(resolver.createSetterInvocator("convertible", converted(ConvertibleObject.class, ConvertibleInterface.class)), notNullValue());
	}

	@Test
	public void testCreateMethodInvocatorGetterInvocatorFails() throws Exception {
		assertThrows(NoSuchFieldException.class, () -> {
			InvocationResolver resolver = new InvocationResolver(TestSubClass.class);
			resolver.createGetterInvocator("a", fixed(boolean.class));
		});
	}

	@Test
	public void testCreateMethodInvocatorSetterInvocatorFails() throws Exception {
		assertThrows(NoSuchFieldException.class, () -> {
			InvocationResolver resolver = new InvocationResolver(TestSubClass.class);
			resolver.createSetterInvocator("a", fixed(boolean.class));
		});
	}

	@Test
	public void testFindFieldNonExisting() throws Exception {
		assertThrows(NoSuchFieldException.class, () -> {
			InvocationResolver resolver = new InvocationResolver(TestSubClass.class);
			assertThat(resolver.findField("a", fixed(boolean.class)), notNullValue());
		});
	}

	@Test
	public void testFindSuperFieldWronglyTyped() throws Exception {
		assertThrows(NoSuchFieldException.class, () -> {
			InvocationResolver resolver = new InvocationResolver(TestSubClass.class);
			assertThat(resolver.findField("s", fixed(boolean.class)), notNullValue());
		});
	}

	@Test
	public void testFindFieldWronglyTyped() throws Exception {
		assertThrows(NoSuchFieldException.class, () -> {
			InvocationResolver resolver = new InvocationResolver(TestSubClass.class);
			assertThat(resolver.findField("b", fixed(String.class)), notNullValue());
		});
	}

	@Test
	public void testCreateMethodInvocatorMethodInvocatorWithoutConversion() throws Exception {
		InvocationResolver resolver = new InvocationResolver(TestSubClass.class);

		assertThat(resolver.createMethodInvocator("methoda", VOID, types(), types()), notNullValue());
		assertThat(resolver.createMethodInvocator("methodb", VOID, types(String.class), types()), notNullValue());
		assertThat(resolver.createMethodInvocator("methodc", VOID, types(String.class), types(Exception.class)), notNullValue());
		assertThat(resolver.createMethodInvocator("methodd", fixed(String.class), types(String.class), types(Exception.class)), notNullValue());
		assertThat(resolver.createMethodInvocator("methode", fixed(String.class), types(String.class), types(Exception.class)), notNullValue());
	}

	@Test
	public void testCreateMethodInvocatorMethodInvocatorWithConversion() throws Exception {
		InvocationResolver resolver = new InvocationResolver(ConvertibleTestClass.class);
		
		assertThat(resolver.createMethodInvocator("methoda", VOID, types(converted(ConvertibleObject.class,ConvertibleInterface.class)), types()), notNullValue());
		assertThat(resolver.createMethodInvocator("methodb", converted(ConvertibleObject.class, ConvertibleInterface.class), types(), types()), notNullValue());
	}

	@Test
	public void testCreateMethodInvocatorMethodInvocatorNonExisting() throws Exception {
		assertThrows(NoSuchMethodException.class, () -> {
			InvocationResolver resolver = new InvocationResolver(TestSubClass.class);
			resolver.createMethodInvocator("methodz", fixed(String.class), types(), types());
		});
	}

	@Test
	public void testCreateMethodInvocatorMethodInvocatorWronglyReturnType() throws Exception {
		assertThrows(NoSuchMethodException.class, () -> {
			InvocationResolver resolver = new InvocationResolver(TestSubClass.class);
			resolver.createMethodInvocator("methoda", fixed(String.class), types(), types());
		});
	}

	@Test
	public void testCreateMethodInvocatorMethodInvocatorWronglySignature() throws Exception {
		assertThrows(NoSuchMethodException.class, () -> {
			InvocationResolver resolver = new InvocationResolver(TestSubClass.class);
			resolver.createMethodInvocator("methodb", VOID, types(), types());
		});
	}

	@Test
	public void testCreateMethodInvocatorMethodInvocatorWronglyTyped() throws Exception {
		assertThrows(NoSuchMethodException.class, () -> {
			InvocationResolver resolver = new InvocationResolver(TestSubClass.class);
			resolver.createMethodInvocator("methodb", VOID, types(int.class), types());
		});
	}

	@Test
	public void testCreateMethodInvocatorMethodInvocatorWronglyExceptionTyped() throws Exception {
		assertThrows(NoSuchMethodException.class, () -> {
			InvocationResolver resolver = new InvocationResolver(TestSubClass.class);
			resolver.createMethodInvocator("methodb", VOID, types(String.class), types(Exception.class));
		});
	}

	@Test
	public void testCreateMethodInvocatorMethodInvocatorWronglyExceptionTypedInSuperclass() throws Exception {
		assertThrows(NoSuchMethodException.class, () -> {
			InvocationResolver resolver = new InvocationResolver(TestSubClass.class);
			resolver.createMethodInvocator("methode", fixed(String.class), types(String.class), types(IOException.class));
		});
	}

	@Test
	public void testCreateMethodInvocatorMethodInvocatorNotConvertibleArguments() throws Exception {
		assertThrows(NoSuchMethodException.class, () -> {
			InvocationResolver resolver = new InvocationResolver(ConvertibleTestClass.class);
			resolver.createMethodInvocator("methoda", VOID, types(ConvertibleInterface.class), types());
		});
	}

	@Test
	public void testCreateMethodInvocatorMethodInvocatorNotConvertibleResult() throws Exception {
		assertThrows(NoSuchMethodException.class, () -> {
			InvocationResolver resolver = new InvocationResolver(ConvertibleTestClass.class);
			resolver.createMethodInvocator("methodb", fixed(ConvertibleInterface.class), types(), types());
		});
	}

	@Test
	public void testCreateMethodInvocatorMethodInvocatorFailedConvertibleArguments() throws Exception {
		assertThrows(NoSuchMethodException.class, () -> {
			InvocationResolver resolver = new InvocationResolver(ConvertibleTestClass.class);
			resolver.createMethodInvocator("methoda", VOID, types(converted(ConvertibleInterface.class, ConvertibleInterface.class)), types());
		});
	}

	@Test
	public void testCreateMethodInvocatorMethodInvocatorFailedConvertibleResult() throws Exception {
		assertThrows(NoSuchMethodException.class, () -> {
			InvocationResolver resolver = new InvocationResolver(ConvertibleTestClass.class);
			resolver.createMethodInvocator("methodb", converted(ConvertibleInterface.class, ConvertibleInterface.class), types(), types());
		});
	}

	private Type[] types(Object... types) {
		return Stream.of(types)
			.map(type -> {
				if (type instanceof Type) {
					return (Type) type;
				} else if (type instanceof Class<?>) {
					return fixed((Class<?>) type);
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
