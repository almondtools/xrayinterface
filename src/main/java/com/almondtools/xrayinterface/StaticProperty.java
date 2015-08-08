package com.almondtools.xrayinterface;

public class StaticProperty {

	private StaticSetter setter;
	private StaticGetter getter;
	
	public StaticProperty() {
	}
	
	public void setSetter(StaticSetter setter) {
		this.setter = setter;
	}
	
	public void setGetter(StaticGetter getter) {
		this.getter = getter;
	}
	
	public StaticSetter set() {
		return setter;
	}
	
	public StaticGetter get() {
		return getter;
	}

	public boolean isReadable() {
		return getter != null;
	}

	public boolean isWritable() {
		return setter != null;
	}
}
