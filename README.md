[XRayInterface](http://almondtools.github.io/xrayinterface/)
=============

XRayInterface (formerly [Picklock](https://github.com/almondtools/picklock)) is a convenient interface to Java reflection.

* not depending on strings
* but on interface conventions

This Readme contains technical infos to the project (e.g. Problems, Roadmap or the State of Work)

Information on Usage and Download can be found on [XRayInterface](http://almondtools.github.io/xrayinterface/). 

XRayInterface vs. Picklock
==========================
XRayInterface follows the same idea as Picklock, yet there are some difference:

* XRayInterface requires Java 8 (or higher)
* XRayInterface may be bound by convention (method names) or annotations
* XRayInterface is probably more performant because it relies on method handles (and not on the reflection api as Picklock)
* the API is different