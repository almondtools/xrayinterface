package com.almondtools.xrayinterface;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

import org.junit.Before;
import org.junit.Test;

public class StaticPropertyTest {

	private MethodHandles.Lookup lookup;
	private MethodHandle get;
	private MethodHandle set;
	
	private static String field;

	@Before
	public void before() throws Exception {
		this.lookup = MethodHandles.lookup();
		Field field = StaticPropertyTest.class.getDeclaredField("field");
		this.get = lookup.unreflectGetter(field);
		this.set = lookup.unreflectSetter(field);
	}

	@Test
	public void testSet() throws Throwable {
		StaticProperty property = staticProperty(new StaticSetter("field", set), null);

		property.set().invoke(null, "valueFromSet");

		assertThat(field, equalTo("valueFromSet"));
	}

	@Test
	public void testGet() throws Throwable {
		field = "valueForGet";
		StaticProperty property = staticProperty(null, new StaticGetter("field", get));

		Object result = property.get().invoke(null);

		assertThat(result, equalTo("valueForGet"));
	}

	@Test
	public void testIsReadable() throws Exception {
		assertThat(staticProperty(new StaticSetter("field", set), new StaticGetter("field", get)).isReadable(), is(true));
		assertThat(staticProperty(new StaticSetter("field", set), null).isReadable(), is(false));
		assertThat(staticProperty(null, new StaticGetter("field", get)).isReadable(), is(true));
	}

	@Test
	public void testIsWritable() throws Exception {
		assertThat(staticProperty(new StaticSetter("field", set), new StaticGetter("field", get)).isWritable(), is(true));
		assertThat(staticProperty(new StaticSetter("field", set), null).isWritable(), is(true));
		assertThat(staticProperty(null, new StaticGetter("field", get)).isWritable(), is(false));
	}

	private StaticProperty staticProperty(StaticSetter set, StaticGetter get) {
		StaticProperty property = new StaticProperty();
		property.setSetter(set);
		property.setGetter(get);
		return property;
	}

}
