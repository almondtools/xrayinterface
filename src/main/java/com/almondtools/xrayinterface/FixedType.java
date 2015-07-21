package com.almondtools.xrayinterface;

public class FixedType implements Type {

	public static final FixedType VOID = new FixedType(void.class);
	
	private Class<?> type;

	public FixedType(Class<?> type) {
		this.type = type;
	}

	public static Type fixed(Class<?> clazz) {
		return new FixedType(clazz);
	}

	@Override
	public Class<?> matchedType() {
		return type;
	}

	@Override
	public Class<?> convertedType() {
		return type;
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
