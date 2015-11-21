package com.almondtools.xrayinterface;

import static com.almondtools.conmatch.conventions.OrdinaryExceptionMatcher.matchesOrdinaryException;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class InterfaceMismatchExceptionTest {

	@Test
	public void testInterfaceMismatchException() throws Exception {
		assertThat(InterfaceMismatchException.class, matchesOrdinaryException());
	}
	
}
