BindingConventions
==================

We shall shortly sketch how XRayInterface binds the interface to the underlying object:

Binding with ObjectAccess
-------------------------
* Methods of the pattern `void setXXX(Type val);` are bound
  * to the method of same signature, if existing
  * to a newly synthesized setter of a field with name `XXX` (or `xXX`) of type `Type`
* Methods of the pattern `Type getXXX();` are bound
  * to the method of same signature, if existing
  * to a newly synthesized getter of a field with name `XXX` (or `xXX`) of type `Type`
* all other methods are bound to
  * to the method of same signature, if existing


Binding with ClassAccess
-------------------------
* Methods of the pattern `void setXXX(Type val);` are bound
  * to the static method of same signature, if existing
  * to a newly synthesized setter of a static field with name `XXX` (or `xXX`) of type `Type`
* Methods of the pattern `Type getXXX();` are bound
  * to the static method of same signature, if existing
  * to a newly synthesized getter of a static field with name `XXX` (or `xXX`) of type `Type`
* Methods of the pattern `Type create(...)` are bound
  * to the static method of same signature, if existing
  * to a constructor of `Type` with matching signature
* all other methods are bound to
  * to the method of same signature, if existing
