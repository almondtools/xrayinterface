Creating Powerful JUnit Matchers and Assertions
===============================================
If you are strongly familiar with unit testing you probably faced the problem that you got a result object with a complex hidden inner state (i.e. many variables without accessors). Sometimes one can test the inner state by calling other methods (relying on the inner state), but often this makes testing even more complicated. So how can XRayInterface help you testing such objects?

XRayInterface answers this with `IsEquivalent`, a property based assertion that is built from an interface you write yourself. It comes in two flavours - a Hamcrest `Matcher` and an AssertJ compatible `Consumer`. Both are backed by the same engine, so everything you learn about one applies to the other.

Hamcrest: `equivalentTo`
------------------------
Look at this test for the class `House` from this [example](OpeningASealedClass.md):

```Java
	@Test
	public void testMatchingHouses() throws Exception {
		assertThat(house, IsEquivalent.equivalentTo(XRayMatcher.class)
			.withHouseKey(key)
			.withLocked(true)
			.withFurniture(hasSize(1)));
	}
	
	interface XRayMatcher extends Matcher<House> {

		XRayMatcher withHouseKey(Key key);
		XRayMatcher withLocked(boolean locked);
		XRayMatcher withFurniture(Matcher<Collection<? extends Object>> furniture);
	}
```

As you can see:

- define a `Matcher` for the object of interest
- give it some Builder methods (similar to setter methods but the return type is your matcher and it is not `set` but `with`)
- a builder methods parameter 
  - may be the expected value of the assigned property
  - or a value matcher that should be applied to the assigned property

AssertJ: `isEquivalent`
-----------------------
If your project asserts with AssertJ you do not want to fall back to `org.hamcrest.MatcherAssert.assertThat` for one assertion. Let your interface extend `java.util.function.Consumer` instead of `Matcher` and build it with `IsEquivalent.isEquivalent`:

```Java
	@Test
	public void testMatchingHouses() throws Exception {
		assertThat(house).satisfies(IsEquivalent.isEquivalent(XRayHouse.class)
			.withHouseKey(key)
			.withLocked(true)
			.withFurniture(furniture -> assertThat(furniture).hasSize(1)));
	}

	interface XRayHouse extends Consumer<House> {

		XRayHouse withHouseKey(Key key);
		XRayHouse withLocked(boolean locked);
		XRayHouse withFurniture(Consumer<Collection<Furniture>> furniture);
	}
```

The resulting `Consumer` throws an `AssertionError` for non matching objects, which is exactly the contract AssertJ expects from a requirement. So it fits everywhere AssertJ takes a `Consumer`:

- `assertThat(object).satisfies(...)` for a single object
- `assertThat(collection).satisfiesExactly(...)` for a list - one requirement per element, in order
- `assertThat(collection).satisfiesExactlyInAnyOrder(...)` - one requirement per element, order irrelevant

```Java
	@Test
	public void testMatchingHouseList() throws Exception {
		List<House> houses = asList(lockedHouse, openHouse);

		assertThat(houses).satisfiesExactly(
			isEquivalent(XRayHouse.class)
				.withLocked(true)
				.withFurniture(furniture -> assertThat(furniture).hasSize(1)),
			isEquivalent(XRayHouse.class)
				.withLocked(false)
				.withFurniture(furniture -> assertThat(furniture).hasSize(2)));
	}
```

These are the pendants of the Hamcrest `contains` and `containsInAnyOrder`:

| flavour  | build with      | interface extends | single object                  | list (ordered)                  | list (any order)                           |
| -------- | --------------- | ----------------- | ------------------------------ | ------------------------------- | ------------------------------------------ |
| Hamcrest | `equivalentTo`  | `Matcher<T>`      | `assertThat(house, matcher)`   | `contains(m1, m2)`              | `containsInAnyOrder(m1, m2)`               |
| AssertJ  | `isEquivalent`  | `Consumer<T>`     | `assertThat(house).satisfies(c)` | `satisfiesExactly(c1, c2)`    | `satisfiesExactlyInAnyOrder(c1, c2)`       |

What a builder method may take
------------------------------
The parameter of a `withXxx` method is the expectation for that property. Four kinds are supported - in **both** flavours, because both share the same matching engine:

```Java
	interface XRaySafe extends Consumer<Safe> {

		XRaySafe withCode(String code);                              // 1. a plain value, compared with equals
		XRaySafe withLocked(Matcher<? super Boolean> locked);        // 2. a hamcrest matcher
		XRaySafe withItems(Consumer<Collection<Item>> items);        // 3. a consumer (e.g. an assertj assertion)
		XRaySafe withOwner(XRayPerson owner);                        // 4. another equivalence builder
	}
```

