package net.amygdalum.xrayinterface.examples.house;

import java.util.ArrayList;
import java.util.List;

public class Safe implements Furniture {

	private String code;
	private boolean locked;
	private List<Item> items;

	public Safe() {
		this.code = "0000";
		this.items = new ArrayList<Item>();
	}

	public void newCombination(String code, String newcode) {
		if (this.code.equals(code)) {
			this.code = newcode;
		}
	}

	public void lock() {
		locked = true;
	}

	public List<Item> open() {
		if (locked) {
			throw new UnsupportedOperationException("cannot open safe, it is locked");
		}
		return items;
	}

	public List<Item> open(String code) {
		if (!code.equals(this.code)) {
			throw new IllegalArgumentException("code failed, try another combination");
		}
		locked = false;
		return open();
	}

	public void put(Item item) {
		items.add(item);
	}

}
