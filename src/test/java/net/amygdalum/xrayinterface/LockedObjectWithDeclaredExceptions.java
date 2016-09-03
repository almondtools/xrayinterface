package net.amygdalum.xrayinterface;

import java.io.IOException;



public class LockedObjectWithDeclaredExceptions extends LockedSuper {

	private String myMethod(String string) throws IOException {
		if (string == null) {
			throw new IOException();
		}
		return string;
	}
	
	public String myPublicMethod() {
		try {
			return myMethod(null);
		} catch (IOException e) {
			try {
				return myMethod("was null");
			} catch (IOException e1) {
				return "unexpected";
			}
		}
	}
	
}
