package net.amygdalum.xrayinterface;

import static net.amygdalum.extensions.hamcrest.conventions.EnumMatcher.isEnum;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class BindingQualifierTest {

	@Test
	public void testBindingQualifier() throws Exception {
		assertThat(BindingQualifier.class, isEnum());
	}
}
