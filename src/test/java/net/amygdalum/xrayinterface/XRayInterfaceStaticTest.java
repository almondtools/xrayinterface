package net.amygdalum.xrayinterface;

import static net.amygdalum.xrayinterface.ClassUnlockableMatcher.canBeTreatedAs;
import static java.util.stream.Collectors.toSet;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import net.amygdalum.xrayinterface.Construct;
import net.amygdalum.xrayinterface.Delegate;
import net.amygdalum.xrayinterface.GetProperty;
import net.amygdalum.xrayinterface.InterfaceMismatchException;
import net.amygdalum.xrayinterface.SetProperty;
import net.amygdalum.xrayinterface.XRayInterface;


public class XRayInterfaceStaticTest {

	@Test
	public void testXrayable() throws Exception {
		
		assertThat(LockedObjectWithPrivateConstructor.class, canBeTreatedAs(UnlockedObject.class));
		assertThat(LockedObjectWithPrivateConstructor.class, not(canBeTreatedAs(UnlockedNotMatchingObject.class)));
		assertThat(LockedObjectWithPrivateConstructor.class, not(canBeTreatedAs(UnlockedFantasyObject.class)));
	}

	@Test
	public void testConstructorInvocation() throws Exception {
		UnlockedObject unlocked = XRayInterface.xray(LockedObjectWithPrivateConstructor.class).to(UnlockedObject.class);
		assertThat(unlocked.newLockedObjectWithPrivateConstructor().getMyField(), equalTo("initialized"));
	}

	@Test
	public void testConstructorInvocationWithBindingAnnotations() throws Exception {
		UnlockedWithBindingAnnotationsObject unlocked = XRayInterface.xray(LockedObjectWithPrivateConstructor.class).to(UnlockedWithBindingAnnotationsObject.class);
		assertThat(unlocked.construct().getMyField(), equalTo("initialized"));
	}

	@Test
	public void testStaticInvocation() throws Exception {
		UnlockedObject unlocked = XRayInterface.xray(LockedObjectWithPrivateConstructor.class).to(UnlockedObject.class);
		unlocked.setDEFAULT(null);
		assertThat(unlocked.reset().getMyField(), nullValue());
	}

	@Test
	public void testStaticInvocationWithBindingAnnotations() throws Exception {
		UnlockedWithBindingAnnotationsObject unlocked = XRayInterface.xray(LockedObjectWithPrivateConstructor.class).to(UnlockedWithBindingAnnotationsObject.class);
		unlocked.set(null);
		assertThat(unlocked.method().getMyField(), nullValue());
	}

	@Test
	public void testStaticSetAfterGet() throws Exception {
		UnlockedObject unlocked = XRayInterface.xray(LockedObjectWithPrivateConstructor.class).to(UnlockedObject.class);
		String result = unlocked.getDEFAULT();
		unlocked.setDEFAULT("");
		unlocked.setDEFAULT(result);
		assertThat(unlocked.getDEFAULT(), nullValue());
	}

	@Test
	public void testStaticSetGetWithBindingAnnotations() throws Exception {
		UnlockedWithBindingAnnotationsObject unlocked = XRayInterface.xray(LockedObjectWithPrivateConstructor.class).to(UnlockedWithBindingAnnotationsObject.class);
		unlocked.set(null);
		assertThat(unlocked.get(), nullValue());
		unlocked.set("default");
		assertThat(unlocked.method().getMyField(), equalTo("default"));
		assertThat(unlocked.get(), equalTo("default"));
	}

	@Test(expected = InterfaceMismatchException.class)
	public void testWrongSignature() throws Exception {
		XRayInterface.xray(LockedObjectWithPrivateConstructor.class).to(UnlockedNotMatchingObject.class);
	}

	@Test
	public void testStaticSetGet() throws Exception {
		UnlockedObject unlocked = XRayInterface.xray(LockedObjectWithPrivateConstructor.class).to(UnlockedObject.class);
		unlocked.setDEFAULT(null);
		assertThat(unlocked.getDEFAULT(), nullValue());
		unlocked.setDEFAULT("default");
		assertThat(unlocked.reset().getMyField(), equalTo("default"));
		assertThat(unlocked.getDEFAULT(), equalTo("default"));
	}

