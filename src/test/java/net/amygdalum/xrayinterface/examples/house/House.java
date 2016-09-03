package net.amygdalum.xrayinterface.examples.house;

import java.util.ArrayList;
import java.util.List;

public class House {

	private Key houseKey;
	private boolean locked;
	private List<Furniture> furniture;

	public House(Key key) {
		this.houseKey = key;
		this.furniture = new ArrayList<Furniture>();
	}

	public boolean lock(Key key) {
		if (houseKey.equals(key)) {
			close();
		}
		return locked;
	}

	public boolean open(Key key) {
		if (houseKey.equals(key)) {
			open();
		}
		return !locked;
	}

	private void open() {
		locked = false;
	}

	private void close() {
		locked = true;
	}

	public List<Furniture> listFurniture() {
		if (locked) {
			throw new UnsupportedOperationException("cannot list furniture before opening the house");
		}
		return furniture;
	}

	public void add(Furniture f) {
		if (locked) {
			throw new UnsupportedOperationException("cannot add furniture before opening the house");
		}
		furniture.add(f);
	}

}
