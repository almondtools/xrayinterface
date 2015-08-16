package com.almondtools.xrayinterface;

import java.util.IdentityHashMap;
import java.util.Map;

public class FixedType implements Type {

	public static final FixedType VOID = new FixedType(void.class);
	
	private static final Map<Class<?>, FixedType> TYPES = new IdentityHashMap<>();
	
	private Class<?> type;

	private FixedType(Class<?> type) {
		this.type = type;
	}

	public static FixedType fixed(Class<?> clazz) {
		FixedType computeIfAbsent = TYPES.computeIfAbsent(clazz, key -> new FixedType(key));
		return computeIfAbsent;
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
