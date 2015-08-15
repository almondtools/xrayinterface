BindingConventions
==================

We shall shortly sketch how XRayInterface binds the interface to the underlying object:

* Methods annotated with `@SetProperty(XXX)` are bound
  * to a newly synthesized setter of a field with name `XXX`
* Methods annotated with `@GetProperty(XXX)` are bound
  * to a newly synthesized getter of a field with name `XXX`
* Methods annotated with `@Construct` are bound
  * to a constructor of same signature
* Methods annotated with `@Delegate(XXX)` are bound
  * to a method of same signature with  name `XXX`
* Methods of the pattern `void setXXX(Type val);` are bound
  * to the method of same signature, if existing
  * to a newly synthesized setter of a field with name `XXX` of type `Type`
* Methods of the pattern `Type getXXX();` are bound
  * to the method of same signature, if existing
  * to a newly synthesized getter of a field with name `XXX` of type `Type`
* Methods of the pattern `Type new<Type>(...)` are bound
  * to the static method of same signature, if existing
  * to a constructor of `Type` with matching signature
* all other methods are bound to
  * to the method of same signature, if existing
