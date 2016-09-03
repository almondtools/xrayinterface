package net.amygdalum.xrayinterface;

import static net.amygdalum.xrayinterface.ConvertedType.converted;
import static net.amygdalum.xrayinterface.FixedType.fixed;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import net.amygdalum.xrayinterface.MatchType;

public class MatchTypeTest {

	@Test
	public void testMatchedType() throws Exception {
		assertThat(new MatchType("Other", Example.class).matchedType(), nullValue());
	}

	@Test
	public void testConvertedType() throws Exception {
		assertThat(new MatchType("Other", Example.class).convertedType(), equalTo(Example.class));
	}

	@Test
	public void testMatches() throws Exception {
		assertThat(new MatchType("Other", Example.class).matches(Example.class), is(true));
		assertThat(new MatchType("Other", Example.class).matches(Other.class), is(true));
		assertThat(new MatchType("Other", Example.class).matches(Object.class), is(false));
	}

	@Test
	public void testMatching() throws Exception {
		MatchType type = new MatchType("Other", Example.class);
		assertThat(type.matching(Example.class), equalTo(fixed(Example.class)));
	}
	
	@Test
	public void testMatchingConverted() throws Exception {
		MatchType type = new MatchType("Other", Example.class);
		assertThat(type.matching(Other.class), equalTo(converted(Other.class, Example.class)));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testMatchingIllegal() throws Exception {
		MatchType type = new MatchType("Other", Example.class);
		type.matching(Object.class);
	}
	
	private static class Example {
	}

	private static class Other {
	}

}
