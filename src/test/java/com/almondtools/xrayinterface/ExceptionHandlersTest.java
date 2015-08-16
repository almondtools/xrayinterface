package com.almondtools.xrayinterface;

import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Test;

public class ExceptionHandlersTest {

	@Test
	public void testReturnNull() throws Exception {
		assertThat(ExceptionHandlers.RETURN_NULL.apply(null), nullValue());
		assertThat(ExceptionHandlers.RETURN_NULL.apply(new IOException()), nullValue());
		assertThat(ExceptionHandlers.RETURN_NULL.apply(new RuntimeException()), nullValue());
	}
}
