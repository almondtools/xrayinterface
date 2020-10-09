package net.amygdalum.xrayinterface;

import static net.amygdalum.xrayinterface.IsEquivalent.equivalentTo;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.junit.Test;

public class IsEquivalentTest {

	@Test
	public void testValueBasedDefaultValues() throws Exception {
		assertThat(new EqTest(), equivalentTo(EqTestValue.class)
			.withI(0)
			.withStr(null));
	}

	@Test
	public void testValueBasedSetValues() throws Exception {
		assertThat(new EqTest("str", 42), equivalentTo(EqTestValue.class)
			.withI(42)
			.withStr("str"));
	}

	@Test
	public void testMatcherBasedDefaultValues() throws Exception {
		assertThat(new EqTest(), equivalentTo(EqTestMatcher.class)
			.withI(equalTo(0))
			.withStr(nullValue()));
	}

	@Test
	public void testMatcherBasedSetValues() throws Exception {
		assertThat(new EqTest("str", 42), equivalentTo(EqTestMatcher.class)
			.withI(CoreMatchers.<Integer> both(greaterThan(41)).and(lessThan(43)))
			.withStr(containsString("st")));
	}

	@Test
	public void testInheritedProperties() throws Exception {
		assertThat(new EqSubTest("str", 42), equivalentTo(EqSubTestValue.class)
			.withI(42)
			.withStr("str"));
	}

	@Test
	public void testMissingProperties() throws Exception {
		assertThat(new EqSuperTest(42), not(equivalentTo(EqMissingTestValue.class)
			.withI(42)
			.withStr("str")));
	}

	@Test
	public void testDescription() throws Exception {
		EqMissingTestValue matcher = equivalentTo(EqMissingTestValue.class)
			.withI(42)
			.withStr("str");
		Description description = new StringDescription();

		matcher.describeTo(description);
		assertThat(description.toString(), equalTo("with properties <I=42>, <Str=str>"));
	}

	@Test
	public void testMismatchDescription() throws Exception {
		EqMissingTestValue matcher = equivalentTo(EqMissingTestValue.class)
			.withI(42)
			.withStr("str");
		Description description = new StringDescription();

		matcher.describeMismatch(new EqTest("Kölnisch Wasser", 4711), description);
		
		assertThat(description.toString(), equalTo("with properties <I=4711>, <Str=Kölnisch Wasser>"));
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

		public EqSubTest() {
		}

		public EqSubTest(String str, int i) {
			super(i);
			this.str = str;
		}

	}

	@SuppressWarnings("unused")
	private static class EqSuperTest {
		private int i;

		public EqSuperTest() {
		}

		public EqSuperTest(int i) {
			this.i = i;
		}

	}

	interface EqTestValue extends Matcher<EqTest> {
		EqTestValue withStr(String str);

		EqTestValue withI(int i);
	}

	interface EqTestMatcher extends Matcher<EqTest> {
		EqTestMatcher withStr(Matcher<? super String> str);

		EqTestMatcher withI(Matcher<? super Integer> i);
	}

	interface EqSubTestValue extends Matcher<EqSubTest> {
		EqSubTestValue withStr(String str);

		EqSubTestValue withI(int i);
	}

	interface EqMissingTestValue extends Matcher<EqSuperTest> {
		EqMissingTestValue withStr(String str);

		EqMissingTestValue withI(int i);
	}

}
