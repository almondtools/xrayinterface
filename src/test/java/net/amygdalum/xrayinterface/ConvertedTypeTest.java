package net.amygdalum.xrayinterface;

import static net.amygdalum.xrayinterface.ConvertedType.converted;
import static net.amygdalum.xrayinterface.FixedType.fixed;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import org.junit.Test;

public class ConvertedTypeTest {

	@Test
	public void testMatchedType() throws Exception {
		assertThat(converted(Other.class, Example.class).matchedType(), sameInstance(Other.class));
	}

	@Test
	public void testConvertedType() throws Exception {
		assertThat(converted(Other.class, Example.class).convertedType(), equalTo(Example.class));
	}

	@Test
	public void testMatches() throws Exception {
		assertThat(converted(Other.class, Example.class).matches(Example.class), is(false));
		assertThat(converted(Other.class, Example.class).matches(Other.class), is(true));
		assertThat(converted(Other.class, Example.class).matches(Object.class), is(false));
	}

	@Test
	public void testMatchingConverted() throws Exception {
		ConvertedType type = converted(Other.class, Example.class);
		assertThat(type.matching(Other.class), sameInstance(type));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testMatchingIllegal() throws Exception {
		ConvertedType type = converted(Other.class, Example.class);
		assertThat(type.matching(Example.class), equalTo(fixed(Example.class)));
	}
	
	@Test
	public void testEqualsHashcode() throws Exception {
		ConvertedType same = converted(Other.class, Example.class);
		assertThat(same, equalTo(same));
		assertThat(converted(Other.class, Example.class), not(equalTo(null)));
		assertThat(converted(Other.class, Example.class), not(equalTo(new Object())));
		assertThat(converted(Other.class, Example.class), equalTo(converted(Other.class, Example.class)));
		assertThat(converted(Other.class, Example.class), not(equalTo(converted(Other.class, Other.class))));
		assertThat(converted(Other.class, Example.class), not(equalTo(converted(Example.class, Example.class))));
		assertThat(converted(Other.class, Example.class).hashCode(), equalTo(converted(Other.class, Example.class).hashCode()));
	}
	
	private static class Example {
	}

	private static class Other {
	}

}
