package net.amygdalum.xrayinterface.examples.house.hamcrest;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collection;
import java.util.List;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.amygdalum.xrayinterface.IsEquivalent;
import net.amygdalum.xrayinterface.examples.house.Diamond;
import net.amygdalum.xrayinterface.examples.house.Item;
import net.amygdalum.xrayinterface.examples.house.Safe;
import net.amygdalum.xrayinterface.XRayInterface;


public class SafeTest {


	private Safe safe;

	@BeforeEach
	public void before() {
		safe = new Safe();
		safe.newCombination("0000","1234");
		safe.put(new Diamond());
		safe.lock();
	}

	@Test
	public void testOwner() throws Exception {
		List<Item> items = safe.open("1234");
		assertThat(items, contains(instanceOf(Diamond.class)));
	}

	@Test
	public void testBrute() throws Exception {
		assertThrows(IllegalArgumentException.class, () -> {
			safe.open("4321");
		});
	}

	@Test
	public void testPatientCombinationTester() throws Exception {
		assertThrows(RuntimeException.class, () -> {
			for (int i = 0; i < 1000; i++) {
				try {
					safe.open(String.valueOf(i));
					return;
				} catch (IllegalArgumentException e) {
				}
			}
			throw new RuntimeException("giving up after 1000 combinations");
		});
	}

	@Test
	public void testLockpicker() throws Exception {
		TransparentSafe transparentSafe = XRayInterface.xray(safe).to(TransparentSafe.class);
		Item item = transparentSafe.getItems().remove(0);
		assertThat(item, instanceOf(Diamond.class));
		assertThat(transparentSafe.isLocked(), is(true));
		assertThat(transparentSafe.getItems(), empty());
	}

	@Test
	public void testMatchingSafe() throws Exception {
		assertThat(safe, XRaySafeMatcher.matchesSafe()
			.withCode("1234")
			.withLocked(true)
			.withItems(hasSize(1)));
	}

	@Test
	public void testMatchingSafeList() throws Exception {
		List<Safe> safes = asList(safe, openSafeWithTwoItems());

		assertThat(safes, contains(
			XRaySafeMatcher.matchesSafe()
				.withCode("1234")
				.withLocked(true)
				.withItems(hasSize(1)),
			XRaySafeMatcher.matchesSafe()
				.withCode("0000")
				.withLocked(false)
				.withItems(hasSize(2))));
	}

	@Test
	public void testMatchingSafeListInAnyOrder() throws Exception {
		List<Safe> safes = asList(safe, openSafeWithTwoItems());

		assertThat(safes, containsInAnyOrder(
			XRaySafeMatcher.matchesSafe()
				.withLocked(false)
				.withItems(hasSize(2)),
			XRaySafeMatcher.matchesSafe()
				.withLocked(true)
				.withItems(hasSize(1))));
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

	interface XRaySafeMatcher extends Matcher<Safe> {

		XRaySafeMatcher withCode(String code);

		XRaySafeMatcher withLocked(boolean locked);

		XRaySafeMatcher withItems(Matcher<Collection<? extends Object>> items);

		static XRaySafeMatcher matchesSafe() {
			return IsEquivalent.equivalentTo(XRaySafeMatcher.class);
		}

	}
}
