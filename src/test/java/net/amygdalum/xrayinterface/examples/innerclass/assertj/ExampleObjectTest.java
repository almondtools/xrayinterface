package net.amygdalum.xrayinterface.examples.innerclass.assertj;

import static java.util.Arrays.asList;
import static net.amygdalum.xrayinterface.IsEquivalent.isEquivalent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import net.amygdalum.xrayinterface.Convert;
import net.amygdalum.xrayinterface.InterfaceMismatchException;
import net.amygdalum.xrayinterface.IsEquivalent;
import net.amygdalum.xrayinterface.XRayInterface;
import net.amygdalum.xrayinterface.examples.innerclass.ExampleObject;

/**
 * the assertj pendant of {@link net.amygdalum.xrayinterface.examples.innerclass.hamcrest.ExampleObjectTest} - the same
 * scenarios asserted with assertjs satisfies methods and xrayinterfaces {@link IsEquivalent#isEquivalent(Class)}
 * consumers
 */
public class ExampleObjectTest {

	@Test
	public void testInnerStaticClassResult() throws Exception {
		ExampleObject exampleObject = new ExampleObject("state");
		UnlockedExampleObject unlockedExampleObject = XRayInterface.xray(exampleObject).to(UnlockedExampleObject.class);
		InnerStatic s = unlockedExampleObject.createInnerStatic();
		assertThat(s.getState()).isEqualTo("state");
		assertThat(s.isBooleanState()).isFalse();
	}

	@Test
	public void testInnerStaticClassArgument() throws Exception {
		ExampleObject exampleObject = new ExampleObject("state");
		UnlockedExampleObject unlockedExampleObject = XRayInterface.xray(exampleObject).to(UnlockedExampleObject.class);
		assertThat(unlockedExampleObject.useInnerStatic(innerStaticWithState(null), "")).isFalse();
		assertThat(unlockedExampleObject.useInnerStatic(innerStaticWithState("state"), "")).isTrue();
	}

	@Test
	public void testInnerStaticMappingExceptionOnResult() throws Exception {
		ExampleObject exampleObject = new ExampleObject("state");

		assertThatThrownBy(() -> XRayInterface.xray(exampleObject).to(UnlockedExampleExceptionResult.class))
			.isInstanceOf(InterfaceMismatchException.class);
	}

	@Test
	public void testInnerStaticMappingExceptionOnParams() throws Exception {
		ExampleObject exampleObject = new ExampleObject("state");

		assertThatThrownBy(() -> XRayInterface.xray(exampleObject).to(UnlockedExampleExceptionParam.class))
			.isInstanceOf(InterfaceMismatchException.class);
	}

	@Test
	public void testInnerStaticRoundtrip() throws Exception {
		ExampleObject exampleObject = new ExampleObject("state");
		UnlockedExampleOther unlockedExampleObject = XRayInterface.xray(exampleObject).to(UnlockedExampleOther.class);
		InnerStaticOther s = unlockedExampleObject.createInnerStatic();
		assertThat(unlockedExampleObject.useInnerStatic(s, "")).isTrue();
	}

	@Test
	public void testInnerStaticGetter() throws Exception {
		ExampleObject exampleObject = new ExampleObject("stateForGetter");
		UnlockedExampleGetSetter unlockedExampleObject = XRayInterface.xray(exampleObject).to(UnlockedExampleGetSetter.class);
		assertThat(unlockedExampleObject.getFieldInnerStatic().getState()).isEqualTo("stateForGetter");
	}

	@Test
	public void testInnerStaticSetter() throws Exception {
		ExampleObject exampleObject = new ExampleObject("stateForSetter");
		UnlockedExampleGetSetter unlockedExampleObject = XRayInterface.xray(exampleObject).to(UnlockedExampleGetSetter.class);
		unlockedExampleObject.setFieldInnerStatic(innerStaticWithState("newState"));
		assertThat(unlockedExampleObject.getFieldInnerStatic().getState()).isEqualTo("newState");
	}

	@Test
	public void testMatchingExampleObject() throws Exception {
		assertThat(new ExampleObject("state")).satisfies(UnlockedExampleObjectConsumer.isEquivalentToExampleObject()
			.withOuterState("state"));
	}

	@Test
	public void testMatchingExampleObjectList() throws Exception {
		List<ExampleObject> exampleObjects = asList(new ExampleObject("first"), new ExampleObject("second"));

		assertThat(exampleObjects).satisfiesExactly(
			UnlockedExampleObjectConsumer.isEquivalentToExampleObject().withOuterState("first"),
			UnlockedExampleObjectConsumer.isEquivalentToExampleObject().withOuterState("second"));
	}

	@Test
	public void testMatchingExampleObjectListInAnyOrder() throws Exception {
		List<ExampleObject> exampleObjects = asList(new ExampleObject("first"), new ExampleObject("second"));

		assertThat(exampleObjects).satisfiesExactlyInAnyOrder(
			UnlockedExampleObjectConsumer.isEquivalentToExampleObject().withOuterState("second"),
			UnlockedExampleObjectConsumer.isEquivalentToExampleObject().withOuterState("first"));
	}

	@Test
	public void testMismatchingExampleObject() throws Exception {
		assertThatThrownBy(() -> assertThat(new ExampleObject("state")).satisfies(UnlockedExampleObjectConsumer.isEquivalentToExampleObject()
			.withOuterState("other")))
				.isInstanceOf(AssertionError.class)
				.hasMessageContaining("OuterState=state");
	}

	private InnerStatic innerStaticWithState(String state) {
		return new InnerStatic() {

			@Override
			public boolean isBooleanState() {
				return false;
			}

			@Override
			public String getState() {
				return state;
			}

			@Override
			public void setState(String newState) {
			}
		};
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

	interface UnlockedExampleObjectConsumer extends Consumer<ExampleObject> {

		UnlockedExampleObjectConsumer withOuterState(String outerState);

		static UnlockedExampleObjectConsumer isEquivalentToExampleObject() {
			return isEquivalent(UnlockedExampleObjectConsumer.class);
		}

	}

}
