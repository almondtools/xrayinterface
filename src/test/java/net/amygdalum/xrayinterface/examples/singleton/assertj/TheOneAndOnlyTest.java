package net.amygdalum.xrayinterface.examples.singleton.assertj;

import static java.util.Arrays.asList;
import static net.amygdalum.xrayinterface.IsEquivalent.isEquivalent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import net.amygdalum.xrayinterface.IsEquivalent;
import net.amygdalum.xrayinterface.XRayInterface;
import net.amygdalum.xrayinterface.examples.singleton.TheOneAndOnly;

/**
 * the assertj pendant of {@link net.amygdalum.xrayinterface.examples.singleton.hamcrest.TheOneAndOnlyTest} - the same
 * scenarios asserted with assertjs satisfies methods and xrayinterfaces {@link IsEquivalent#isEquivalent(Class)}
 * consumers
 */
public class TheOneAndOnlyTest {

	@Test
	public void testDirectSingletonModification() throws Exception {
		TheOneAndOnly instance = TheOneAndOnly.getInstance();
		XRayInterface.xray(instance).to(XRayed.class).setUnique(false);
		assertThat(instance.isUnique()).isFalse();
	}

	@Test
	public void testSingletonFactoryIntrusion() throws Exception {
		TheOneAndOnly instance = XRayInterface.xray(TheOneAndOnly.class).to(XRayedStatic.class).getInstance();
		XRayInterface.xray(instance).to(XRayed.class).setUnique(false);
		assertThat(TheOneAndOnly.getInstance().isUnique()).isFalse();
	}

	@Test
	public void testSingletonInjection() throws Exception {
		XRayedStaticWithConstructor xrayedOneAndOnly = XRayInterface.xray(TheOneAndOnly.class).to(XRayedStaticWithConstructor.class);
		TheOneAndOnly instance = xrayedOneAndOnly.newTheOneAndOnly();
		XRayInterface.xray(instance).to(XRayed.class).setUnique(false);
		xrayedOneAndOnly.setInstance(instance);
		assertThat(TheOneAndOnly.getInstance().isUnique()).isFalse();
	}

	@Test
	public void testMatchingSingleton() throws Exception {
		TheOneAndOnly instance = newInstance();

		assertThat(instance).satisfies(XRayedConsumer.isEquivalentToTheOneAndOnly()
			.withUnique(true));
	}

	@Test
	public void testMatchingSingletonList() throws Exception {
		TheOneAndOnly unique = newInstance();
		TheOneAndOnly cloned = newInstance();
		XRayInterface.xray(cloned).to(XRayed.class).setUnique(false);
		List<TheOneAndOnly> instances = asList(unique, cloned);

		assertThat(instances).satisfiesExactly(
			XRayedConsumer.isEquivalentToTheOneAndOnly().withUnique(true),
			XRayedConsumer.isEquivalentToTheOneAndOnly().withUnique(false));
	}

	@Test
	public void testMatchingSingletonListInAnyOrder() throws Exception {
		TheOneAndOnly unique = newInstance();
		TheOneAndOnly cloned = newInstance();
		XRayInterface.xray(cloned).to(XRayed.class).setUnique(false);
		List<TheOneAndOnly> instances = asList(unique, cloned);

		assertThat(instances).satisfiesExactlyInAnyOrder(
			XRayedConsumer.isEquivalentToTheOneAndOnly().withUnique(false),
			XRayedConsumer.isEquivalentToTheOneAndOnly().withUnique(true));
	}

	@Test
	public void testMismatchingSingleton() throws Exception {
		TheOneAndOnly instance = newInstance();

		assertThatThrownBy(() -> assertThat(instance).satisfies(XRayedConsumer.isEquivalentToTheOneAndOnly()
			.withUnique(false)))
				.isInstanceOf(AssertionError.class)
				.hasMessageContaining("Unique=true");
	}

	private TheOneAndOnly newInstance() {
		return XRayInterface.xray(TheOneAndOnly.class).to(XRayedStaticWithConstructor.class).newTheOneAndOnly();
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

	interface XRayedConsumer extends Consumer<TheOneAndOnly> {

		XRayedConsumer withUnique(boolean unique);

		static XRayedConsumer isEquivalentToTheOneAndOnly() {
			return isEquivalent(XRayedConsumer.class);
		}

	}

}
