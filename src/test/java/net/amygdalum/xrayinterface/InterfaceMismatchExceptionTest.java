package net.amygdalum.xrayinterface;

import static net.amygdalum.extensions.hamcrest.conventions.OrdinaryExceptionMatcher.matchesOrdinaryException;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

public class InterfaceMismatchExceptionTest {

	@Test
	public void testInterfaceMismatchException() throws Exception {
		assertThat(InterfaceMismatchException.class, matchesOrdinaryException());
	}
	
}
