package net.amygdalum.xrayinterface;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;

import org.junit.Before;
import org.junit.Test;

public class FieldPropertyTest {

	private MethodHandle get;
	private MethodHandle set;
	
	private String field;

	@Before
	public void before() throws Exception {
		Lookup lookup = MethodHandles.lookup();
		Field field = FieldPropertyTest.class.getDeclaredField("field");
		this.get = lookup.unreflectGetter(field);
		this.set = lookup.unreflectSetter(field);
	}

	@Test
	public void testSet() throws Throwable {
		FieldProperty property = fieldProperty(new FieldSetter("field", set), null);

		property.set().invoke(this, "valueFromSet");

		assertThat(field, equalTo("valueFromSet"));
	}

	@Test
	public void testGet() throws Throwable {
		field = "valueForGet";
		FieldProperty property = fieldProperty(null, new FieldGetter("field", get));

		Object result = property.get().invoke(this);

		assertThat(result, equalTo("valueForGet"));
	}

	@Test
	public void testIsReadable() throws Exception {
		assertThat(fieldProperty(new FieldSetter("field", set), new FieldGetter("field", get)).isReadable(), is(true));
		assertThat(fieldProperty(new FieldSetter("field", set), null).isReadable(), is(false));
		assertThat(fieldProperty(null, new FieldGetter("field", get)).isReadable(), is(true));
	}

	@Test
	public void testIsWritable() throws Exception {
		assertThat(fieldProperty(new FieldSetter("field", set), new FieldGetter("field", get)).isWritable(), is(true));
		assertThat(fieldProperty(new FieldSetter("field", set), null).isWritable(), is(true));
		assertThat(fieldProperty(null, new FieldGetter("field", get)).isWritable(), is(false));
	}

	private FieldProperty fieldProperty(FieldSetter set, FieldGetter get) {
		FieldProperty property = new FieldProperty();
		property.setSetter(set);
		property.setGetter(get);
		return property;
	}

}
