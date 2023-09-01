XRaying a sealed class
======================
For the following examples consider following class

```Java
public class House {

	private Key houseKey;
	private boolean locked;
	private List<Furniture> furniture;
	...

	public boolean open(Key key) {
		if (houseKey.equals(key)) {
			open();
		}
		return !locked;
	}

	private void open() {
		locked = false;
	}

	public List<Furniture> listFurniture() {
		if (locked) {
			throw new UnsupportedOperationException("cannot list furniture if house is locked");
		}
		return furniture;
	}

	...
}
```

Executing hidden behaviour
==========================
Not knowing the house key will not give you access to the house and its furniture. Yet we want to call the open method.

The classic way to access the house would be reflection:

```Java
	public List<Furniture> open(House house) throws Exception {
		Class<? extends House> houseClass = house.getClass(); // class lookup
		Method open = houseClass.getDeclaredMethod("open", new Class<?>[0]); // method lookup, type signature wrapping
		open.setAccessible(true); // access enabling
		open.invoke(house,new Object[0]); // non object oriented call, argument wrapping
		return house.listFurniture();
	}
```

The xrayinterface style for example:

```Java
	public List<Furniture> open(House house) {
		XRayHouse xRayHouse = XRayInterface.xray(house).to(XRayHouse.class); // unlock interface
		xRayHouse.open(); // call unlocked method
		return house.listFurniture();
	}
	
	interface XRayHouse {
		void open();
	}
```

Of course one may also unlock methods with arguments, simply repeat the signature of the private method you want to use in
the xrayinterface.

Snooping Private State
======================
Assume that we do not want to call the open method, but we want to know the house key we have to pass to the open method.

For example:

```Java
	public List<Furniture> open(House house) throws NoSuchMethodException {
		XrayHouse xRayHouse = XRayInterface.xray(house).to(XrayHouse.class);
		Key key = xRayHouse.getHouseKey(); // aquire the private key
		house.open(key); // execute the public method
		return house.listFurniture();
	}
	
	interface XrayHouse {
		Key getHouseKey();
	}
```

Changing Private State
======================
Assume that we do not want to spy out the correct key, but we want to change the key/lock to a more convenient behaviour.

For example:

```Java
	public List<Furniture> open(House house) throws NoSuchMethodException {
		Key key = new Key();
		XRayHouse xRayHouse = XRayInterface.xray(house).to(XRayHouse.class);
		xRayHouse.setHouseKey(key);
		house.open(key);
		return house.listFurniture();
	}
	
	interface XRayHouse {
		void setHouseKey(Key key);
	}
```