package com.almondtools.xrayinterface.examples;

import static com.almondtools.xrayinterface.XRayInterface.xray;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Ignore;
import org.junit.Test;

import com.almondtools.xrayinterface.XRayInterface;

public class RomanProviderTest {

	@Test
	public void testToRoman0() {
		assertThat(RomanProvider.toRoman(0), equalTo("0"));
	}

	@Test
	public void testToRoman1to3() {
		assertThat(RomanProvider.toRoman(1), equalTo("I"));
		assertThat(RomanProvider.toRoman(2), equalTo("II"));
		assertThat(RomanProvider.toRoman(3), equalTo("III"));
	}

	@Test
	public void testToRoman4() {
		assertThat(RomanProvider.toRoman(4), equalTo("IV"));
	}

	@Test
	public void testParts5() {
		assertThat(RomanProvider.toRoman(5), equalTo("V"));
	}

	/**
	 * this test will not work since numberProvider is a Singleton with
	 * hidden/inaccessible interior
	 */
	@Test
	@Ignore
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
		NumberProvider numberProvider = XRayInterface.xray(RandomNumberProvider.class)
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
		NumberProvider numberProvider = XRayInterface.xray(RandomNumberProvider.class)
				.to(UnlockedStaticNumberProvider.class)
				.newRandomNumberProvider(21);
		XRayInterface.xray(RandomNumberProvider.class)
				.to(UnlockedStaticNumberProvider.class)
				.setINSTANCE(numberProvider);
		RomanProvider decorator = new RomanProvider(RandomNumberProvider.getInstance());
		assertThat(decorator.nextRoman(), equalTo("XXI"));
		assertThat(decorator.nextRoman(), equalTo("XXII"));
	}

	@Test
	public void testDecorateStaticFinalField() throws Exception {
		UnlockedReallyMeanNumberProvider bluePrint = XRayInterface
				.xray(ReallyMeanNumberProvider.class)
				.to(UnlockedReallyMeanNumberProvider.class);

		ReallyMeanNumberProvider numberProvider = bluePrint.newReallyMeanNumberProvider();
		bluePrint.setFINAL_INSTANCE(numberProvider);
		xray(numberProvider)
				.to(UnlockedNumberProvider.class)
				.setNr(21);

		RomanProvider decorator = new RomanProvider(ReallyMeanNumberProvider.getInstance());
		assertThat(decorator.nextRoman(), equalTo("XXI"));
		assertThat(decorator.nextRoman(), equalTo("XXII"));
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

}