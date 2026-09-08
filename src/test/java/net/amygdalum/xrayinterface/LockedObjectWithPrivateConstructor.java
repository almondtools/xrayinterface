package net.amygdalum.xrayinterface;



public class LockedObjectWithPrivateConstructor {

	private static String DEFAULT = null;
	@SuppressWarnings("unused")
	private static final String CONSTANT = "constant".toString();
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
