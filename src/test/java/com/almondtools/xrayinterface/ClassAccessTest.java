package com.almondtools.xrayinterface;

import static com.almondtools.xrayinterface.ClassUnlockableMatcher.canBeTreatedAs;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class ClassAccessTest {

	@Test
	public void testXrayable() throws Exception {
		
		assertThat(LockedObjectWithPrivateConstructor.class, canBeTreatedAs(UnlockedObject.class));
		assertThat(LockedObjectWithPrivateConstructor.class, not(canBeTreatedAs(UnlockedNotMatchingObject.class)));
		assertThat(LockedObjectWithPrivateConstructor.class, not(canBeTreatedAs(UnlockedFantasyObject.class)));
	}

	@Test
	public void testConstructorInvocation() throws Exception {
		UnlockedObject unlocked = ClassAccess.xray(LockedObjectWithPrivateConstructor.class).to(UnlockedObject.class);
		assertThat(unlocked.newLockedObjectWithPrivateConstructor().getMyField(), equalTo("initialized"));
	}

	@Test
	public void testConstructorInvocationWithBindingAnnotations() throws Exception {
		UnlockedWithBindingAnnotationsObject unlocked = ClassAccess.xray(LockedObjectWithPrivateConstructor.class).to(UnlockedWithBindingAnnotationsObject.class);
		assertThat(unlocked.construct().getMyField(), equalTo("initialized"));
	}

	@Test
	public void testStaticInvocation() throws Exception {
		UnlockedObject unlocked = ClassAccess.xray(LockedObjectWithPrivateConstructor.class).to(UnlockedObject.class);
		unlocked.setDEFAULT(null);
		assertThat(unlocked.reset().getMyField(), nullValue());
	}

	@Test
	public void testStaticInvocationWithBindingAnnotations() throws Exception {
		UnlockedWithBindingAnnotationsObject unlocked = ClassAccess.xray(LockedObjectWithPrivateConstructor.class).to(UnlockedWithBindingAnnotationsObject.class);
		unlocked.set(null);
		assertThat(unlocked.method().getMyField(), nullValue());
	}

	@Test
	public void testStaticSetAfterGet() throws Exception {
		UnlockedObject unlocked = ClassAccess.xray(LockedObjectWithPrivateConstructor.class).to(UnlockedObject.class);
		String result = unlocked.getDEFAULT();
		unlocked.setDEFAULT("");
		unlocked.setDEFAULT(result);
		assertThat(unlocked.getDEFAULT(), nullValue());
	}

	@Test
	public void testStaticSetGetWithBindingAnnotations() throws Exception {
		UnlockedWithBindingAnnotationsObject unlocked = ClassAccess.xray(LockedObjectWithPrivateConstructor.class).to(UnlockedWithBindingAnnotationsObject.class);
		unlocked.set(null);
		assertThat(unlocked.get(), nullValue());
		unlocked.set("default");
		assertThat(unlocked.method().getMyField(), equalTo("default"));
		assertThat(unlocked.get(), equalTo("default"));
	}

	@Test(expected = InterfaceMismatchException.class)
	public void testWrongSignature() throws Exception {
		ClassAccess.xray(LockedObjectWithPrivateConstructor.class).to(UnlockedNotMatchingObject.class);
	}

	@Test
	public void testStaticSetGet() throws Exception {
		UnlockedObject unlocked = ClassAccess.xray(LockedObjectWithPrivateConstructor.class).to(UnlockedObject.class);
		unlocked.setDEFAULT(null);
		assertThat(unlocked.getDEFAULT(), nullValue());
		unlocked.setDEFAULT("default");
		assertThat(unlocked.reset().getMyField(), equalTo("default"));
		assertThat(unlocked.getDEFAULT(), equalTo("default"));
	}

	public static interface UnlockedObject {

		public LockedObjectWithPrivateConstructor newLockedObjectWithPrivateConstructor();

		public LockedObjectWithPrivateConstructor reset();

		public void setDEFAULT(String value);

		public String getDEFAULT();

	}

	public static interface UnlockedNotMatchingObject {

		public void setNOTEXISTING(String value);

		public String getNOTEXISTING();

	}

	public static interface UnlockedFantasyObject {

		public void method(String arg);

	}

	interface UnlockedWithBindingAnnotationsObject {
		
		@Construct
		public LockedObjectWithPrivateConstructor construct();

		@Delegate("reset")
		public LockedObjectWithPrivateConstructor method();

		@SetProperty("DEFAULT")
		public void set(String value);

		@GetProperty("DEFAULT")
		public String get();


	}

}
