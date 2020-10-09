package net.amygdalum.xrayinterface;

import static java.util.stream.Collectors.toSet;
import static net.amygdalum.xrayinterface.XRayMatcher.providesFeaturesOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

public class XRayInterfaceTest {

	private LockedObject object;

	@Before
	public void before() {
		object = new LockedObject();
	}

	@Test
	public void testXrayable() throws Exception {
		assertThat(LockedObject.class, providesFeaturesOf(UnlockedObject.class));
		assertThat(LockedObject.class, not(providesFeaturesOf(UnlockedNotMatchingMethodObject.class)));
		assertThat(LockedObject.class, not(providesFeaturesOf(UnlockedNotMatchingGetterObject.class)));
		assertThat(LockedObject.class, not(providesFeaturesOf(UnlockedNotMatchingSetterObject.class)));
		assertThat(LockedObjectWithDeclaredExceptions.class, providesFeaturesOf(UnlockedWithCorrectExceptions.class));
		assertThat(LockedObjectWithDeclaredExceptions.class, not(providesFeaturesOf(UnlockedWithMissingExceptions.class)));
		assertThat(LockedObjectWithDeclaredExceptions.class, not(providesFeaturesOf(UnlockedWithFalseExceptions.class)));
	}

	@Test
	public void testMethodInvocation() throws Exception {
		UnlockedObject unlocked = XRayInterface.xray(object).to(UnlockedObject.class);
		assertThat(unlocked.myMethod("123", true), equalTo(123));
		assertThat(unlocked.myMethod("123", false), equalTo(0));
		assertThat(unlocked.myMethod("ABC", false), equalTo(0));
	}

	@Test
	public void testMethodEquals() throws Exception {
		UnlockedObject unlocked = XRayInterface.xray(object).to(UnlockedObject.class);
		assertThat(unlocked.equals(unlocked), is(true));
	}

	@Test
	public void testMethodHashcode() throws Exception {
		UnlockedObject unlocked = XRayInterface.xray(object).to(UnlockedObject.class);
		assertThat(unlocked.hashCode(), equalTo(object.hashCode()));
	}

	@Test
	public void testMethodInvocationWithBindingAnnotations() throws Exception {
		UnlockedWithBindingAnnotationsObject unlocked = XRayInterface.xray(object).to(UnlockedWithBindingAnnotationsObject.class);
		assertThat(unlocked.method("123", true), equalTo(123));
		assertThat(unlocked.method("123", false), equalTo(0));
		assertThat(unlocked.method("ABC", false), equalTo(0));
	}

	@Test
	public void testSetGetField() throws Exception {
		UnlockedObject unlocked = XRayInterface.xray(object).to(UnlockedObject.class);
		unlocked.setMyField("123");
		assertThat(object.myPublicMethod(), equalTo(123));
		assertThat(unlocked.getMyField(), equalTo("123"));

		unlocked.setMyField("ABC");
		assertThat(object.myPublicMethod(), equalTo(0));
		assertThat(unlocked.getMyField(), equalTo("ABC"));
	}

	@Test
	public void testSetGetFieldWithBindingAnnotations() throws Exception {
		UnlockedWithBindingAnnotationsObject unlocked = XRayInterface.xray(object).to(UnlockedWithBindingAnnotationsObject.class);
		unlocked.set("123");
		assertThat(object.myPublicMethod(), equalTo(123));
		assertThat(unlocked.get(), equalTo("123"));
		
		unlocked.set("ABC");
		assertThat(object.myPublicMethod(), equalTo(0));
		assertThat(unlocked.get(), equalTo("ABC"));
	}
	
	@Test
	public void testNotExistingMethodInvocation() throws Exception {
		try {
			UnlockedNotMatchingMethodObject unlocked = XRayInterface.xray(object).to(UnlockedNotMatchingMethodObject.class);
			unlocked.notExistingMethod();
		} catch (InterfaceMismatchException e) {
			assertThat(e.toString(), containsString("notExistingMethod"));
		}
	}

