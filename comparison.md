---
layout: default
title: Comparison
---


## Overview

As a [fluent] assertion library, Truth is similar to [AssertJ], which was forked
from [FEST]. An assertion written with those libraries looks like this:

```java
assertThat(notificationText).contains("testuser@google.com");
```

Truth differs significantly from [Hamcrest]. An assertion written with Hamcrest
looks like this:

```java
assertThat(notificationText, containsString("testuser@google.com"));
```


## Why create Truth when AssertJ already exists?

The reason is historical: AssertJ didn’t exist when we started Truth. By the
time it was created, we’d begun using Truth widely at Google, and we’d made some
decisions that would be difficult to retrofit onto AssertJ.

## Truth vs. Hamcrest

Because Truth and Hamcrest differ so significantly, I'll cover only the main
points:

-   Truth assertions are made with chained method calls, so IDEs can suggest the
    assertions appropriate for a given object.
-   Hamcrest is a more general "matching" library, used not only for making
    assertions but also for setting expections on mocking frameworks, with
    matchers composed together in arbitrary ways. But this flexibility requires
    complex generics and makes it hard for Hamcrest to produce readable failure
    messages.

## Truth vs. AssertJ

Again, the two are very similar. For anyone trying to decide between them, our
pitch is: We prefer Truth for its simpler API:

-   Truth provides fewer assertions, while still covering the most common needs
    of [Google’s codebase][monorepo]. Compare:
    -   number of types: [Truth][truth-api] vs. [AssertJ][assertj-api]
    -   number of assertions per type: for example, for `Iterable`:
        [Truth][`IterableSubject`] vs. [AssertJ][`AbstractIterableAssert`]
-   Truth aims to provide a single way to perform most tasks. This makes tests
    easier to understand, and it lets us spend more time improving core
    features.

## Truth vs. AssertJ, more details {#assertj-detail}

The two libraries differ in other, smaller ways, which I'll try to cover below.

I'll try to be objective, but I acknolwedge that I'm in a better position to
explain our own decisions than AssertJ's. If you identify something wrong,
missing, or misleading, please [let us know][bug].

### Stability

AssertJ, while it continues to add APIs, rarely removes them anymore.

Truth will stop removing APIs when we release 1.0, targeted for July 1, 2019.

### Number of assertion methods

AssertJ has more: more classes of assertions ([AssertJ][assertj-api],
[Truth][truth-api]) and more methods per class
([AssertJ][`AbstractIterableAssert`], [Truth][`IterableSubject`]). That includes
advanced features like [reflective field comparisons].

More is not *necessarily* better: It can make it harder to find what you're
looking for, and it can mean you need to learn to read multiple styles of tests
or understand the interaction of more features. But naturally, more features can
also mean more convenience and power.

<!-- TODO(cpovirk): Link to a doc about the "act vs. verify" distinction once we've written it, using PredicateAssert as an example. -->

### Failure messages

I argue that Truth overall has a better approach here. But I also acknowledge
that we haven't fully adopted that approach, so some of our assertion methods
still produce a wall of prose:

```
Not true that <{jan=1, feb=2, march=3}> contains exactly <{jan=1, march=33}>. It has the following entries with unexpected keys: {feb=2} and has the following entries with matching keys but different values: {march=(expected 33 but got 3)}
```

Most of our messages, though, have migrated to a key-value style:

```
value of    : projectsByTeam().valuesForKey(corelibs)
missing (1) : truth
───
expected    : [guava, dagger, truth, auto, caliper]
but was     : [guava, auto, dagger, caliper]
multimap was: {corelibs=[guava, auto, dagger, caliper]}
  at com.google.common.truth.example.DemoTest.testTruth(DemoTest.java:71)
```

This is similar to AssertJ's message:

```
java.lang.AssertionError:
Expecting:
  <["guava", "auto", "dagger", "caliper"]>
to contain exactly in any order:
  <["guava", "dagger", "truth", "auto", "caliper"]>
but could not find the following elements:
  <["truth"]>
  at com.google.common.truth.example.DemoTest.testTruth(DemoTest.java:71) <19 internal calls>
```

But I'll point to a few differences:

-   The Truth message has fewer quotes and brackets, plus no
    `java.lang.AssertionEror:` or `internal calls`.
-   The Truth message includes a `value of` line describing the value under
    test.