	@Test
	public void testGetInterfaceMethods() throws Exception {
		XRayInterface xray = XRayInterface.xray(LockedObjectWithPrivateConstructor.class);
		
		xray.to(UnlockedObject.class);

		assertThat(xray.getInterfaceMethods().keySet(), hasSize(UnlockedObject.class.getMethods().length));
	}

	@Test
	public void testGetConstructors() throws Exception {
		XRayInterface xray = XRayInterface.xray(LockedObjectWithPrivateConstructor.class);
		
		xray.to(UnlockedObject.class);

		assertThat(xray.getConstructors(), hasSize(1));
	}

	@Test
	public void testGetStaticSetters() throws Exception {
		XRayInterface xray = XRayInterface.xray(LockedObjectWithPrivateConstructor.class);
		
		xray.to(UnlockedObject.class);

		assertThat(xray.getStaticSetters(), hasSize(1));
		assertThat(xray.getStaticSetters().stream()
			.map(field -> field.getFieldName())
			.collect(toSet()), containsInAnyOrder("DEFAULT"));
	}

	@Test
	public void testGetStaticGetters() throws Exception {
		XRayInterface xray = XRayInterface.xray(LockedObjectWithPrivateConstructor.class);
		
		xray.to(UnlockedObject.class);

		assertThat(xray.getStaticGetters(), hasSize(1));
		assertThat(xray.getStaticGetters().stream()
			.map(field -> field.getFieldName())
			.collect(toSet()), containsInAnyOrder("DEFAULT"));
	}

	@Test
	public void testGetMethods() throws Exception {
		XRayInterface xray = XRayInterface.xray(LockedObjectWithPrivateConstructor.class);
		
		xray.to(UnlockedObject.class);

		assertThat(xray.getStaticMethods(), hasSize(1));
		assertThat(xray.getStaticMethods().stream()
			.map(method -> method.getName())
			.collect(toSet()), containsInAnyOrder("reset"));
	}

	@Test
	public void testGetStaticProperties() throws Exception {
		XRayInterface xray = XRayInterface.xray(LockedObjectWithPrivateConstructor.class);
		
		xray.to(UnlockedObject.class);

		assertThat(xray.getStaticProperties(), hasSize(1));
		assertThat(xray.getStaticProperties().stream()
			.filter(field -> field.get() != null)
			.map(field -> field.get().getFieldName())
			.collect(toSet()), containsInAnyOrder("DEFAULT"));
		assertThat(xray.getStaticProperties().stream()
			.filter(field -> field.get() != null)
			.map(field -> field.set().getFieldName())
			.collect(toSet()), containsInAnyOrder("DEFAULT"));
	}

	@Test
	public void testGetStaticPropertiesSetterOnly() throws Exception {
		XRayInterface xray = XRayInterface.xray(LockedObjectWithPrivateConstructor.class);
		
		xray.to(UnlockedObjectSetterOnly.class);

		assertThat(xray.getStaticProperties(), hasSize(1));
		assertThat(xray.getStaticProperties().stream()
			.filter(field -> field.get() != null)
			.collect(toSet()), empty());
		assertThat(xray.getStaticProperties().stream()
			.filter(field -> field.set() != null)
			.map(field -> field.set().getFieldName())
			.collect(toSet()), containsInAnyOrder("DEFAULT"));
	}

	@Test
	public void testGetStaticPropertiesGetterOnly() throws Exception {
		XRayInterface xray = XRayInterface.xray(LockedObjectWithPrivateConstructor.class);
		
		xray.to(UnlockedObjectGetterOnly.class);
		
		assertThat(xray.getStaticProperties(), hasSize(1));
		assertThat(xray.getStaticProperties().stream()
			.filter(field -> field.set() != null)
			.collect(toSet()), empty());
		assertThat(xray.getStaticProperties().stream()
			.filter(field -> field.get() != null)
			.map(field -> field.get().getFieldName())
			.collect(toSet()), containsInAnyOrder("DEFAULT"));
	}
	
	public static interface UnlockedObject {

		public LockedObjectWithPrivateConstructor newLockedObjectWithPrivateConstructor();

		public LockedObjectWithPrivateConstructor reset();

		public void setDEFAULT(String value);

		public String getDEFAULT();

	}

	public static interface UnlockedObjectGetterOnly {
		
		public String getDEFAULT();
		
	}
	
	public static interface UnlockedObjectSetterOnly {

		public void setDEFAULT(String value);

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
