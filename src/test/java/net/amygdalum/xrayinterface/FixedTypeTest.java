package net.amygdalum.xrayinterface;

import static net.amygdalum.xrayinterface.FixedType.fixed;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

public class FixedTypeTest {

	@Test
	public void testMatchedType() throws Exception {
		assertThat(fixed(Example.class).matchedType(), equalTo(Example.class));
	}

	@Test
	public void testConvertedType() throws Exception {
		assertThat(fixed(Example.class).convertedType(), equalTo(Example.class));
	}

	@Test
	public void testMatches() throws Exception {
		assertThat(fixed(Example.class).matches(Example.class), is(true));
		assertThat(fixed(Example.class).matches(Other.class), is(false));
	}

	@Test
	public void testMatching() throws Exception {
		Type type = fixed(Example.class);
		assertThat(type.matching(Example.class), sameInstance(type));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testMatchingIllegal() throws Exception {
		fixed(Example.class).matching(Other.class);
	}
	
	private static class Example {
	}

	private static class Other {
	}

}
