package net.amygdalum.xrayinterface.examples.singleton.hamcrest;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;

import java.util.List;

import org.hamcrest.Matcher;

import org.junit.jupiter.api.Test;

import net.amygdalum.xrayinterface.IsEquivalent;
import net.amygdalum.xrayinterface.XRayInterface;
import net.amygdalum.xrayinterface.examples.singleton.TheOneAndOnly;


public class TheOneAndOnlyTest {

	@Test
	public void testDirectSingletonModification() throws Exception {
		TheOneAndOnly instance = TheOneAndOnly.getInstance();
		XRayInterface.xray(instance).to(XRayed.class).setUnique(false);
		assertThat(instance.isUnique(), is(false));
	}
	
	@Test
	public void testSingletonFactoryIntrusion() throws Exception {
		TheOneAndOnly instance = XRayInterface.xray(TheOneAndOnly.class).to(XRayedStatic.class).getInstance();
		XRayInterface.xray(instance).to(XRayed.class).setUnique(false);
		assertThat(TheOneAndOnly.getInstance().isUnique(), is(false));
	}
	
	@Test
	public void testSingletonInjection() throws Exception {
		XRayedStaticWithConstructor xrayedOneAndOnly = XRayInterface.xray(TheOneAndOnly.class).to(XRayedStaticWithConstructor.class);
		TheOneAndOnly instance = xrayedOneAndOnly.newTheOneAndOnly();
		XRayInterface.xray(instance).to(XRayed.class).setUnique(false);
		xrayedOneAndOnly.setInstance(instance);
		assertThat(TheOneAndOnly.getInstance().isUnique(), is(false));
	}
	
	@Test
	public void testMatchingSingleton() throws Exception {
		TheOneAndOnly instance = newInstance();

		assertThat(instance, XRayedMatcher.matchesTheOneAndOnly()
			.withUnique(true));
	}

	@Test
	public void testMatchingSingletonList() throws Exception {
		TheOneAndOnly unique = newInstance();
		TheOneAndOnly cloned = newInstance();
		XRayInterface.xray(cloned).to(XRayed.class).setUnique(false);
		List<TheOneAndOnly> instances = asList(unique, cloned);

		assertThat(instances, contains(
			XRayedMatcher.matchesTheOneAndOnly().withUnique(true),
			XRayedMatcher.matchesTheOneAndOnly().withUnique(false)));
	}

	@Test
	public void testMatchingSingletonListInAnyOrder() throws Exception {
		TheOneAndOnly unique = newInstance();
		TheOneAndOnly cloned = newInstance();
		XRayInterface.xray(cloned).to(XRayed.class).setUnique(false);
		List<TheOneAndOnly> instances = asList(unique, cloned);

		assertThat(instances, containsInAnyOrder(
			XRayedMatcher.matchesTheOneAndOnly().withUnique(false),
			XRayedMatcher.matchesTheOneAndOnly().withUnique(true)));
	}

	private TheOneAndOnly newInstance() {
		return XRayInterface.xray(TheOneAndOnly.class).to(XRayedStaticWithConstructor.class).newTheOneAndOnly();
	}

	interface XRayed {
		void setUnique(boolean unique);
	}

	interface XRayedMatcher extends Matcher<TheOneAndOnly> {

		XRayedMatcher withUnique(boolean unique);

		static XRayedMatcher matchesTheOneAndOnly() {
			return IsEquivalent.equivalentTo(XRayedMatcher.class);
		}

	}

	interface XRayedStatic {
		TheOneAndOnly getInstance();
	}

	interface XRayedStaticWithConstructor {
		TheOneAndOnly newTheOneAndOnly();
		void setInstance(TheOneAndOnly instance);
	}

}
