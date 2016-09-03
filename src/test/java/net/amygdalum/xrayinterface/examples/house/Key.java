package net.amygdalum.xrayinterface.examples.house;

public class Key {

	private double id;

	public Key() {
		this.id = Math.random();
	}
	
	private Key(double id) {
		this.id = id;
	}
	
	protected Key copy() {
		return new Key(id);
	}

	@Override
	public int hashCode() {
		return new Double(id).hashCode();
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
		Key that = (Key) obj;
		return this.id == that.id;
	}

}