-   The Truth message includes the contents of the full multimap.

We can see other differences by making other assertions. For example, compare
AssertJ...

```
org.junit.ComparisonFailure: expected:<...        <version>0.4[5</version>
        <scope>test</scope>
        <exclusions>
          <exclusion>
            <!-- use the guava we're building. -->
            <groupId>com.google.guava</groupId>
            <artifactId>guava</artifactId>
          </exclusion>
        </exclusions>
      </dependency>
      <dependency>
        <groupId>com.google.truth.extensions</groupId>
        <artifactId>truth-java8-extension</artifactId>
        <version>0.45]</version>
        <...> but was:<...        <version>0.4[4</version>
        <scope>test</scope>
        <exclusions>
          <exclusion>
            <!-- use the guava we're building. -->
            <groupId>com.google.guava</groupId>
            <artifactId>guava</artifactId>
          </exclusion>
        </exclusions>
      </dependency>
      <dependency>
        <groupId>com.google.truth.extensions</groupId>
        <artifactId>truth-java8-extension</artifactId>
        <version>0.44]</version>
        <...>
```

...to Truth...

```
diff:
    @@ -7,7 +7,7 @@
           <dependency>
             <groupId>com.google.truth</groupId>
             <artifactId>truth</artifactId>
    -        <version>0.45</version>
    +        <version>0.44</version>
             <scope>test</scope>
             <exclusions>
               <exclusion>
    @@ -20,7 +20,7 @@
           <dependency>
             <groupId>com.google.truth.extensions</groupId>
             <artifactId>truth-java8-extension</artifactId>
    -        <version>0.45</version>
    +        <version>0.44</version>
             <scope>test</scope>
             <exclusions>
               <exclusion>
```

Or compare AssertJ...

```
java.lang.AssertionError:
Expecting:
  <[year: 2019
month: 7
day: 15
]>
to contain exactly in any order:
  <[year: 2019
month: 6
day: 30
]>
elements not found:
  <[year: 2019
month: 6
day: 30
]>
and elements not expected:
  <[year: 2019
month: 7
day: 15
]>
```

...to Truth...

```
value of:
    iterable.onlyElement()
expected:
    year: 2019
    month: 6
    day: 30

but was:
    year: 2019
    month: 7
    day: 15

```

Also note that the exception thrown by Truth is a `ComparisonFailure`
([useful for IDEs][intellij-diff]) in both of the last two cases, not just one
of the two as with AssertJ.

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

Kotlin users of Truth can emulate AssertJ-style chaining by using [`apply`]:

```java
assertThat(list).apply {
  contains(x, y)
  doesNotContain(z);
}
```

### Assertion methods for other libraries' types

AssertJ provides assertions for several libraries' types. As of this writing,
[its home page][AssertJ] lists Guava, Joda-Time, DB, Neo4j, and Swing.

Truth includes assertions for [Guava] and [Protocol Buffers].

Both have third-party extensions, such as for Android types
([AssertJ][AssertJ-Android], [Truth][Truth-Android]). I don't have a feel for
the overall size of each ecosystem.

### Platform support (Android, GWT) {#platforms}

AssertJ supports Android -- though I had to use 2.x because the dexer rejected
3.x, even when I used only `Java6Assertions`. Possibly this was an issue with my
build setup.

