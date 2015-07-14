package com.almondtools.xrayinterface;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.almondtools.xrayinterface.StaticMethodInvocationHandler;


public class StaticMethodInvocationHandlerTest  {

	@Test
	public void testNull() throws Throwable {
		assertThat(StaticMethodInvocationHandler.NULL.invoke((Object) null), nullValue());
		assertThat(StaticMethodInvocationHandler.NULL.invoke(new Object[0]), nullValue());
		assertThat(StaticMethodInvocationHandler.NULL.invoke(new Object()), nullValue());
	}
	
}
