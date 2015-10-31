package com.almondtools.xrayinterface;

import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.almondtools.conmatch.conventions.EnumMatcher;

public class BindingQualifierTest {

	@Test
	public void testBindingQualifier() throws Exception {
		assertThat(BindingQualifier.class, EnumMatcher.isEnum());
	}
	
}
