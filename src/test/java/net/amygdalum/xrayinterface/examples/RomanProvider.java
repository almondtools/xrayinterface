package net.amygdalum.xrayinterface.examples;


public class RomanProvider {

	private static int[] PARTS = new int[] { 100, 90, 50, 40, 10, 9, 5, 4, 1 };
	private static String[] ROMAN = new String[] { "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I" };

	private NumberProvider single;

	public RomanProvider(NumberProvider single) {
		this.single = single;
	}

	protected static String toRoman(int nr) {
		if (nr == 0) {
			return "0";
		}
		StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < PARTS.length; i++) {
			int part = PARTS[i];
			String roman = ROMAN[i];
			while (nr >= part) {
				nr -= part;
				buffer.append(roman);
			}
		}
		return buffer.toString();
	}

	public String nextRoman() {
		return toRoman(single.nextNr());
	}

}
