package net.amygdalum.xrayinterface.examples.singleton;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

import net.amygdalum.xrayinterface.XRayInterface;


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
	
	interface XRayed {
		void setUnique(boolean unique);
	}

	interface XRayedStatic {
		TheOneAndOnly getInstance();
	}

	interface XRayedStaticWithConstructor {
		TheOneAndOnly newTheOneAndOnly();
		void setInstance(TheOneAndOnly instance);
	}

}
