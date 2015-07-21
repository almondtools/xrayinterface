package com.almondtools.xrayinterface;

public class ConvertedType implements Type {

	private Class<?> type;
	private Class<?> converted;

	public ConvertedType(Class<?> type, Class<?> converted) {
		this.type = type;
		this.converted = converted;
	}

	public static Type converted(Class<?> clazz, Class<?> converted) {
		return new ConvertedType(clazz, converted);
	}

	@Override
	public Class<?> matchedType() {
		return type;
	}

	@Override
	public Class<?> convertedType() {
		return converted;
	}

	@Override
	public boolean matches(Class<?> type) {
		return this.type.equals(type);
	}

	@Override
	public Type matching(Class<?> type) {
		if (matches(type)) {
			return this;
		} else {
			throw new IllegalArgumentException();
		}
	}

}