1. **A plain value** - the property must be `equals` to it (`null` matches a `null` property).
2. **A hamcrest matcher** - applied to the property. Perfectly legal in the AssertJ flavour too, which is why `withFurniture(hasSize(1))` still works there.
3. **A `Consumer`** - satisfied as long as it throws no `AssertionError`. This is where an AssertJ assertion on a single property goes. Declare the property as `Consumer<PropertyType>` and pass a lambda:

```Java
	interface XRaySafeConsumers extends Consumer<Safe> {

		XRaySafeConsumers withCode(Consumer<String> code);
		XRaySafeConsumers withItems(Consumer<Collection<Item>> items);
	}

	assertThat(safe).satisfies(isEquivalent(XRaySafeConsumers.class)
		.withCode(code -> assertThat(code).startsWith("12"))
		.withItems(items -> assertThat(items).hasSize(1)));
```

4. **Another `equivalentTo`/`isEquivalent` builder** - see the next section.

Nesting equivalences
--------------------
A property that is itself an object with hidden state is asserted with its own builder. Declare the nested builder interface as the parameter type:

```Java
	interface XRayHouse extends Consumer<House> {

		XRayHouse withHouseKey(XRayKey houseKey);
		XRayHouse withLocked(boolean locked);
	}

	interface XRayKey extends Consumer<Key> {

		XRayKey withId(double id);
	}

	@Test
	public void testMatchingNestedKey() throws Exception {
		assertThat(house).satisfies(isEquivalent(XRayHouse.class)
			.withHouseKey(isEquivalent(XRayKey.class)
				.withId(expectedId))
			.withLocked(true));
	}
```

Nested builders are unwrapped when they are recorded, so they describe themselves in the failure message instead of showing up as an opaque proxy. Combine this with the collection consumer of the previous section to descend into collections of hidden objects:

```Java
		.withFurniture(furniture -> assertThat(furniture).satisfiesExactly(
			isEquivalent(XRaySafe.class).withCode("1234")))
```

The same nesting works in the Hamcrest flavour - there the nested builder extends `Matcher`, and collections of hidden objects are descended into with `contains`/`containsInAnyOrder` instead of a collection consumer.

A static factory keeps the tests readable
-----------------------------------------
Building the expectation with `isEquivalent(XRayHouse.class)` at every call site is noisy. Put a static factory method on the interface itself - it is a `static` method, so XRayInterface does not try to bind it to a property:

```Java
	interface XRayHouse extends Consumer<House> {

		XRayHouse withHouseKey(Key houseKey);
		XRayHouse withLocked(boolean locked);
		XRayHouse withFurniture(Consumer<Collection<Furniture>> furniture);

		static XRayHouse house() {
			return isEquivalent(XRayHouse.class);
		}
	}
```

The tests then read as plain sentences:

```Java
	assertThat(houses).satisfiesExactly(
		house().withLocked(true).withFurniture(furniture -> assertThat(furniture).hasSize(1)),
		house().withLocked(false).withFurniture(furniture -> assertThat(furniture).hasSize(2)));
```

The factory may even prefill the properties that identify the object:

```Java
		static TreeNodeConsumer treeNodeWithId(String id) {
			return isEquivalent(TreeNodeConsumer.class).withId(id);
		}
```

```Java
	assertThat(root.getChildren()).satisfiesExactly(
		treeNodeWithId("a").withChildren(hasSize(2)),
		treeNodeWithId("d").withChildren(hasSize(0)));
```

Failure messages
----------------
A mismatch reports the expected and the found properties side by side, so you see the complete hidden state and not only the first deviation:

```
	expected object with properties <I=42>, <Str=str>
	but found object with properties <I=4711>, <Str=other>
```

A property that does not exist on the asserted object at all is reported as `<missing>` - which is how a renamed field shows up. To catch that earlier, see [Maintaining and Tracking XRayInterfaces](MaintainingAndTrackingXRayInterfaces.md).

Choosing a flavour
------------------
Both flavours are equally powerful, so pick the one matching the assertion library of the surrounding test. Do not mix them in one assertion: an interface extending `Matcher` must be built with `equivalentTo`, an interface extending `Consumer` with `isEquivalent`. Inside the expectations you may mix freely - hamcrest matchers, AssertJ consumers and nested builders live next to each other in both worlds.

The worked examples for both flavours are in the test sources under `net.amygdalum.xrayinterface.examples`, in a `hamcrest` and an `assertj` sub package each covering the same scenarios.
