---
subtitle: Changelog
layout: default
url: /changelog/
---

* auto-gen TOC:
{:toc}

Planned improvements and changes
--------------------------------

  - Subject wrappers for new types:
    - New subjects for Float/Double and other currently missing types.
	    - Support for Annotations and methods and field availability on classes.
    - Support for Protocol Buffers, JavaBeans (and other reflective types)
    - Support for Guava collections and types (Multimaps, Multisets, etc.)
  - New propositions on existing Subject wrappers:
    - StringSubject, IntegerSubject, etc.


1.0-SNAPSHOT
-------

  - Basic form of:
    - `verb().that(subject).somePredicate();`
  - Shorthand for `assert_().that(...)...` as `assertThat(...)...`
  - Support for different "failure strategies":
    - Assertions: Immediate short-circuited failure
    - Assumptions: Skipping the test as invalid (useful in parameterized tests)
    - Expectations: Error at end of test, gathering failures for later reporting.
  - Iterative form, such as `ASSERT.in(collection).thatEach(type).somePredicate();`
  - Strongly typed proposition classes (Subjects) including support for
    - Arbitrary objects
    - Strings
    - Collections: Iterators, Collections, and Lists, Maps
    - Arrays: Object, primitives (boolean, int, float, etc.)
    - Class objects
    - Booleans
    - Integers
    - Guava Optional<T> (java8 Optional<T> needs to be provided through an extension) 
  - Extensibility via `about()`, e.g. `ASSERT.about(javaSources()).that(sourceFile)...`
  - ComparisonFailure used for Strings (for better IDE errors)
  
