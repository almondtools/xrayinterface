package net.amygdalum.xrayinterface;

import static java.util.Arrays.asList;
import static net.amygdalum.xrayinterface.IsEquivalent.isEquivalent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.nullValue;

import java.util.List;
import java.util.function.Consumer;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;

public class IsEquivalentConsumerTest {

	@Test
	public void testSatisfiesValueBasedDefaultValues() throws Exception {
		assertThat(new EqTest()).satisfies(isEquivalent(EqTestConsumer.class)
			.withI(0)
			.withStr(null));
	}

	@Test
	public void testSatisfiesValueBasedSetValues() throws Exception {
		assertThat(new EqTest("str", 42)).satisfies(isEquivalent(EqTestConsumer.class)
			.withI(42)
			.withStr("str"));
	}

	@Test
	public void testSatisfiesMatcherBasedDefaultValues() throws Exception {
		assertThat(new EqTest()).satisfies(isEquivalent(EqTestMatcherConsumer.class)
			.withI(CoreMatchers.equalTo(0))
			.withStr(nullValue()));
	}

	@Test
	public void testSatisfiesMatcherBasedSetValues() throws Exception {
		assertThat(new EqTest("str", 42)).satisfies(isEquivalent(EqTestMatcherConsumer.class)
			.withI(CoreMatchers.<Integer> both(greaterThan(41)).and(lessThan(43)))
			.withStr(containsString("st")));
	}

	@Test
	public void testSatisfiesInheritedProperties() throws Exception {
		assertThat(new EqSubTest("str", 42)).satisfies(isEquivalent(EqSubTestConsumer.class)
			.withI(42)
			.withStr("str"));
	}

	@Test
	public void testSatisfiesExactly() throws Exception {
		List<EqTest> items = asList(new EqTest("a", 1), new EqTest("b", 2));

		assertThat(items).satisfiesExactly(
			isEquivalent(EqTestConsumer.class).withStr("a").withI(1),
			isEquivalent(EqTestConsumer.class).withStr("b").withI(2));
	}

	@Test
	public void testSatisfiesExactlyRespectsOrder() throws Exception {
		List<EqTest> items = asList(new EqTest("a", 1), new EqTest("b", 2));

		assertThatThrownBy(() -> assertThat(items).satisfiesExactly(
			isEquivalent(EqTestConsumer.class).withStr("b").withI(2),
			isEquivalent(EqTestConsumer.class).withStr("a").withI(1)))
				.isInstanceOf(AssertionError.class);
	}

	@Test
	public void testSatisfiesExactlyInAnyOrder() throws Exception {
		List<EqTest> items = asList(new EqTest("a", 1), new EqTest("b", 2));

		assertThat(items).satisfiesExactlyInAnyOrder(
			isEquivalent(EqTestConsumer.class).withStr("b").withI(2),
			isEquivalent(EqTestConsumer.class).withStr("a").withI(1));
	}

	@Test
	public void testSatisfiesFailsOnMismatch() throws Exception {
		assertThatThrownBy(() -> assertThat(new EqTest("str", 42)).satisfies(isEquivalent(EqTestConsumer.class)
			.withI(42)
			.withStr("other")))
				.isInstanceOf(AssertionError.class)
				.hasMessageContaining("Str=str");
	}

	@Test
	public void testSatisfiesConsumerBasedProperties() throws Exception {
		assertThat(new EqTest("str", 42)).satisfies(isEquivalent(EqTestConsumerConsumer.class)
			.withI(i -> assertThat(i).isBetween(41, 43))
			.withStr(str -> assertThat(str).startsWith("st")));
	}

	@Test
	public void testSatisfiesConsumerBasedPropertiesMismatch() throws Exception {
		assertThatThrownBy(() -> assertThat(new EqTest("str", 42)).satisfies(isEquivalent(EqTestConsumerConsumer.class)
			.withStr(str -> assertThat(str).startsWith("other"))))
				.isInstanceOf(AssertionError.class)
				.hasMessageContaining("to start with");
	}

	@Test
	public void testSatisfiesNestedEquivalentConsumer() throws Exception {
		assertThat(new EqNestedTest(new EqTest("str", 42))).satisfies(isEquivalent(EqNestedTestConsumer.class)
			.withNested(isEquivalent(EqTestConsumer.class)
				.withI(42)
				.withStr("str")));
	}

	@Test
	public void testSatisfiesNestedEquivalentConsumerMismatch() throws Exception {
		assertThatThrownBy(() -> assertThat(new EqNestedTest(new EqTest("str", 42))).satisfies(isEquivalent(EqNestedTestConsumer.class)
			.withNested(isEquivalent(EqTestConsumer.class)
				.withStr("other"))))
					.isInstanceOf(AssertionError.class)
					.hasMessageContaining("Str=str");
	}