	@Test
	public void testNotExistingGetter() throws Exception {
		try {
			UnlockedNotMatchingGetterObject unlocked = XRayInterface.xray(object).to(UnlockedNotMatchingGetterObject.class);
			unlocked.getNotExisting();
		} catch (InterfaceMismatchException e) {
			assertThat(e.toString(), containsString("getNotExisting"));
		}
	}

	@Test
	public void testNotExistingBindingGetter() throws Exception {
		try {
			UnlockedNotMatchingBindingGetterObject unlocked = XRayInterface.xray(object).to(UnlockedNotMatchingBindingGetterObject.class);
			unlocked.get();
		} catch (InterfaceMismatchException e) {
			assertThat(e.toString(), containsString("notExisting"));
		}
	}

	@Test
	public void testNotExistingSetter() throws Exception {
		try {
			UnlockedNotMatchingSetterObject unlocked = XRayInterface.xray(object).to(UnlockedNotMatchingSetterObject.class);
			unlocked.setNotExisting(true);
		} catch (InterfaceMismatchException e) {
			assertThat(e.toString(), containsString("setNotExisting"));
		}
	}

	@Test
	public void testNotExistingBindingSetter() throws Exception {
		try {
			UnlockedNotMatchingBindingSetterObject unlocked = XRayInterface.xray(object).to(UnlockedNotMatchingBindingSetterObject.class);
			unlocked.set(true);
		} catch (InterfaceMismatchException e) {
			assertThat(e.toString(), containsString("notExisting"));
		}
	}

	@Test
	public void testSuperClassAccessForMethods() throws Exception {
		UnlockedObject unlocked = XRayInterface.xray(object).to(UnlockedObject.class);
		assertThat(unlocked.superMethod(), equalTo(5.0));
	}

	@Test
	public void testSuperClassAccessForProperties() throws Exception {
		UnlockedObject unlocked = XRayInterface.xray(object).to(UnlockedObject.class);
		unlocked.setSuperField(1.0);
		assertThat(unlocked.getSuperField(), equalTo(1.0));
	}

	@Test
	public void testCorrectExceptionSignature() throws Exception {
		UnlockedWithCorrectExceptions unlocked = XRayInterface.xray(new LockedObjectWithDeclaredExceptions()).to(UnlockedWithCorrectExceptions.class);
		try {
			String msg = unlocked.myMethod(null);
			fail("expected io exception, but found: " + msg);
		} catch (IOException e) {
			// expected
		}
	}

	@Test(expected = InterfaceMismatchException.class)
	public void testMissingExceptionSignature() throws Exception {
		XRayInterface.xray(new LockedObjectWithDeclaredExceptions()).to(UnlockedWithMissingExceptions.class);
	}

	@Test(expected = InterfaceMismatchException.class)
	public void testFalseExceptionSignature() throws Exception {
		XRayInterface.xray(new LockedObjectWithDeclaredExceptions()).to(UnlockedWithFalseExceptions.class);
	}

	@Test
	public void testFinalGetSet() throws Exception {
		UnlockedObject unlocked = XRayInterface.xray(object).to(UnlockedObject.class);
		unlocked.setInteger(-2);
		assertThat(unlocked.getInteger(), equalTo(-2));
	}

	@Test
	public void testFinalSetAfterGet() throws Exception {
		UnlockedObject unlocked = XRayInterface.xray(object).to(UnlockedObject.class);
		int result = unlocked.getInteger();
		unlocked.setInteger(-2);
		unlocked.setInteger(result);
		assertThat(unlocked.getInteger(), equalTo(2));
	}

	@Test
	public void testGetInterfaceMethodsUnbound() throws Exception {
		XRayInterface xray = XRayInterface.xray(object);
		assertThat(xray.getInterfaceMethods().keySet(), empty());
	}

	@Test
	public void testGetInterfaceMethodsBound() throws Exception {
		XRayInterface xray = XRayInterface.xray(object);
		
		xray.to(UnlockedObject.class);

		assertThat(xray.getInterfaceMethods().keySet(), hasSize(UnlockedObject.class.getMethods().length));
	}
	
