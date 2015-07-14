package com.almondtools.xrayinterface.examples.innerclass;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.almondtools.xrayinterface.Convert;
import com.almondtools.xrayinterface.ObjectAccess;
import com.almondtools.xrayinterface.InterfaceMismatchException;

public class ExampleObjectTest {

	@Test
	public void testInnerStaticClassResult() throws Exception {
		ExampleObject exampleObject = new ExampleObject("state");
		UnlockedExampleObject unlockedExampleObject = ObjectAccess.xray(exampleObject).to(UnlockedExampleObject.class);
		InnerStatic s = unlockedExampleObject.createInnerStatic();
		assertThat(s.getState(), equalTo("state"));
		assertThat(s.isBooleanState(), is(false));
	}

	@Test
	public void testInnerStaticClassArgument() throws Exception {
		ExampleObject exampleObject = new ExampleObject("state");
		UnlockedExampleObject unlockedExampleObject = ObjectAccess.xray(exampleObject).to(UnlockedExampleObject.class);
		assertThat(unlockedExampleObject.useInnerStatic(new InnerStatic() {

			@Override
			public boolean isBooleanState() {
				return false;
			}

			@Override
			public String getState() {
				return null;
			}

			@Override
			public void setState(String state) {
			}
		}, ""), is(false));
		assertThat(unlockedExampleObject.useInnerStatic(new InnerStatic() {

			@Override
			public boolean isBooleanState() {
				return false;
			}

			@Override
			public String getState() {
				return "state";
			}

			@Override
			public void setState(String state) {
			}
		}, ""), is(true));
	}

	@Test(expected = InterfaceMismatchException.class)
	public void testInnerStaticMappingExceptionOnResult() throws Exception {
		ExampleObject exampleObject = new ExampleObject("state");
		ObjectAccess.xray(exampleObject).to(UnlockedExampleExceptionResult.class);
	}

	@Test(expected = InterfaceMismatchException.class)
	public void testInnerStaticMappingExceptionOnParams() throws Exception {
		ExampleObject exampleObject = new ExampleObject("state");
		ObjectAccess.xray(exampleObject).to(UnlockedExampleExceptionParam.class);
	}

	@Test
	public void testInnerStaticRoundtrip() throws Exception {
		ExampleObject exampleObject = new ExampleObject("state");
		UnlockedExampleOther unlockedExampleObject = ObjectAccess.xray(exampleObject).to(UnlockedExampleOther.class);
		InnerStaticOther s = unlockedExampleObject.createInnerStatic();
		assertThat(unlockedExampleObject.useInnerStatic(s, ""), is(true));
	}

	@Test
	public void testInnerStaticGetter() throws Exception {
		ExampleObject exampleObject = new ExampleObject("stateForGetter");
		UnlockedExampleGetSetter unlockedExampleObject = ObjectAccess.xray(exampleObject).to(UnlockedExampleGetSetter.class);
		assertThat(unlockedExampleObject.getFieldInnerStatic().getState(), equalTo("stateForGetter"));
	}

	@Test
	public void testInnerStaticSetter() throws Exception {
		ExampleObject exampleObject = new ExampleObject("stateForSetter");
		UnlockedExampleGetSetter unlockedExampleObject = ObjectAccess.xray(exampleObject).to(UnlockedExampleGetSetter.class);
		unlockedExampleObject.setFieldInnerStatic(new InnerStatic() {

			@Override
			public boolean isBooleanState() {
				return false;
			}

			@Override
			public String getState() {
				return "newState";
			}

			@Override
			public void setState(String state) {
			}

		});
		assertThat(unlockedExampleObject.getFieldInnerStatic().getState(), equalTo("newState"));
	}

	interface UnlockedExampleObject {
		@Convert
		InnerStatic createInnerStatic();

		boolean useInnerStatic(@Convert InnerStatic arg, String s);

	}

	interface UnlockedExampleGetSetter {

		@Convert
		InnerStatic getFieldInnerStatic();

		void setFieldInnerStatic(@Convert InnerStatic field);
	}

	interface InnerStatic {
		boolean isBooleanState();

		String getState();

		void setState(String state);
	}

	interface UnlockedExampleOther {
		@Convert("InnerStatic")
		InnerStaticOther createInnerStatic();

		boolean useInnerStatic(@Convert("InnerStatic") InnerStaticOther arg, String s);

	}

	interface InnerStaticOther {
	}

	interface UnlockedExampleExceptionParam {

		boolean useInnerStatic(@Convert InnerStaticException arg);

	}

	interface UnlockedExampleExceptionResult {

		@Convert
		InnerStaticException createInnerStatic();

	}

	interface InnerStaticException {
	}

}