	@Test
	public void testDescriptionOfNestedEquivalentConsumer() throws Exception {
		Consumer<EqNestedTest> consumer = isEquivalent(EqNestedTestConsumer.class)
			.withNested(isEquivalent(EqTestConsumer.class)
				.withI(42)
				.withStr("str"));

		assertThatThrownBy(() -> consumer.accept(new EqNestedTest(new EqTest("other", 4711))))
			.isInstanceOf(AssertionError.class)
			.hasMessageContaining("expected object with properties <Nested=with properties <I=42>, <Str=str>>");
	}

	@Test
	public void testDescriptionOfPlainConsumer() throws Exception {
		Consumer<EqTest> consumer = isEquivalent(EqTestConsumerConsumer.class)
			.withStr(str -> assertThat(str).startsWith("other"));

		assertThatThrownBy(() -> consumer.accept(new EqTest("str", 42)))
			.isInstanceOf(AssertionError.class)
			.hasMessageContaining("expected object with properties <Str=satisfying the given consumer>");
	}

	@Test
	public void testAcceptMatchingItem() throws Exception {
		Consumer<EqTest> consumer = isEquivalent(EqTestConsumer.class)
			.withI(42)
			.withStr("str");

		consumer.accept(new EqTest("str", 42));
	}

	@Test
	public void testAcceptMismatchingItem() throws Exception {
		Consumer<EqTest> consumer = isEquivalent(EqTestConsumer.class)
			.withI(42)
			.withStr("str");

		assertThatThrownBy(() -> consumer.accept(new EqTest("other", 4711)))
			.isInstanceOf(AssertionError.class)
			.hasMessageContaining("expected object with properties <I=42>, <Str=str>")
			.hasMessageContaining("but found object with properties <I=4711>, <Str=other>");
	}

	@Test
	public void testAcceptNull() throws Exception {
		Consumer<EqTest> consumer = isEquivalent(EqTestConsumer.class)
			.withI(42)
			.withStr("str");

		assertThatThrownBy(() -> consumer.accept(null))
			.isInstanceOf(AssertionError.class)
			.hasMessageContaining("but found object null");
	}

	@Test
	public void testAcceptItemWithMissingProperty() throws Exception {
		Consumer<EqSuperTest> consumer = isEquivalent(EqMissingTestConsumer.class)
			.withI(42)
			.withStr("str");

		assertThatThrownBy(() -> consumer.accept(new EqSuperTest(42)))
			.isInstanceOf(AssertionError.class)
			.hasMessageContaining("Str=<missing>");
	}

	@SuppressWarnings("unused")
	private static class EqTest {
		private String str;
		private int i;

		public EqTest() {
		}

		public EqTest(String str, int i) {
			this.str = str;
			this.i = i;
		}

	}

	@SuppressWarnings("unused")
	private static class EqSubTest extends EqSuperTest {
		private String str;

		public EqSubTest(String str, int i) {
			super(i);
			this.str = str;
		}

	}

	@SuppressWarnings("unused")
	private static class EqSuperTest {
		private int i;

		public EqSuperTest(int i) {
			this.i = i;
		}

	}

	@SuppressWarnings("unused")
	private static class EqNestedTest {
		private EqTest nested;

		public EqNestedTest(EqTest nested) {
			this.nested = nested;
		}

	}

	interface EqTestConsumer extends Consumer<EqTest> {
		EqTestConsumer withStr(String str);

		EqTestConsumer withI(int i);
	}

	interface EqTestConsumerConsumer extends Consumer<EqTest> {
		EqTestConsumerConsumer withStr(Consumer<String> str);

		EqTestConsumerConsumer withI(Consumer<Integer> i);
	}

	interface EqNestedTestConsumer extends Consumer<EqNestedTest> {
		EqNestedTestConsumer withNested(Consumer<EqTest> nested);
	}

	interface EqTestMatcherConsumer extends Consumer<EqTest> {
		EqTestMatcherConsumer withStr(Matcher<? super String> str);

		EqTestMatcherConsumer withI(Matcher<? super Integer> i);
	}

	interface EqSubTestConsumer extends Consumer<EqSubTest> {
		EqSubTestConsumer withStr(String str);

		EqSubTestConsumer withI(int i);
	}

	interface EqMissingTestConsumer extends Consumer<EqSuperTest> {
		EqMissingTestConsumer withStr(String str);

		EqMissingTestConsumer withI(int i);
	}

}
