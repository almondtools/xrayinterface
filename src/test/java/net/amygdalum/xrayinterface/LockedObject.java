package net.amygdalum.xrayinterface;



public class LockedObject extends LockedSuper {

	@SuppressWarnings("unused")
	private final int integer = new Integer(2).intValue();
	private String myField;
	
	private int myMethod(String string, boolean flag) {
		return flag ? Integer.parseInt(string) : 0;
	}

	public int myPublicMethod() {
		try {
			return myMethod(myField, true);
		} catch (Exception e) {
			return myMethod(myField, false);
		}
	}
	
}
