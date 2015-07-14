package com.almondtools.xrayinterface.examples.singleton;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.almondtools.xrayinterface.ClassAccess;
import com.almondtools.xrayinterface.ObjectAccess;


public class TheOneAndOnlyTest {

	@Test
	public void testDirectSingletonModification() throws Exception {
		TheOneAndOnly instance = TheOneAndOnly.getInstance();
		ObjectAccess.xray(instance).to(XRayed.class).setUnique(false);
		assertThat(instance.isUnique(), is(false));
	}
	
	@Test
	public void testSingletonFactoryIntrusion() throws Exception {
		TheOneAndOnly instance = ClassAccess.xray(TheOneAndOnly.class).to(XRayedStatic.class).getInstance();
		ObjectAccess.xray(instance).to(XRayed.class).setUnique(false);
		assertThat(TheOneAndOnly.getInstance().isUnique(), is(false));
	}
	
	@Test
	public void testSingletonInjection() throws Exception {
		XRayedStaticWithConstructor xrayedOneAndOnly = ClassAccess.xray(TheOneAndOnly.class).to(XRayedStaticWithConstructor.class);
		TheOneAndOnly instance = xrayedOneAndOnly.create();
		ObjectAccess.xray(instance).to(XRayed.class).setUnique(false);
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
		TheOneAndOnly create();
		void setInstance(TheOneAndOnly instance);
	}

}
