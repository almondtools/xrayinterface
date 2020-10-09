package net.amygdalum.xrayinterface;

import static net.amygdalum.extensions.hamcrest.conventions.EnumMatcher.isEnum;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

public class BindingQualifierTest {

	@Test
	public void testBindingQualifier() throws Exception {
		assertThat(BindingQualifier.class, isEnum());
	}
}
