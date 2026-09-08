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
import net.amygdalum.xrayinterface.examples.house.Furniture;
import net.amygdalum.xrayinterface.examples.house.House;
import net.amygdalum.xrayinterface.examples.house.Key;
import net.amygdalum.xrayinterface.examples.house.Safe;
import net.amygdalum.xrayinterface.XRayInterface;

/**
 * the assertj pendant of {@link net.amygdalum.xrayinterface.examples.house.hamcrest.HouseTest} - the same scenarios asserted with assertjs satisfies methods and
 * xrayinterfaces {@link IsEquivalent#isEquivalent(Class)} consumers
 */
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
		assertThat(open).isTrue();
		List<Furniture> furniture = house.listFurniture();
		assertThat(furniture).satisfiesExactly(safe -> assertThat(safe).isInstanceOf(Safe.class));
	}

	@Test
	public void testBrute() throws Exception {
		assertThatThrownBy(() -> house.listFurniture())
			.isInstanceOf(UnsupportedOperationException.class);
	}

	@Test
	public void testStranger() throws Exception {
		boolean open = house.open(new Key());
		assertThat(open).isFalse();
	}

	@Test
	public void testLockpicker() throws Exception {
		XRayHouse xrayHouse = XRayInterface.xray(house).to(XRayHouse.class);
		xrayHouse.open();
		List<Furniture> furniture = house.listFurniture();
		assertThat(furniture).satisfiesExactly(safe -> assertThat(safe).isInstanceOf(Safe.class));
	}

	@Test
	public void testAquiringHousekey() throws Exception {
		XRayHouseWithKeyGetter xrayHouse = XRayInterface.xray(house).to(XRayHouseWithKeyGetter.class);
		key = xrayHouse.getHouseKey();
		house.open(key);
		List<Furniture> furniture = house.listFurniture();
		assertThat(furniture).satisfiesExactly(safe -> assertThat(safe).isInstanceOf(Safe.class));
	}

	@Test
	public void testChangingLock() throws Exception {
		XRayHouseWithKeySetter xrayHouse = XRayInterface.xray(house).to(XRayHouseWithKeySetter.class);
		xrayHouse.setHouseKey(key);
		house.open(key);
		List<Furniture> furniture = house.listFurniture();
		assertThat(furniture).satisfiesExactly(safe -> assertThat(safe).isInstanceOf(Safe.class));
	}

	@Test
	public void testMatchingHouses() throws Exception {
		assertThat(house).satisfies(XRayHouseConsumer.isEquivalentToHouse()
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

		assertThat(houses).satisfiesExactly(
			XRayHouseConsumer.isEquivalentToHouse()
				.withHouseKey(key)
				.withLocked(true)
				.withFurniture(hasSize(1)),
			XRayHouseConsumer.isEquivalentToHouse()
				.withHouseKey(key)
				.withLocked(false)
				.withFurniture(hasSize(2)));
	}

	@Test
	public void testMatchingHouseListInAnyOrder() throws Exception {
		House openHouse = new House(key);
		openHouse.add(new Safe());
		openHouse.add(new Safe());
		List<House> houses = asList(house, openHouse);

		assertThat(houses).satisfiesExactlyInAnyOrder(
			XRayHouseConsumer.isEquivalentToHouse()
				.withLocked(false)
				.withFurniture(hasSize(2)),
			XRayHouseConsumer.isEquivalentToHouse()
				.withLocked(true)
				.withFurniture(hasSize(1)));
	}

	@Test
	public void testMismatchingHouse() throws Exception {
		assertThatThrownBy(() -> assertThat(house).satisfies(XRayHouseConsumer.isEquivalentToHouse()
			.withLocked(false)))
				.isInstanceOf(AssertionError.class)
				.hasMessageContaining("Locked=");
	}

	@Test
	public void testPreventRuntimeErrorsOnXRaying() throws Exception {
		assertThat(XRayInterface.xray(House.class).unMappable(XRayHouse.class)).isEmpty();
		assertThat(XRayInterface.xray(House.class).unMappable(XRayHouseWithKeyGetter.class)).isEmpty();
		assertThat(XRayInterface.xray(House.class).unMappable(XRayHouseWithKeySetter.class)).isEmpty();
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

	interface XRayHouseConsumer extends Consumer<House> {

		XRayHouseConsumer withHouseKey(Key key);

		XRayHouseConsumer withLocked(boolean locked);

		XRayHouseConsumer withFurniture(Matcher<Collection<? extends Object>> furniture);

		static XRayHouseConsumer isEquivalentToHouse() {
			return isEquivalent(XRayHouseConsumer.class);
		}

	}
}
