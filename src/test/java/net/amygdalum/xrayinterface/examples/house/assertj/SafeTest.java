package net.amygdalum.xrayinterface.examples.house.assertj;

import static java.util.Arrays.asList;
import static net.amygdalum.xrayinterface.IsEquivalent.isEquivalent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.hasSize;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.amygdalum.xrayinterface.IsEquivalent;
import net.amygdalum.xrayinterface.examples.house.Diamond;
import net.amygdalum.xrayinterface.examples.house.Item;
import net.amygdalum.xrayinterface.examples.house.Safe;
import net.amygdalum.xrayinterface.XRayInterface;

/**
 * the assertj pendant of {@link net.amygdalum.xrayinterface.examples.house.hamcrest.SafeTest} - the same scenarios asserted with assertjs satisfies methods and
 * xrayinterfaces {@link IsEquivalent#isEquivalent(Class)} consumers
 */
public class SafeTest {

	private Safe safe;

	@BeforeEach
	public void before() {
		safe = new Safe();
		safe.newCombination("0000", "1234");
		safe.put(new Diamond());
		safe.lock();
	}

	@Test
	public void testOwner() throws Exception {
		List<Item> items = safe.open("1234");
		assertThat(items).satisfiesExactly(item -> assertThat(item).isInstanceOf(Diamond.class));
	}

	@Test
	public void testBrute() throws Exception {
		assertThatThrownBy(() -> safe.open("4321"))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public void testPatientCombinationTester() throws Exception {
		assertThatThrownBy(() -> {
			for (int i = 0; i < 1000; i++) {
				try {
					safe.open(String.valueOf(i));
					return;
				} catch (IllegalArgumentException e) {
				}
			}
			throw new RuntimeException("giving up after 1000 combinations");
		}).isInstanceOf(RuntimeException.class);
	}

	@Test
	public void testLockpicker() throws Exception {
		TransparentSafe transparentSafe = XRayInterface.xray(safe).to(TransparentSafe.class);
		Item item = transparentSafe.getItems().remove(0);
		assertThat(item).isInstanceOf(Diamond.class);
		assertThat(transparentSafe.isLocked()).isTrue();
		assertThat(transparentSafe.getItems()).isEmpty();
	}

	@Test
	public void testMatchingSafe() throws Exception {
		assertThat(safe).satisfies(XRaySafeConsumer.isEquivalentToSafe()
			.withCode("1234")
			.withLocked(true)
			.withItems(hasSize(1)));
	}

	@Test
	public void testMatchingSafeList() throws Exception {
		List<Safe> safes = asList(safe, openSafeWithTwoItems());

		assertThat(safes).satisfiesExactly(
			XRaySafeConsumer.isEquivalentToSafe()
				.withCode("1234")
				.withLocked(true)
				.withItems(hasSize(1)),
			XRaySafeConsumer.isEquivalentToSafe()
				.withCode("0000")
				.withLocked(false)
				.withItems(hasSize(2)));
	}

	@Test
	public void testMatchingSafeListInAnyOrder() throws Exception {
		List<Safe> safes = asList(safe, openSafeWithTwoItems());

		assertThat(safes).satisfiesExactlyInAnyOrder(
			XRaySafeConsumer.isEquivalentToSafe()
				.withLocked(false)
				.withItems(hasSize(2)),
			XRaySafeConsumer.isEquivalentToSafe()
				.withLocked(true)
				.withItems(hasSize(1)));
	}

	@Test
	public void testMismatchingSafe() throws Exception {
		assertThatThrownBy(() -> assertThat(safe).satisfies(XRaySafeConsumer.isEquivalentToSafe()
			.withCode("0000")))
				.isInstanceOf(AssertionError.class)
				.hasMessageContaining("Code=1234");
	}

	private Safe openSafeWithTwoItems() {
		Safe openSafe = new Safe();
		openSafe.put(new Diamond());
		openSafe.put(new Diamond());
		return openSafe;
	}

	interface TransparentSafe {
		boolean isLocked();

		List<Item> getItems();
	}

	interface XRaySafeConsumer extends Consumer<Safe> {

		XRaySafeConsumer withCode(String code);

		XRaySafeConsumer withLocked(boolean locked);

		XRaySafeConsumer withItems(Matcher<Collection<? extends Object>> items);

		static XRaySafeConsumer isEquivalentToSafe() {
			return isEquivalent(XRaySafeConsumer.class);
		}

	}
}
