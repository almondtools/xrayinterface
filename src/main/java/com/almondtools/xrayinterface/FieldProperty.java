package com.almondtools.xrayinterface;

public class FieldProperty {

	private FieldSetter setter;
	private FieldGetter getter;
	
	public FieldProperty() {
	}
	
	public void setSetter(FieldSetter setter) {
		this.setter = setter;
	}
	
	public void setGetter(FieldGetter getter) {
		this.getter = getter;
	}
	
	public FieldSetter set() {
		return setter;
	}
	
	public FieldGetter get() {
		return getter;
	}

	public boolean isReadable() {
		return getter != null;
	}

	public boolean isWritable() {
		return setter != null;
	}
}
