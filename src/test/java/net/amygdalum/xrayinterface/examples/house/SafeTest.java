package net.amygdalum.xrayinterface.examples.house;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import net.amygdalum.xrayinterface.XRayInterface;


public class SafeTest {


	private Safe safe;

	@Before
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

	@Test(expected=IllegalArgumentException.class)
	public void testBrute() throws Exception {
		safe.open("4321");
	}

	@Test(expected=RuntimeException.class)
	public void testPatientCombinationTester() throws Exception {
		for (int i = 0; i < 1000; i++) {
			try {
				safe.open(String.valueOf(i));
				return;
			} catch (IllegalArgumentException e) {
			}
		}
		throw new RuntimeException("giving up after 1000 combinations");
	}

	@Test
	public void testLockpicker() throws Exception {
		TransparentSafe transparentSafe = XRayInterface.xray(safe).to(TransparentSafe.class);
		Item item = transparentSafe.getItems().remove(0);
		assertThat(item, instanceOf(Diamond.class));
		assertThat(transparentSafe.isLocked(), is(true));
		assertThat(transparentSafe.getItems(), empty());
	}
	
	interface TransparentSafe {
		boolean isLocked();
		List<Item> getItems();
	}
}