	@Test
	public void testGetConstructors() throws Exception {
		XRayInterface xray = XRayInterface.xray(object);
		
		xray.to(UnlockedObject.class);

		assertThat(xray.getConstructors(), hasSize(0));
	}

	@Test
	public void testGetFieldSetters() throws Exception {
		XRayInterface xray = XRayInterface.xray(object);
		
		xray.to(UnlockedObject.class);

		assertThat(xray.getFieldSetters(), hasSize(3));
		assertThat(xray.getFieldSetters().stream()
			.map(field -> field.getFieldName())
			.collect(toSet()), containsInAnyOrder("myField", "superField", "integer"));
	}

	@Test
	public void testGetFieldGetters() throws Exception {
		XRayInterface xray = XRayInterface.xray(object);
		
		xray.to(UnlockedObject.class);

		assertThat(xray.getFieldGetters(), hasSize(3));
		assertThat(xray.getFieldGetters().stream()
			.map(field -> field.getFieldName())
			.collect(toSet()), containsInAnyOrder("myField", "superField", "integer"));
	}

	@Test
	public void testGetMethods() throws Exception {
		XRayInterface xray = XRayInterface.xray(object);
		
		xray.to(UnlockedObject.class);

		assertThat(xray.getMethods(), hasSize(2));
		assertThat(xray.getMethods().stream()
			.map(method -> method.getName())
			.collect(toSet()), containsInAnyOrder("myMethod", "superMethod"));
	}

	@Test
	public void testGetFieldProperties() throws Exception {
		XRayInterface xray = XRayInterface.xray(object);
		
		xray.to(UnlockedObject.class);

		assertThat(xray.getFieldProperties(), hasSize(3));
		assertThat(xray.getFieldProperties().stream()
			.map(field -> field.get().getFieldName())
			.collect(toSet()), containsInAnyOrder("myField", "superField", "integer"));
		assertThat(xray.getFieldProperties().stream()
			.map(field -> field.set().getFieldName())
			.collect(toSet()), containsInAnyOrder("myField", "superField", "integer"));
	}

	@Test
	public void testGetFieldPropertiesAsymetric() throws Exception {
		XRayInterface xray = XRayInterface.xray(object);
		
		xray.to(UnlockedObjectAsymetricProperties.class);

		assertThat(xray.getFieldProperties(), hasSize(2));
		assertThat(xray.getFieldProperties().stream()
			.filter(field -> field.get() != null)
			.map(field -> field.get().getFieldName())
			.collect(toSet()), containsInAnyOrder("superField"));
		assertThat(xray.getFieldProperties().stream()
			.filter(field -> field.set() != null)
			.map(field -> field.set().getFieldName())
			.collect(toSet()), containsInAnyOrder("myField"));
	}

	interface UnlockedObject {
		void setMyField(String value);

		String getMyField();

		int myMethod(String string, boolean flag);

		double getSuperField();

		void setSuperField(double d);

		double superMethod();

		void setInteger(int i);

		int getInteger();

	}

	interface UnlockedObjectAsymetricProperties {
		void setMyField(String value);

		double getSuperField();

	}

	interface UnlockedNotMatchingMethodObject {

		boolean notExistingMethod();
	}

	interface UnlockedNotMatchingSetterObject {

		void setNotExisting(boolean b);
	}

	interface UnlockedNotMatchingGetterObject {

		boolean getNotExisting();
	}

	interface UnlockedNotMatchingBindingSetterObject {
		
		@SetProperty("notExisting")
		void set(boolean b);
	}
	
	interface UnlockedNotMatchingBindingGetterObject {
		
		@GetProperty("notExisting")
		boolean get();
	}
	
	interface UnlockedWithCorrectExceptions {

		String myMethod(String string) throws IOException;
	}

	interface UnlockedWithMissingExceptions {

		String myMethod(String string);
	}

	interface UnlockedWithFalseExceptions {

		String myMethod(String string) throws ClassCastException;
	}

	interface UnlockedWithBindingAnnotationsObject {
		@SetProperty("myField")
		void set(String value);

		@GetProperty("myField")
		String get();

		@Delegate("myMethod")
		int method(String string, boolean flag);

	}

}
