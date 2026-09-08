package net.amygdalum.xrayinterface.examples.house.hamcrest;

import static java.util.Arrays.asList;
import static net.amygdalum.xrayinterface.XRayMatcher.providesFeaturesOf;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collection;
import java.util.List;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.amygdalum.xrayinterface.IsEquivalent;
import net.amygdalum.xrayinterface.examples.house.Furniture;
import net.amygdalum.xrayinterface.examples.house.House;
import net.amygdalum.xrayinterface.examples.house.Key;
import net.amygdalum.xrayinterface.examples.house.Safe;
import net.amygdalum.xrayinterface.XRayInterface;

public class HouseTest {

	private Key key;
	private House house;

	@BeforeEach
	public void before() {
		key = new Key();
		house = new House(key);
		house.add(new Safe());
		house.lock(key);
	}

	@Test
	public void testHouseOwner() throws Exception {
		boolean open = house.open(key);
		assertThat(open, is(true));
		List<Furniture> furniture = house.listFurniture();
		assertThat(furniture, contains(instanceOf(Safe.class)));
	}

	@Test
	public void testBrute() throws Exception {
		assertThrows(UnsupportedOperationException.class, () -> {
			house.listFurniture();
		});
	}

	@Test
	public void testStranger() throws Exception {
		boolean open = house.open(new Key());
		assertThat(open, is(false));
	}

	@Test
	public void testLockpicker() throws Exception {
		XRayHouse xrayHouse = XRayInterface.xray(house).to(XRayHouse.class);
		xrayHouse.open();
		List<Furniture> furniture = house.listFurniture();
		assertThat(furniture, contains(instanceOf(Safe.class)));
	}

	@Test
	public void testAquiringHousekey() throws Exception {
		XRayHouseWithKeyGetter xrayHouse = XRayInterface.xray(house).to(XRayHouseWithKeyGetter.class);
		key = xrayHouse.getHouseKey();
		house.open(key);
		List<Furniture> furniture = house.listFurniture();
		assertThat(furniture, contains(instanceOf(Safe.class)));
	}

	@Test
	public void testChangingLock() throws Exception {
		XRayHouseWithKeySetter xrayHouse = XRayInterface.xray(house).to(XRayHouseWithKeySetter.class);
		xrayHouse.setHouseKey(key);
		house.open(key);
		List<Furniture> furniture = house.listFurniture();
		assertThat(furniture, contains(instanceOf(Safe.class)));
	}

	@Test
	public void testMatchingHouses() throws Exception {
		assertThat(house, XRayHouseMatcher.matchesHouse()
			.withHouseKey(key)
			.withLocked(true)
			.withFurniture(hasSize(1)));
	}

	@Test
	public void testMatchingHouseList() throws Exception {
		House openHouse = new House(key);
		openHouse.add(new Safe());
		openHouse.add(new Safe());
		List<House> houses = asList(house, openHouse);

		assertThat(houses, contains(
			XRayHouseMatcher.matchesHouse()
				.withHouseKey(key)
				.withLocked(true)
				.withFurniture(hasSize(1)),
			XRayHouseMatcher.matchesHouse()
				.withHouseKey(key)
				.withLocked(false)
				.withFurniture(hasSize(2))));
	}

	@Test
	public void testMatchingHouseListInAnyOrder() throws Exception {
		House openHouse = new House(key);
		openHouse.add(new Safe());
		openHouse.add(new Safe());
		List<House> houses = asList(house, openHouse);

		assertThat(houses, containsInAnyOrder(
			XRayHouseMatcher.matchesHouse()
				.withLocked(false)
				.withFurniture(hasSize(2)),
			XRayHouseMatcher.matchesHouse()
				.withLocked(true)
				.withFurniture(hasSize(1))));
	}

	@Test
	public void testPreventRuntimeErrorsOnXRaying() throws Exception {
		assertThat(House.class, providesFeaturesOf(XRayHouse.class));
		assertThat(House.class, providesFeaturesOf(XRayHouseWithKeyGetter.class));
		assertThat(House.class, providesFeaturesOf(XRayHouseWithKeySetter.class));
	}

	interface XRayHouse {
		void open();
	}

	interface XRayHouseWithKeyGetter {
		Key getHouseKey();
	}

	interface XRayHouseWithKeySetter {
		void setHouseKey(Key key);
	}

	interface XRayHouseMatcher extends Matcher<House> {

		XRayHouseMatcher withHouseKey(Key key);
		XRayHouseMatcher withLocked(boolean locked);
		XRayHouseMatcher withFurniture(Matcher<Collection<? extends Object>> furniture);
		
		static XRayHouseMatcher matchesHouse() {
			return IsEquivalent.equivalentTo(XRayHouseMatcher.class);
		}
		
	}
}
