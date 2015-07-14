Handling Singletons
===================
Some frameworks make heavy use of singletons. The singleton pattern has a small unconvenience considering testability: 
classes using a singleton almost always contain a fixed dependency that is not testable.

XRayInterface enables us to bypass the design decisions temporarily, when needed.

We will use following singleton for the following examples:

```Java
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
```

Obviously we cannot make the method isUnique return false without hacking. So lets go ahead

Changing the Singleton Instance directly
========================================
We can of course directly modify the singleton with XRayInterface, e.g.

```Java
	@Test
	public void testDirectSingletonModification() throws Exception {
		TheOneAndOnly instance = TheOneAndOnly.getInstance();
		ObjectAccess.xray(instance).to(XRayed.class).setUnique(false);
		assertThat(instance.isUnique(), is(false));
	}
	
	interface XRayed {
		void setUnique(boolean unique);
	}
```

A true singleton would do exactly what we want.

Intruding into the Singleton Factory
====================================
Another way would be to access and change the static property instance.

```Java
	@Test
	public void testSingletonSingletonFactoryIntrusion() throws Exception {
		TheOneAndOnly instance = ClassAccess.xray(TheOneAndOnly.class).to(XRayedStatic.class).getInstance();
		ObjectAccess.xray(instance).to(XRayed.class).setUnique(false);
		assertThat(TheOneAndOnly.getInstance().isUnique(), is(false));
	}
	
	interface XRayed {
		void setUnique(boolean unique);
	}
	
	interface XRayedStatic {
		TheOneAndOnly getInstance();
	}
```

To provide static properties, we have to use the class ClassAccess instead of ObjectAccess. The conventions for the interface are similar
to the interfaces for ObjectAccess. Note that the interface has itself non-static methods, the accessed methods/properties are static.  

Singleton Injection
===================
Now there is a third possibility - inject the correctly configured singleton. This could be very helpful when the singleton class is very complex and we want to use a mock instead of a modified singleton.

```Java
	@Test
	public void testSingletonInjection() throws Exception {
		XRayedStaticWithConstructor xRayedOneAndOnly = ClassAccess.xray(TheOneAndOnly.class).to(XRayedStaticWithConstructor.class);
		TheOneAndOnly instance = xRayedOneAndOnly.create();
		ObjectAccess.xray(instance).to(XRayed.class).setUnique(false);
		XRayedOneAndOnly.setInstance(instance);
		assertThat(TheOneAndOnly.getInstance().isUnique(), is(false));
	}
	
	interface XRayed {
		void setUnique(boolean unique);
	}
	
	interface XRayedStaticWithConstructor {
		TheOneAndOnly create();
		void setInstance(TheOneAndOnly instance);
	}
```
  
As one can see we use the ClassAccess object to invoke the private constructor. Any interface method named create delegates to a constructor with a matching signature.