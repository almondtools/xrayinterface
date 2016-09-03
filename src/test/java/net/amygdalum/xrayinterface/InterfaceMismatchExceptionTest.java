package net.amygdalum.xrayinterface;

import static com.almondtools.conmatch.conventions.OrdinaryExceptionMatcher.matchesOrdinaryException;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import net.amygdalum.xrayinterface.InterfaceMismatchException;

public class InterfaceMismatchExceptionTest {

	@Test
	public void testInterfaceMismatchException() throws Exception {
		assertThat(InterfaceMismatchException.class, matchesOrdinaryException());
	}
	
}
