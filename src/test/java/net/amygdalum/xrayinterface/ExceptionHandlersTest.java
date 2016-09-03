package net.amygdalum.xrayinterface;

import static com.almondtools.conmatch.conventions.UtilityClassMatcher.isUtilityClass;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Test;

import net.amygdalum.xrayinterface.ExceptionHandlers;

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
