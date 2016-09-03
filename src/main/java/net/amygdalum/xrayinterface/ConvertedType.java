package net.amygdalum.xrayinterface;

public class ConvertedType implements Type {

	private Class<?> type;
	private Class<?> converted;

	private ConvertedType(Class<?> type, Class<?> converted) {
		this.type = type;
		this.converted = converted;
	}

	public static ConvertedType converted(Class<?> clazz, Class<?> converted) {
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

	@Override
	public int hashCode() {
		return type.getName().hashCode() * 17
			+ converted.getName().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ConvertedType that = (ConvertedType) obj;
		return this.type == that.type
			&& this.converted == that.converted;
	}
	
	

}
