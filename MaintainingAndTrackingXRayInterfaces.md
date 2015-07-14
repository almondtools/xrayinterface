Maintaining and Tracking XRayInterfaces
=======================================
Can we view XRayInterface as a statically typed interface to Reflection? In Theory we would use XRayInterface with a statically known features. In this case only the compiler limits us from ruling out broken reflection mappings at compile time.

But it is rather easy to be safe from broken reflection mappings. Just write a test for each pair of closed classes and feature interfaces, e.g. for the upper example:
  
```Java
import static com.almondtools.xrayinterface.XRayMatcher.providesFeaturesOf;

...
	@Test
	public void testPreventRuntimeErrorsOnXRaying() throws Exception {
		assertThat(House.class, providesFeaturesOf(XRayed.class));
		assertThat(House.class, providesFeaturesOf(XRayedKey.class));
		assertThat(House.class, providesFeaturesOf(XRayedLock.class));
	}
...
```	