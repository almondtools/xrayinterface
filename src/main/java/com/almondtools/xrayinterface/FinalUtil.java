package com.almondtools.xrayinterface;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public final class FinalUtil {

	private static final String MODIFIERS = "modifiers";

	private FinalUtil() {
	}

	public static boolean isFinal(Field field) {
		return (field.getModifiers() & Modifier.FINAL) == Modifier.FINAL;
	}

	public static void makeNonFinal(Field field) {
		try {
			Field modifiersField = Field.class.getDeclaredField(MODIFIERS);
			modifiersField.setAccessible(true);
			modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
		} catch (Exception e) {
			//omit this exception
		}
	}

	public static void ensureNonFinal(Field field) {
		if (isFinal(field)) {
			makeNonFinal(field);
		}
	}
}
