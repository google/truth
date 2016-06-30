---
subtitle: Changelog
layout: default
url: /changelog
---

* auto-gen TOC:
{:toc}


## Planned improvements and changes

-   Subject wrappers for new types:
    -   Support for Protocol Buffers (and other reflective types)
    -   Support for Java8 types (Optional, Stream, etc.)
    -   Cleaned up API for extending Truth via failure strategies, supplementary
        context.
    -   Code-generated boilerplate for custom subjects
    -   Iterating subjects (code-generated for each Subject), e.g. `java
        assertAbout(strings()).in(collection).thatEach().contains("foo");`

## 1.0-SNAPSHOT

-   Basic form of:
    -   `verb().that(subject).somePredicate();`
-   Shorthand for `assert_().that(...)...` as `assertThat(...)...`
-   Support for different "failure strategies":
    -   Assertions: Immediate short-circuited failure
    -   Assumptions: Skipping the test as invalid (useful in parameterized
        tests)
    -   Expectations: Error at end of test, gathering failures for later
        reporting.
    -   Mechanism for supplying other behavior on failure.
-   Strongly typed proposition classes (Subject wrapper classes) including
    support for
    -   Arbitrary objects
    -   Strings
    -   Collections: Iterators, Collections, and Lists, Maps
    -   Arrays: Object, primitives (boolean, int, float, etc.)
    -   Class objects
    -   Booleans
    -   Integral types
    -   Floating point types (with proper delta/tolerance-aware comparisons)
    -   Guava Optional<T> and other Guava types (java8 `Optional<T>` will be
        provided through an extension)
    -   And [many other types](known_types)
-   Extensibility via `about`, e.g.
    `assertAbout(javaSources()).that(sourceFile)...`
-   Labeling of subjects without reasonable toString() implementations via
    `named("label")`
-   Providing a contextual failure message via
    `assertWithMessage("someMessage").that(...)...`
-   ComparisonFailure used for Strings (for better IDE errors)
