package com.almondtools.xrayinterface;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.almondtools.xrayinterface.BoxingUtil;


public class BoxingUtilTest {

	@SuppressWarnings("rawtypes")
	@Test
	public void testGetBoxed() throws Exception {
		assertThat(BoxingUtil.getBoxed(byte.class), equalTo((Class) Byte.class));
		assertThat(BoxingUtil.getBoxed(char.class), equalTo((Class) Character.class));
		assertThat(BoxingUtil.getBoxed(short.class), equalTo((Class) Short.class));
		assertThat(BoxingUtil.getBoxed(int.class), equalTo((Class) Integer.class));
		assertThat(BoxingUtil.getBoxed(long.class), equalTo((Class) Long.class));
		assertThat(BoxingUtil.getBoxed(float.class), equalTo((Class) Float.class));
		assertThat(BoxingUtil.getBoxed(double.class), equalTo((Class) Double.class));
		assertThat(BoxingUtil.getBoxed(boolean.class), equalTo((Class) Boolean.class));
		assertThat(BoxingUtil.getBoxed(void.class), equalTo((Class) Void.class));
		assertThat(BoxingUtil.getBoxed(String.class), equalTo((Class) String.class));
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void testGetUnBoxed() throws Exception {
		assertThat(BoxingUtil.getUnboxed(Byte.class), equalTo((Class) byte.class));
		assertThat(BoxingUtil.getUnboxed(Character.class), equalTo((Class) char.class));
		assertThat(BoxingUtil.getUnboxed(Short.class), equalTo((Class) short.class));
		assertThat(BoxingUtil.getUnboxed(Integer.class), equalTo((Class) int.class));
		assertThat(BoxingUtil.getUnboxed(Long.class), equalTo((Class) long.class));
		assertThat(BoxingUtil.getUnboxed(Float.class), equalTo((Class) float.class));
		assertThat(BoxingUtil.getUnboxed(Double.class), equalTo((Class) double.class));
		assertThat(BoxingUtil.getUnboxed(Boolean.class), equalTo((Class) boolean.class));
		assertThat(BoxingUtil.getUnboxed(Void.class), equalTo((Class) void.class));
		assertThat(BoxingUtil.getUnboxed(String.class), equalTo((Class) String.class));
	}

}
