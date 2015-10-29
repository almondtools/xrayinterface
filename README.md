XRayInterface
=============

XRayInterface is a convenient interface to Java reflection.

* not depending on strings
* but on interface conventions

XRayInterface is easy to use:

* determine which private fields or methods you are missing 
* define the public interface you are missing
* bind this interface with XRayInterface to the object you want to access


e.g. setting a private field `x` of your instance `foo`. Then define an interface:

    interface OpenFoo {
      void setX(String value);
    }
    
and take an x-ray snapshot:

     OpenFoo openFoo = XRayInterface.xray(foo).to(OpenFoo.class);
     openFoo.setX("bar");

or getting the value of some private field `y` of your instance `foo`. Then define an interface:

    interface OpenFoo {
      String getX();
    }
    
and take an x-ray snapshot:

     OpenFoo openFoo = XRayInterface.xray(foo).to(OpenFoo.class);
     String bar = openFoo.getX();

or making a simple private method `boolean m(int value)` of `foo` accessible. Then define an interface:

    interface OpenFoo {
      boolean m(int value);
    }
    
and take an x-ray snapshot:

     OpenFoo openFoo = XRayInterface.xray(foo).to(OpenFoo.class);
     int bar = openFoo.m("bar");

You want to learn about the advantages of XRayInterface over pure Java reflection? Then see [here](XRayInterfaceVsJavaReflection.md).

Maybe you are interested in some advanced examples

* on [opening a sealed class](OpeningASealedClass.md)
* on [handling Singletons](HandlingSingletons.md)
* on [creating powerful JUnit Matchers](CreatingPowerfulJUnitMatchers.md)
* on [the binding conventions of XrayInterface](BindingConventions.md) 
* on [maintaining and tracking XrayInterfaces](MaintainingAndTrackingXRayInterfaces.md) 


Using XRayInterface
===================

Maven Dependency
----------------

```xml
<dependency>
	<groupId>com.github.almondtools</groupId>
	<artifactId>xrayinterface</artifactId>
	<version>0.2.8</version>
</dependency>
```

Bugs and Issues
---------------
If you find a bug or some other inconvenience with xrayinterface:
- Open an Issue
- If possible provide a code example which reproduces the problem
- Optional: Provide a pull request which fixes (or works around) the problem

If you miss a feature:
- Open an Issue describing the missing feature

If you find bad or misleading english in the documentation:
- Tell me
