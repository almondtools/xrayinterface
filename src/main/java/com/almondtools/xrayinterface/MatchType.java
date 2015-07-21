package com.almondtools.xrayinterface;

public class MatchType implements Type {

	private String match;
	private Class<?> converted;

	public MatchType(String match, Class<?> converted) {
		this.match = match;
		this.converted = converted;
	}

	@Override
	public Class<?> matchedType() {
		return null;
	}

	@Override
	public Class<?> convertedType() {
		return converted;
	}

	@Override
	public boolean matches(Class<?> type) {
		return type.getSimpleName().equals(match)
			|| converted.isAssignableFrom(type);
	}

	@Override
	public Type matching(Class<?> type) {
		if (type.equals(converted)) {
			return new FixedType(type);
		} else {
			return new ConvertedType(type, converted);
		}
	}

}
