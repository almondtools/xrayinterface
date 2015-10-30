package com.almondtools.xrayinterface;

public final class BoxingUtil {

	private BoxingUtil() {
	}

	public static Class<?> getBoxed(Class<?> type) {
		if (!type.isPrimitive()) {
			return type;
		} else if (Byte.TYPE.equals(type)) {
			return Byte.class;
		} else if (Character.TYPE.equals(type)) {
			return Character.class;
		} else if (Short.TYPE.equals(type)) {
			return Short.class;
		} else if (Integer.TYPE.equals(type)) {
			return Integer.class;
		} else if (Long.TYPE.equals(type)) {
			return Long.class;
		} else if (Float.TYPE.equals(type)) {
			return Float.class;
		} else if (Double.TYPE.equals(type)) {
			return Double.class;
		} else if (Boolean.TYPE.equals(type)) {
			return Boolean.class;
		} else if (Void.TYPE.equals(type)) {
			return Void.class;
		} else {
			return Object.class;
		}
	}

	public static Class<?> getUnboxed(Class<?> type) {
		if (Byte.class.equals(type)) {
			return Byte.TYPE;
		} else if (Character.class.equals(type)) {
			return Character.TYPE;
		} else if (Short.class.equals(type)) {
			return Short.TYPE;
		} else if (Integer.class.equals(type)) {
			return Integer.TYPE;
		} else if (Long.class.equals(type)) {
			return Long.TYPE;
		} else if (Float.class.equals(type)) {
			return Float.TYPE;
		} else if (Double.class.equals(type)) {
			return Double.TYPE;
		} else if (Boolean.class.equals(type)) {
			return Boolean.TYPE;
		} else if (Void.class.equals(type)) {
			return Void.TYPE;
		} else {
			return type;
		}
	}
}
