package net.amygdalum.xrayinterface;

import static net.amygdalum.extensions.hamcrest.conventions.UtilityClassMatcher.isUtilityClass;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;

import java.io.IOException;

import org.junit.Test;

public class ExceptionHandlersTest {

	@Test
	public void testExceptionHandlers() throws Exception {
		assertThat(ExceptionHandlers.class, isUtilityClass());
	}

	@Test
	public void testReturnNull() throws Exception {
		assertThat(ExceptionHandlers.RETURN_NULL.apply(null), nullValue());
		assertThat(ExceptionHandlers.RETURN_NULL.apply(new IOException()), nullValue());
		assertThat(ExceptionHandlers.RETURN_NULL.apply(new RuntimeException()), nullValue());
	}

}
