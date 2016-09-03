package net.amygdalum.xrayinterface.examples.singleton;

public final class TheOneAndOnly {

	private static TheOneAndOnly instance;
	
	private boolean unique;
	
	private TheOneAndOnly()  {
		unique = true;
	}
	
	public boolean isUnique() {
		return unique;
	}
	
	public static TheOneAndOnly getInstance() {
		if (instance == null) {
			instance = new TheOneAndOnly();
		}
		return instance;
	}
	
}
