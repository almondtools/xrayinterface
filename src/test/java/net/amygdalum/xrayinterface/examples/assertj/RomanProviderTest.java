package net.amygdalum.xrayinterface.examples.assertj;

import static java.util.Arrays.asList;
import static net.amygdalum.xrayinterface.IsEquivalent.isEquivalent;
import static net.amygdalum.xrayinterface.XRayInterface.xray;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import net.amygdalum.xrayinterface.IsEquivalent;
import net.amygdalum.xrayinterface.ReflectionFailedException;
import net.amygdalum.xrayinterface.examples.NumberProvider;
import net.amygdalum.xrayinterface.examples.RandomNumberProvider;
import net.amygdalum.xrayinterface.examples.ReallyMeanNumberProvider;
import net.amygdalum.xrayinterface.examples.RomanProvider;

/**
 * the assertj pendant of {@link net.amygdalum.xrayinterface.examples.hamcrest.RomanProviderTest} - the same scenarios
 * asserted with assertjs satisfies methods and xrayinterfaces {@link IsEquivalent#isEquivalent(Class)} consumers
 */
public class RomanProviderTest {

	private static final UnlockedRomanProvider ROMAN = xray(RomanProvider.class).to(UnlockedRomanProvider.class);

	@Test
	public void testToRoman0() {
		assertThat(ROMAN.toRoman(0)).isEqualTo("0");
	}

	@Test
	public void testToRoman1to3() {
		assertThat(ROMAN.toRoman(1)).isEqualTo("I");
		assertThat(ROMAN.toRoman(2)).isEqualTo("II");
		assertThat(ROMAN.toRoman(3)).isEqualTo("III");
	}

	@Test
	public void testToRoman4() {
		assertThat(ROMAN.toRoman(4)).isEqualTo("IV");
	}

	@Test
	public void testParts5() {
		assertThat(ROMAN.toRoman(5)).isEqualTo("V");
	}

	@Test
	public void testToRomanList() {
		List<String> romans = asList(ROMAN.toRoman(1), ROMAN.toRoman(4), ROMAN.toRoman(5));

		assertThat(romans).satisfiesExactly(
			one -> assertThat(one).isEqualTo("I"),
			four -> assertThat(four).isEqualTo("IV"),
			five -> assertThat(five).isEqualTo("V"));
	}

	/**
	 * this test will not work since numberProvider is a Singleton with hidden/inaccessible interior
	 */
	@Test
	@Disabled
	public void testDecorate() throws Exception {
		NumberProvider numberProvider = RandomNumberProvider.getInstance();
		RomanProvider decorator = new RomanProvider(numberProvider);
		assertThat(decorator.nextRoman()).isEqualTo("XXI");
		assertThat(decorator.nextRoman()).isEqualTo("XXII");
	}

	@Test
	public void testDecoratePrivateField() throws Exception {
		NumberProvider numberProvider = RandomNumberProvider.getInstance();
		xray(numberProvider)
			.to(UnlockedNumberProvider.class)
			.setNr(21);
		RomanProvider decorator = new RomanProvider(numberProvider);
		assertThat(decorator.nextRoman()).isEqualTo("XXI");
		assertThat(decorator.nextRoman()).isEqualTo("XXII");
	}

	@Test
	public void testDecorateConstructor() throws Exception {
		NumberProvider numberProvider = xray(RandomNumberProvider.class)
			.to(UnlockedStaticNumberProvider.class)
			.newRandomNumberProvider(21);
		RomanProvider decorator = new RomanProvider(numberProvider);
		assertThat(decorator.nextRoman()).isEqualTo("XXI");
		assertThat(decorator.nextRoman()).isEqualTo("XXII");
	}

	@Test
	public void testDecoratePrivateMethod() throws Exception {
		NumberProvider numberProvider = RandomNumberProvider.getInstance();
		xray(numberProvider)
			.to(UnlockedNumberProvider.class)
			.reset(21);
		RomanProvider decorator = new RomanProvider(numberProvider);
		assertThat(decorator.nextRoman()).isEqualTo("XXI");
		assertThat(decorator.nextRoman()).isEqualTo("XXII");
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
		assertThat(decorator.nextRoman()).isEqualTo("XXI");
		assertThat(decorator.nextRoman()).isEqualTo("XXII");
	}

	/**
	 * static final fields cannot be written since java 12, so a really mean singleton stays mean
	 */
	@Test
	public void testDecorateStaticFinalFieldIsUnsupported() throws Exception {
		assertThatThrownBy(() -> xray(ReallyMeanNumberProvider.class)
			.to(UnlockedReallyMeanNumberProvider.class))
				.isInstanceOf(ReflectionFailedException.class)
				.hasMessageContaining("cannot write static final field");
	}

	@Test
	public void testDecorateStaticFinalFieldReadOnly() throws Exception {
		UnlockedReallyMeanNumberProviderReadOnly bluePrint = xray(ReallyMeanNumberProvider.class)
			.to(UnlockedReallyMeanNumberProviderReadOnly.class);

		assertThat(bluePrint.getFINAL_INSTANCE()).isEqualTo(ReallyMeanNumberProvider.getInstance());
	}

	@Test
	public void testMatchingNumberProviders() throws Exception {
		NumberProvider first = newNumberProvider(21);
		NumberProvider second = newNumberProvider(42);

		assertThat(first).satisfies(NumberProviderConsumer.isEquivalentToNumberProvider().withNr(21));
		assertThat(asList(first, second)).satisfiesExactly(
			NumberProviderConsumer.isEquivalentToNumberProvider().withNr(21),
			NumberProviderConsumer.isEquivalentToNumberProvider().withNr(42));
	}

	@Test
	public void testMatchingNumberProvidersInAnyOrder() throws Exception {
		List<NumberProvider> providers = asList(newNumberProvider(21), newNumberProvider(42));

		assertThat(providers).satisfiesExactlyInAnyOrder(
			NumberProviderConsumer.isEquivalentToNumberProvider().withNr(42),
			NumberProviderConsumer.isEquivalentToNumberProvider().withNr(21));
	}

	private NumberProvider newNumberProvider(int seed) {
		return xray(RandomNumberProvider.class)
			.to(UnlockedStaticNumberProvider.class)
			.newRandomNumberProvider(seed);
	}

	private static interface UnlockedRomanProvider {
		String toRoman(int nr);
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

	interface NumberProviderConsumer extends Consumer<NumberProvider> {

		NumberProviderConsumer withNr(int nr);

		static NumberProviderConsumer isEquivalentToNumberProvider() {
			return isEquivalent(NumberProviderConsumer.class);
		}

	}

}
