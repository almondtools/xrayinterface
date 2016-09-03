package net.amygdalum.xrayinterface;

import static com.almondtools.conmatch.conventions.EnumMatcher.isEnum;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import net.amygdalum.xrayinterface.BindingQualifier;

public class BindingQualifierTest {

	@Test
	public void testBindingQualifier() throws Exception {
		assertThat(BindingQualifier.class, isEnum());
	}
}
