Akkajaure
=========

Generic Pool built on Akka.

Object pools that are implemented by Akka Actors.

Suitable for many things, including:

* Connection pool (e.g. JDBC or TCP)
* Template pools (e.g. XSLT's javax.xml.transform.Templates)
* Grammar pools (e.g. XML Schema's javax.xml.validation.Schema)

We provide two main generic patterns:

* Simple Object Pool (i.e. a pool of N items which may be borrowed and then returned).
* Key-Value Object Pool. Basically a keyed pool of pools. Each Key has its own pool of objects (all objects for all keys must be of the same type).


All objects in any type of pool are Items which are one of two types:
* ***ImmortalItem*** An Item that never expires
* ***MortalItem*** An Item that has a specific lifetime
