package com.almondtools.xrayinterface;

import static com.almondtools.xrayinterface.XRayMatcher.providesFeaturesOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

public class ObjectAccessTest {

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
		UnlockedObject unlocked = ObjectAccess.xray(object).to(UnlockedObject.class);
		assertThat(unlocked.myMethod("123", true), equalTo(123));
		assertThat(unlocked.myMethod("123", false), equalTo(0));
		assertThat(unlocked.myMethod("ABC", false), equalTo(0));
	}

	@Test
	public void testMethodInvocationWithBindingAnnotations() throws Exception {
		UnlockedWithBindingAnnotationsObject unlocked = ObjectAccess.xray(object).to(UnlockedWithBindingAnnotationsObject.class);
		assertThat(unlocked.method("123", true), equalTo(123));
		assertThat(unlocked.method("123", false), equalTo(0));
		assertThat(unlocked.method("ABC", false), equalTo(0));
	}

	@Test
	public void testSetGetField() throws Exception {
		UnlockedObject unlocked = ObjectAccess.xray(object).to(UnlockedObject.class);
		unlocked.setMyField("123");
		assertThat(object.myPublicMethod(), equalTo(123));
		assertThat(unlocked.getMyField(), equalTo("123"));

		unlocked.setMyField("ABC");
		assertThat(object.myPublicMethod(), equalTo(0));
		assertThat(unlocked.getMyField(), equalTo("ABC"));
	}

	@Test
	public void testSetGetFieldWithBindingAnnotations() throws Exception {
		UnlockedWithBindingAnnotationsObject unlocked = ObjectAccess.xray(object).to(UnlockedWithBindingAnnotationsObject.class);
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
			UnlockedNotMatchingMethodObject unlocked = ObjectAccess.xray(object).to(UnlockedNotMatchingMethodObject.class);
			unlocked.notExistingMethod();
		} catch (InterfaceMismatchException e) {
			assertThat(e.toString(), containsString("notExistingMethod"));
		}
	}

	@Test
	public void testNotExistingGetter() throws Exception {
		try {
			UnlockedNotMatchingGetterObject unlocked = ObjectAccess.xray(object).to(UnlockedNotMatchingGetterObject.class);
			unlocked.getNotExisting();
		} catch (InterfaceMismatchException e) {
			assertThat(e.toString(), containsString("getNotExisting"));
		}
	}

	@Test
	public void testNotExistingSetter() throws Exception {
		try {
			UnlockedNotMatchingSetterObject unlocked = ObjectAccess.xray(object).to(UnlockedNotMatchingSetterObject.class);
			unlocked.setNotExisting(true);
		} catch (InterfaceMismatchException e) {
			assertThat(e.toString(), containsString("setNotExisting"));
		}
	}

	@Test
	public void testSuperClassAccessForMethods() throws Exception {
		UnlockedObject unlocked = ObjectAccess.xray(object).to(UnlockedObject.class);
		assertThat(unlocked.superMethod(), equalTo(5.0));
	}

	@Test
	public void testSuperClassAccessForProperties() throws Exception {
		UnlockedObject unlocked = ObjectAccess.xray(object).to(UnlockedObject.class);
		unlocked.setSuperField(1.0);
		assertThat(unlocked.getSuperField(), equalTo(1.0));
	}

	@Test
	public void testCorrectExceptionSignature() throws Exception {
		UnlockedWithCorrectExceptions unlocked = ObjectAccess.xray(new LockedObjectWithDeclaredExceptions()).to(UnlockedWithCorrectExceptions.class);
		try {
			String msg = unlocked.myMethod(null);
			fail("expected io exception, but found: " + msg);
		} catch (IOException e) {
			// expected
		}
	}

	@Test(expected = InterfaceMismatchException.class)
	public void testMissingExceptionSignature() throws Exception {
		ObjectAccess.xray(new LockedObjectWithDeclaredExceptions()).to(UnlockedWithMissingExceptions.class);
	}

	@Test(expected = InterfaceMismatchException.class)
	public void testFalseExceptionSignature() throws Exception {
		ObjectAccess.xray(new LockedObjectWithDeclaredExceptions()).to(UnlockedWithFalseExceptions.class);
	}

	@Test
	public void testFinalGetSet() throws Exception {
		UnlockedObject unlocked = ObjectAccess.xray(object).to(UnlockedObject.class);
		unlocked.setInteger(-2);
		assertThat(unlocked.getInteger(), equalTo(-2));
	}

	@Test
	public void testFinalSetAfterGet() throws Exception {
		UnlockedObject unlocked = ObjectAccess.xray(object).to(UnlockedObject.class);
		int result = unlocked.getInteger();
		unlocked.setInteger(-2);
		unlocked.setInteger(result);
		assertThat(unlocked.getInteger(), equalTo(2));
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

	interface UnlockedNotMatchingMethodObject {

		boolean notExistingMethod();
	}

	interface UnlockedNotMatchingSetterObject {

		void setNotExisting(boolean b);
	}

	interface UnlockedNotMatchingGetterObject {

		boolean getNotExisting();
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
