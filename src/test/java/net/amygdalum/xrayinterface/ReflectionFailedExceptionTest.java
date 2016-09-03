package net.amygdalum.xrayinterface;

import static com.almondtools.conmatch.conventions.OrdinaryExceptionMatcher.matchesOrdinaryException;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import net.amygdalum.xrayinterface.ReflectionFailedException;

public class ReflectionFailedExceptionTest {

	@Test
	public void testReflectionFailedException() throws Exception {
		assertThat(ReflectionFailedException.class, matchesOrdinaryException());
	}

}
