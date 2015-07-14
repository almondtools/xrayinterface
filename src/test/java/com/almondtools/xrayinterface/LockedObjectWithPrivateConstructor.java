package com.almondtools.xrayinterface;



public class LockedObjectWithPrivateConstructor {

	private static final String DEFAULT = null;
	private String myField;
	
	private LockedObjectWithPrivateConstructor() {
		this.myField = "initialized";
	}
	
	private LockedObjectWithPrivateConstructor(String field) {
		this.myField = field;
	}
	
	public String getMyField() {
		return myField;
	}
	
	@SuppressWarnings("unused")
	private static LockedObjectWithPrivateConstructor reset() {
		return new LockedObjectWithPrivateConstructor(DEFAULT);
	}
	
}
