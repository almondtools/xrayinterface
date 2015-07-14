Creating Powerful JUnit/Hamcrest Matchers
=========================================
If you are strongly familiar with unit testing you probably faced the problem that you got a result object with a complex hidden inner state (i.e. many variables without accessors). Sometimes one can test the inner state by calling other methods (relying on the inner state), but often this makes testing even more complicated. So how can XRayInterface help you testing such objects.

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
