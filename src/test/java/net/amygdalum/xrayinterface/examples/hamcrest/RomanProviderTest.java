package net.amygdalum.xrayinterface.examples.hamcrest;

import static java.util.Arrays.asList;
import static net.amygdalum.xrayinterface.XRayInterface.xray;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import net.amygdalum.xrayinterface.IsEquivalent;
import net.amygdalum.xrayinterface.ReflectionFailedException;
import net.amygdalum.xrayinterface.examples.NumberProvider;
import net.amygdalum.xrayinterface.examples.RandomNumberProvider;
import net.amygdalum.xrayinterface.examples.ReallyMeanNumberProvider;
import net.amygdalum.xrayinterface.examples.RomanProvider;

public class RomanProviderTest {

	private static final UnlockedRomanProvider ROMAN = xray(RomanProvider.class).to(UnlockedRomanProvider.class);

	@Test
	public void testToRoman0() {
		assertThat(ROMAN.toRoman(0), equalTo("0"));
	}

	@Test
	public void testToRoman1to3() {
		assertThat(ROMAN.toRoman(1), equalTo("I"));
		assertThat(ROMAN.toRoman(2), equalTo("II"));
		assertThat(ROMAN.toRoman(3), equalTo("III"));
	}

	@Test
	public void testToRoman4() {
		assertThat(ROMAN.toRoman(4), equalTo("IV"));
	}

	@Test
	public void testParts5() {
		assertThat(ROMAN.toRoman(5), equalTo("V"));
	}

	/**
	 * this test will not work since numberProvider is a Singleton with
	 * hidden/inaccessible interior
	 */
	@Test
	@Disabled
	public void testDecorate() throws Exception {
		NumberProvider numberProvider = RandomNumberProvider.getInstance();
		RomanProvider decorator = new RomanProvider(numberProvider);
		assertThat(decorator.nextRoman(), equalTo("XXI"));
		assertThat(decorator.nextRoman(), equalTo("XXII"));
	}

	@Test
	public void testDecoratePrivateField() throws Exception {
		NumberProvider numberProvider = RandomNumberProvider.getInstance();
		xray(numberProvider)
				.to(UnlockedNumberProvider.class)
				.setNr(21);
		RomanProvider decorator = new RomanProvider(numberProvider);
		assertThat(decorator.nextRoman(), equalTo("XXI"));
		assertThat(decorator.nextRoman(), equalTo("XXII"));
	}

	@Test
	public void testDecorateConstructor() throws Exception {
		NumberProvider numberProvider = xray(RandomNumberProvider.class)
				.to(UnlockedStaticNumberProvider.class)
				.newRandomNumberProvider(21);
		RomanProvider decorator = new RomanProvider(numberProvider);
		assertThat(decorator.nextRoman(), equalTo("XXI"));
		assertThat(decorator.nextRoman(), equalTo("XXII"));
	}

	@Test
	public void testDecoratePrivateMethod() throws Exception {
		NumberProvider numberProvider = RandomNumberProvider.getInstance();
		xray(numberProvider)
				.to(UnlockedNumberProvider.class)
				.reset(21);
		RomanProvider decorator = new RomanProvider(numberProvider);
		assertThat(decorator.nextRoman(), equalTo("XXI"));
		assertThat(decorator.nextRoman(), equalTo("XXII"));
	}

	@Test
	public void testDecorateStaticField() throws Exception {
		NumberProvider numberProvider = xray(RandomNumberProvider.class)
				.to(UnlockedStaticNumberProvider.class)
				.newRandomNumberProvider(21);
		xray(RandomNumberProvider.class)
				.to(UnlockedStaticNumberProvider.class)
				.setINSTANCE(numberProvider);
		RomanProvider decorator = new RomanProvider(RandomNumberProvider.getInstance());
		assertThat(decorator.nextRoman(), equalTo("XXI"));
		assertThat(decorator.nextRoman(), equalTo("XXII"));
	}

	/**
	 * static final fields cannot be written since java 12, so a really mean singleton stays mean
	 */
	@Test
	public void testDecorateStaticFinalFieldIsUnsupported() throws Exception {
		ReflectionFailedException exception = assertThrows(ReflectionFailedException.class, () -> {
			xray(ReallyMeanNumberProvider.class)
				.to(UnlockedReallyMeanNumberProvider.class);
		});

		assertThat(exception.getMessage(), containsString("cannot write static final field"));
	}

	@Test
	public void testDecorateStaticFinalFieldReadOnly() throws Exception {
		UnlockedReallyMeanNumberProviderReadOnly bluePrint = xray(ReallyMeanNumberProvider.class)
				.to(UnlockedReallyMeanNumberProviderReadOnly.class);

		assertThat(bluePrint.getFINAL_INSTANCE(), equalTo(ReallyMeanNumberProvider.getInstance()));
	}

	@Test
	public void testToRomanList() {
		List<String> romans = asList(ROMAN.toRoman(1), ROMAN.toRoman(4), ROMAN.toRoman(5));

		assertThat(romans, contains(equalTo("I"), equalTo("IV"), equalTo("V")));
	}

	@Test
	public void testMatchingNumberProviders() throws Exception {
		NumberProvider first = newNumberProvider(21);
		NumberProvider second = newNumberProvider(42);

		assertThat(first, NumberProviderMatcher.matchesNumberProvider().withNr(21));
		assertThat(asList(first, second), contains(
			NumberProviderMatcher.matchesNumberProvider().withNr(21),
			NumberProviderMatcher.matchesNumberProvider().withNr(42)));
	}

	@Test
	public void testMatchingNumberProvidersInAnyOrder() throws Exception {
		List<NumberProvider> providers = asList(newNumberProvider(21), newNumberProvider(42));

		assertThat(providers, containsInAnyOrder(
			NumberProviderMatcher.matchesNumberProvider().withNr(42),
			NumberProviderMatcher.matchesNumberProvider().withNr(21)));
	}

	private NumberProvider newNumberProvider(int seed) {
		return xray(RandomNumberProvider.class)
				.to(UnlockedStaticNumberProvider.class)
				.newRandomNumberProvider(seed);
	}

	private static interface UnlockedRomanProvider {
		String toRoman(int nr);
	}

	interface NumberProviderMatcher extends Matcher<NumberProvider> {

		NumberProviderMatcher withNr(int nr);

		static NumberProviderMatcher matchesNumberProvider() {
			return IsEquivalent.equivalentTo(NumberProviderMatcher.class);
		}

	}

	private static interface UnlockedNumberProvider {
		void setNr(int nr);

		void reset(int resetNr);
	}

	private static interface UnlockedStaticNumberProvider {
		RandomNumberProvider newRandomNumberProvider(int seed);

		void setINSTANCE(NumberProvider provider);
	}

	private static interface UnlockedReallyMeanNumberProvider {
		ReallyMeanNumberProvider newReallyMeanNumberProvider();

		void setFINAL_INSTANCE(ReallyMeanNumberProvider provider);
	}

	private static interface UnlockedReallyMeanNumberProviderReadOnly {
		ReallyMeanNumberProvider getFINAL_INSTANCE();
	}

}
