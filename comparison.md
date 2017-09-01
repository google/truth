---
subtitle: Comparison
layout: default
url: /comparison
---


## Overview

Truth is a fluent assertion framework, which has a lot of similarities to
[AssertJ], which was forked from [FEST]. Truth is significantly different from
[Hamcrest], which uses [Polish prefix notation] to compose assertions.


### Why create Truth when AssertJ already exists?

In other words: Maybe Truth is better for some use cases, but why not just
improve AssertJ instead? The reason is historical: AssertJ didn't exist when we
started Truth. By the time it was created, we'd begun using Truth widely
internally, and we'd added some features that would be difficult to retrofit
onto AssertJ.

## Feature Comparison vs. AssertJ

Truth and AssertJ are very similar. If you're happy with one, you probably
wouldn't benefit from switching to the other. So I provide this comparison
mostly for people who don't currently use either. I'll try to be objective, but
I acknolwedge that I'm in a better position to explain our own decisions than
AssertJ's. If you identify something wrong, missing, or misleading, please let
us know.

### Stability

AssertJ, while it continues to add APIs, rarely removes them anymore.

Truth has not reached 1.0. We're still making changes, particularly in removing
and reworking our extensibility APIs.

### Number of assertion methods

AssertJ has more: more classes of assertions ([AssertJ][AssertJ-classes],
[Truth][Truth-classes]) and more methods per class ([AssertJ][AssertJ-Iterable],
[Truth][Truth-Iterable]). That includes advanced features like [reflective field
comparisons].

More is not *necessarily* better: It can make it harder to find what you're
looking for, and it can mean you need to learn to read multiple styles of tests
or understand the interaction of more features. But naturally, more features can
also mean more convenience and power.

<!-- TODO(cpovirk): Link to a doc about the "act vs. verify" distinction once we've written it, using PredicateAssert as an example. -->

### Failure messages

I think that AssertJ generally does this better at the moment. It follows an
"expected: ... but was: ..." model, while Truth follows a "Not true that ... was
..." model. We plan to improve this, but AssertJ is better today.

### Chaining

AssertJ supports multiple assertion calls on the same object in the same
statement:

```java
assertThat(list)
    .contains(x, y)
    .doesNotContain(z);
```

Truth does not.

Both libraries support "chaining" in the sense of a method that returns a new
asserter for a sub-object:

For example, AssertJ supports:

```java
assertThat(list).last().isEqualTo(x);
```

And Truth supports:

```java
assertThat(multimap).valuesForKey(x).containsExactly(y, z);
```

Our philosophy has been that it's clearer to support only one kind of chaining,
but I suspect that the AssertJ style is generally clear, too, and it can be
convenient.

### Assertion methods for other libraries' types

AssertJ provides assertions for several libraries' types. As of this writing,
[its home page][AssertJ] lists Guava, Joda-Time, DB, Neo4j, and Swing.

Truth includes assertions for [Guava] and [Protocol Buffers].

Both have third-party extensions, such as for Android types
([AssertJ][AssertJ-Android], [Truth][Truth-Android]). I don't have a feel for
the overall size of each ecosystem.

### Platform support (Android, GWT)

AssertJ supports Android (though I had to use 2.x because the dexer rejected
3.x, even when I used only `Java6Assertions`).

Truth supports Android in all its versions. The downside is that it requires you
to look in a separate class for Java 8 assertions.

Truth also supports [GWT].

### Writing your own assertion methods

Both support this. A few notes on differences:

-   The two are verbose in different places: AssertJ requires (at least by
    convention) `return this;` at the end of each method; Truth requires a
    `SubjectFactory` anonymous class. Both require some verbose generics (and
    even an additional abstract class if you want to properly support
    subclassing), though we have plans to improve things in Truth.
-   Truth's `Subject` class provides some convenience methods that build a
    failure message for you. (But we need to improve the format of that message,
    so the methods will change soon.)
-   AssertJ offers [a tool][AssertJ-generator] to generate the methods for you.

### Failure strategies

AssertJ supports standard, fast-fail assertions. It also supports ["soft"
assertions], with which you can perform multiple checks and see all their
failures, not just the first.

