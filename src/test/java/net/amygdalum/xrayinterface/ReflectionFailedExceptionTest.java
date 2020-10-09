package net.amygdalum.xrayinterface;

import static net.amygdalum.extensions.hamcrest.conventions.OrdinaryExceptionMatcher.matchesOrdinaryException;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

public class ReflectionFailedExceptionTest {

	@Test
	public void testReflectionFailedException() throws Exception {
		assertThat(ReflectionFailedException.class, matchesOrdinaryException());
	}

}
