[XRayInterface](http://xrayinterface.amygdalum.net/)
=============
[![Build Status](https://api.travis-ci.org/almondtools/xrayinterface.svg)](https://travis-ci.org/almondtools/xrayinterface)
[![codecov](https://codecov.io/gh/almondtools/xrayinterface/branch/master/graph/badge.svg)](https://codecov.io/gh/almondtools/xrayinterface)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/c5cf275a6ff9468f95b22b8998b52e1d)](https://www.codacy.com/project/almondtools/xrayinterface/dashboard?utm_source=github.com&utm_medium=referral&utm_content=almondtools/xrayinterface&utm_campaign=Badge_Grade_Dashboard)

XRayInterface (formerly [Picklock](https://github.com/almondtools/picklock)) is a convenient interface to Java reflection.

* not depending on strings
* but on interface conventions

This Readme contains technical infos to the project (e.g. Problems, Roadmap or the State of Work)

Information on Usage and Download can be found on [XRayInterface](http://xrayinterface.amygdalum.net/). 

XRayInterface vs. Picklock
==========================
XRayInterface follows the same idea as Picklock, yet there are some difference:

* XRayInterface requires Java 8 (or higher)
* XRayInterface may be bound by convention (method names) or annotations
* XRayInterface is probably more performant because it relies on method handles (and not on the reflection api as Picklock)
* the API is different

Maven Dependency
================

```xml
<dependency>
    <groupId>net.amygdalum</groupId>
    <artifactId>xrayinterface</artifactId>
    <version>0.3.3</version>
</dependency>
```