Truth supports Android in all its versions. The downside is that it requires you
to [look in a separate class](faq#java8) for Java 8 assertions.

Truth also supports [GWT].

### Writing your own assertion methods

Both support this. A few notes on differences:

-   The two are verbose in different places: AssertJ
    [requires][`AbstractOptionalAssert`] (at least by convention) an `abstract`
    superclass, verbose generics, and `return this;` at the end of each method;
    Truth [requires][`OptionalSubject`] a method that returns a
    `Subject.Factory` (generally implemented as a method reference) and an
    `actual` field.
-   Truth's `Subject` class provides some
    [convenience methods that build a failure message][`failWithActual`].
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
and other libraries to AssertJ.

Truth has one, but it's only for JUnit, and it's currently only available inside
Google.

### Bug-proneness

Both libraries have some sharp edges. For example:

Under AssertJ,
`assertThat(uniqueIdGenerator.next()).isNotSameAs(uniqueIdGenerator.next())` can
pass even if both `next()` calls return the same value. Under Truth, we've
chosen a different name for the method (`isNotSameInstanceAs`) to steer people
away from using it unless they mean to test reference equality.

Under Truth, `assertThat(listOfStrings).doesNotContain(integer)` passes, even
though your test is probably buggy. Under AssertJ, it doesn't compile. (Truth's
looser types are [occasionally useful][pull-575-thread], but they may be more
[trouble][`CollectionIncompatibleType`] than they're worth.) We plan to add
static analysis to [Error Prone] to catch such bugs.

### Conditions

AssertJ supports Hamcrest-style "[conditions]".

Truth mostly does not. We encourage people to instead write
[custom `Subject` implementations](extension), which IDEs can better surface
during autocompletion. However, we do offer similar functionality for
collections through our [`Correspondence`] class.

### And more

This list is not exhaustive. Let us know if you think we're missing something
significant.

<!-- References -->

[fluent]: https://en.wikipedia.org/wiki/Fluent_interface
[AssertJ]: https://joel-costigliola.github.io/assertj/
[FEST]: https://github.com/alexruiz/fest-assert-2.x
[Hamcrest]: http://hamcrest.org/JavaHamcrest/
[Polish prefix notation]: https://en.wikipedia.org/wiki/Polish_notation
[`Expect`]: https://truth.dev/api/latest/com/google/common/truth/Expect.html
["soft" assertions]: https://joel-costigliola.github.io/assertj/assertj-core-features-highlight.html#soft-assertions
[assumptions]: https://truth.dev/api/latest/com/google/common/truth/TruthJUnit.html#assume--
[`FailureStrategy`]: https://truth.dev/api/latest/com/google/common/truth/FailureStrategy.html
[`ExpectFailure`]: https://truth.dev/api/latest/com/google/common/truth/ExpectFailure.html
[Guava]: https://github.com/google/guava
[Protocol Buffers]: https://developers.google.com/protocol-buffers/
[GWT]: http://www.gwtproject.org/
[AssertJ-Android]: https://square.github.io/assertj-android/
[Truth-Android]: https://developer.android.com/training/testing/fundamentals#assertions
[AssertJ-migrator]: https://joel-costigliola.github.io/assertj/assertj-core-converting-junit-assertions-to-assertj.html
[AssertJ-generator]: https://joel-costigliola.github.io/assertj/assertj-assertions-generator.html
[conditions]: https://joel-costigliola.github.io/assertj/assertj-core-conditions.html
[reflective field comparisons]: https://joel-costigliola.github.io/assertj/assertj-core-features-highlight.html#field-by-field-comparison
[bug]: https://github.com/google/truth/issues/new
[`Correspondence`]: https://truth.dev/api/latest/com/google/common/truth/Correspondence.html
[truth-api]: https://truth.dev/api/latest/index.html
[assertj-api]: http://joel-costigliola.github.io/assertj/core-8/api/index.html
[`IterableSubject`]: https://truth.dev/api/latest/com/google/common/truth/IterableSubject.html
[`AbstractIterableAssert`]: http://joel-costigliola.github.io/assertj/core-8/api/org/assertj/core/api/AbstractIterableAssert.html
[`apply`]: https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/apply.html
[`failWithActual`]: https://truth.dev/api/latest/com/google/common/truth/Subject.html#failWithActual-java.lang.String-java.lang.Object-
[`AbstractOptionalAssert`]: https://github.com/joel-costigliola/assertj-core/blob/ced2937f8e1647ab7893adb9fc766e64b1ab20a9/src/main/java/org/assertj/core/api/AbstractOptionalAssert.java#L45
[`OptionalSubject`]: https://github.com/google/truth/blob/3740ee650867927a6d685dbf8d792e7cc0fcb328/extensions/java8/src/main/java/com/google/common/truth/OptionalSubject.java#L29
[monorepo]: https://cacm.acm.org/magazines/2016/7/204032-why-google-stores-billions-of-lines-of-code-in-a-single-repository/fulltext
[intellij-diff]: https://stackoverflow.com/q/48565168/28465
[Error Prone]: https://errorprone.info
[pull-575-thread]: https://github.com/google/truth/pull/575#discussion_r293444380
[`CollectionIncompatibleType`]: https://errorprone.info/bugpattern/CollectionIncompatibleType

