Maintaining and Tracking XRayInterfaces
=======================================
We have shown that XRayInterface provides a convenient interface to reflection. Yet it is not completely robust against refactorings. So we have to find a way to provide robustness with other means. Our Recommendation: Just write a test for each pair of closed classes and feature interfaces, e.g. for the upper example:
  
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