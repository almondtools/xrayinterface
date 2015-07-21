package com.almondtools.xrayinterface;

import java.util.Objects;
import java.util.stream.Stream;

public interface Type {

	Class<?> matchedType();
	Class<?> convertedType();
	boolean matches(Class<?> type);
	Type matching(Class<?> type);

	static boolean matches(Type[] matchTypes, Class<?>[] types) {
		if (matchTypes.length != types.length) {
			return false;
		}
		for (int i = 0; i < types.length; i++) {
			if (!matchTypes[i].matches(types[i])) {
				return false;
			}
		}
		return true;
	}

	static Class<?>[] matchedTypes(Type[] types) {
		return Stream.of(types).map(c -> c.matchedType()).toArray(l -> new Class<?>[l]);
	}

	static Class<?>[] convertedTypes(Type[] types) {
		return Stream.of(types).map(c -> c.convertedType()).toArray(l -> new Class<?>[l]);
	}

	static boolean isWeakMatching(Type[] types) {
		return Stream.of(types).map(c -> c.matchedType()).anyMatch(Objects::isNull);
	}

}