Truth supports both of these, though its "soft assertions" API ([`Expect`]) does
not let you [divide a single test
method](https://github.com/google/truth/issues/266) into multiple "groups" as
AssertJ does. It additionally supports [assumptions] and custom
[`FailureStrategy`] implementations. This support also underlies its [utility
for testing user-defined assertion methods][`ExpectFailure`].

I had some problems with AssertJ's soft assertions:

-   They don't work with chained assertions like `last()`. Specifically, they
    fall back to behaving as fail-fast assertions.
-   They don't work on Android. The implementation of soft assertions uses
    bytecode generation, which doesn't work there.

These seem fixable in principle, but they demonstrate some of the reasons that
we chose to make `FailureStrategy` a fundamental part of Truth.

### Migration

AssertJ provides [a tool][AssertJ-migrator] to automatically migrate from JUnit
and other frameworks to AssertJ.

Truth has one, but it's only for JUnit, and it's currently only available inside
Google.

### Documentation

It's hard for me to compare the documentation of a library I already understand
well with one that's new to me. So I'll just say:

Both libraries are easy to use in the simple case.

Truth still needs more documentation for some of the complex cases.

### Bug-proneness

Both libraries have some sharp edges:

Under AssertJ, `assertThat(someLongThatIsEqualTo1).isNotEqualTo(1)` passes, even
though you probably didn't mean it to. Under Truth, it fails as intended.

Under Truth, `assertThat(listOfStrings).doesNotContain(integer)` passes, even
though your test is probably buggy. Under AssertJ, it doesn't compile.

### Conditions

AssertJ supports Hamcrest-style ["conditions."][conditions]

Truth does not. We encourage people to instead write [custom `Subject`
implementations](extension), which IDEs can better surface during autocompletion.

### And more

This list is not exhaustive. Let us know if you think we're missing something
significant.

## Examples

Some typical examples that can highlight the similarities and differences of the
common assertions can be found below.

### Equality

Framework | Code example
--------- | -----------------------------------------
Truth     | `assertThat(actual).isEqualTo(expected);`
AssertJ   | `assertThat(actual).isEqualTo(expected);`
Hamcrest  | `assertThat(actual, equalTo(expected));`
JUnit     | `assertEquals(expected, actual);`

### Custom error messages

Framework | Code example
--------- | --------------------------------------------------
Truth     | `assertWithMessage("custom msg").that(actual).isEqualTo(expected);`
AssertJ   | `assertThat(actual).overridingErrorMessage("custom msg").isEqualTo(expected);`
Hamcrest  | `assertThat("custom msg", actual, equalTo(expected));`
JUnit     | `assertEquals("custom msg", expected, actual);`

### Custom labeling

Framework | Code example
--------- | ------------------------------------------------------
Truth     | `assertThat(actual).named("foo").isEqualTo(expected);`
AssertJ   | `assertThat(actual).as("foo").isEqualTo(expected);`
Hamcrest  | n/a
JUnit     | n/a

### Null checking

Framework | Code example
--------- | ----------------------------------
Truth     | `assertThat(actual).isNull();`
AssertJ   | `assertThat(actual).isNull();`
Hamcrest  | `assertThat(actual, nullValue());`
JUnit     | `assertNull(actual);`

### Boolean checks

Framework | Code example
--------- | -------------------------------
Truth     | `assertThat(actual).isTrue();`
AssertJ   | `assertThat(actual).isTrue();`
Hamcrest  | `assertThat(actual, is(true));`
JUnit     | `assertTrue(actual);`

### Double comparisons {#floating-point}

Framework | Code example
--------- | ----------------------------------------------------------------------------
Truth     | `assertThat(actualDouble).isWithin(tolerance).of(expectedDouble);`
AssertJ   | `assertThat(actualDouble).isCloseTo(expectedDouble, Offset.offset(offset));`
Hamcrest  | `assertThat(actualDouble, closeTo(expectedDouble, error));`
JUnit     | `assertEquals(expectedDouble, actualDouble, delta);`

### Float comparisons

Framework | Code example
--------- | ----------------------------------------------------------------------------
Truth     | `assertThat(actualFloat).isWithin(tolerance).of(expectedFloat);`
AssertJ   | `assertThat(actualFloat).isCloseTo(expectedFloat, Offset.offset(offset));`
Hamcrest  | `assertThat(actualFloat, closeTo(expectedFloat, error));`
JUnit     | `assertEquals(expectedFloat, actualFloat, delta);`

### Assume/Assumption (JUnit's skipping behavior)

Framework | Code example
--------- | --------------------------------------------
Truth     | `assume().that(actual).isEqualTo(expected);`
AssertJ   | n/a
Hamcrest  | `assumeThat(actual, equalTo(expected));`
JUnit     | `assumeEquals(expected, actual);`

### Expect (fail-at-end)

Framework | Code example
--------- | ---------------------------------------------------------------
Truth     | `expect.that(actual).isEqualTo(expected); // supplied by @Rule`
AssertJ   | `softly.assertThat(actual).isEqualTo(expected); // @Rule with JUnitSoftAssertions`
Hamcrest  | n/a
JUnit     | n/a

<!-- References -->

[AssertJ]: http://joel-costigliola.github.io/assertj/
[FEST]: https://github.com/alexruiz/fest-assert-2.x
[Hamcrest]: https://code.google.com/p/hamcrest/
[Polish prefix notation]: http://en.wikipedia.org/wiki/Polish_notation
[`Expect`]: https://github.com/google/truth/blob/master/core/src/main/java/com/google/common/truth/Expect.java
["soft" assertions]: http://joel-costigliola.github.io/assertj/assertj-core-features-highlight.html#soft-assertions
[assumptions]: https://github.com/google/truth/blob/master/core/src/main/java/com/google/common/truth/TruthJUnit.java
[`FailureStrategy`]: https://github.com/google/truth/blob/master/core/src/main/java/com/google/common/truth/FailureStrategy.java
[`ExpectFailure`]: https://github.com/google/truth/blob/master/core/src/main/java/com/google/common/truth/ExpectFailure.java
[AssertJ-classes]: http://joel-costigliola.github.io/assertj/core-8/api/org/assertj/core/api/Assertions.html
[Truth-classes]: http://google.github.io/truth/api/0.34/com/google/common/truth/Truth.html
[AssertJ-Iterable]: http://joel-costigliola.github.io/assertj/core-8/api/org/assertj/core/api/AbstractIterableAssert.html
[Truth-Iterable]: http://google.github.io/truth/api/0.34/com/google/common/truth/IterableSubject.html
[Guava]: https://github.com/google/guava
[Protocol Buffers]: https://developers.google.com/protocol-buffers/
[GWT]: http://www.gwtproject.org/
[AssertJ-Android]: http://square.github.io/assertj-android/
[Truth-Android]: https://pkware.github.io/truth-android/
[AssertJ-migrator]: http://joel-costigliola.github.io/assertj/assertj-core-converting-junit-assertions-to-assertj.html
[AssertJ-generator]: http://joel-costigliola.github.io/assertj/assertj-assertions-generator.html
[conditions]: http://joel-costigliola.github.io/assertj/assertj-core-conditions.html
[reflective field comparisons]: http://joel-costigliola.github.io/assertj/assertj-core-features-highlight.html#field-by-field-comparison

