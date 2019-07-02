---
layout: default
title: Truth vs. AssertJ and Hamcrest
---


## Overview

Truth is similar to [AssertJ]. An assertion written with either library looks
like this:

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

## Truth vs. Hamcrest {#vs-hamcrest}

Because Truth and Hamcrest differ so significantly, we'll cover only the main
points:

-   Truth assertions are made with chained method calls, so IDEs can suggest the
    assertions appropriate for a given object.
-   Hamcrest is a more general "matching" library, used not only for making
    assertions but also for setting expections on mocking frameworks, with
    matchers composed together in arbitrary ways. But this flexibility requires
    complex generics and makes it hard for Hamcrest to produce readable failure
    messages.

## Truth vs. AssertJ {#vs-assertj}

Again, the two are very similar. We prefer Truth for its
[simpler API](#assertion-count):

-   Truth provides fewer assertions, while still covering the most common needs
    of [Google’s codebase][monorepo]. Compare:
    -   number of types: [Truth][truth-api] vs. [AssertJ][assertj-api]
    -   number of assertions per type: for example, for `Iterable`:
        [Truth][`IterableSubject`] vs. [AssertJ][`AbstractIterableAssert`]
-   Truth aims to provide a single way to perform most tasks. This makes tests
    easier to understand, and it lets us spend more time improving core
    features.

We also usually prefer Truth's [failure messages](#failure-messages) (though we
find AssertJ's to often be similar and, in some cases we're still working on, to
even be better).

Additionally, Truth works on Android devices [by default](#platforms), without
requiring users to use an older version or import a different class than usual.

## Truth vs. AssertJ, more details {#assertj-detail}

### Number of assertion methods {#assertion-count}

AssertJ has more: more classes of assertions ([AssertJ][assertj-api],
[Truth][truth-api]) and more methods per class
([AssertJ][`AbstractIterableAssert`], [Truth][`IterableSubject`]).

It's easy to understand how every extra feature can be a good thing. We have
found, though, that more is not always better:

-   When a library has more APIs, it's harder to find what you're looking for.
-   Users need to understand the behavior of many different methods.
-   Users have to choose between multiple ways of doing the same thing.
-   Different projects develop their own "dialects," so an assertion in one
    project may look different than an equivalent assertion in another.
-   Assertions that use a mixture of approaches (e.g., both chained method calls
    and composable matchers) can be harder to understand that assertions that
    stick to a single approach.
-   As a practical matter, when there are more features, it's hard to spend as
    much time on [designing](subject_named_actual_type_parameters) each API and
    failure message.

<!-- TODO(cpovirk): Link to a doc about the "act vs. verify" distinction once we've written it, using PredicateAssert as an example. -->

### Failure messages {#failure-messages}

We prefer Truth's (in most cases, at least). Here's an example:

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

But note a few differences:

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

### Platform support (Android, GWT) {#platforms}

Both libraries support Android. However, to use AssertJ on Android, you must
[fall back to AssertJ 2.x](https://github.com/joel-costigliola/assertj-core/issues/1474#issuecomment-477762965),
and you [can't use "soft assertions."][soft-assertions-android]

Truth supports Android in its main `Truth` class, and its equivalent to soft
assertions ([`Expect`]) works under Android. Truth also supports [GWT].

Both libraries have third-party extensions for Android types: AssertJ has
[AssertJ-Android] \(which is [deprecated][AssertJ-Android-deprecated]), and
Truth has [AndroidX Test][Truth-Android].

### Puzzlers {#puzzlers}

Here are some AssertJ puzzlers:

```java
assertThat(uniqueIdGenerator.next()).isNotSameAs(uniqueIdGenerator.next());
assertThat(primaryColors).containsAll(RED, YELLOW, BLUE);
assertThat(Longs.tryParse(s)).isEqualTo(parsedValues.get(s));
assertThat(event.getText())
    .usingComparator(comparing(Object::toString))
    .contains("2 suggestions");
assertThat(defaults).has(new Condition<>(x -> x instanceof String, "a string"));
```

Each of these behaves differently than the reader might expect. See if you can
figure them out, and then have a look at
[the puzzler answers](#puzzler-answers).

### Writing your own assertion methods

Both support this.

AssertJ is more verbose overall, [including][`AbstractOptionalAssert`] (at least
by convention) an `abstract` superclass, verbose generics, and `return this;` at
the end of each method. AssertJ also requires you to format failure messages
yourself.

While Truth has some boilerplate of its own, [including][`OptionalSubject`] a
method that returns a `Subject.Factory` (generally implemented as a method
reference) and an `actual` field, it is usually less. Also, Truth supplies
[convenience methods to format failure messages][`failWithActual`].

### Failure reporting

AssertJ supports standard, fast-fail assertions. It also supports
["soft assertions"], with which you can perform multiple checks and see all
their failures, not just the first.

Truth supports both of these. It additionally supports [assumptions] and custom
[`FailureStrategy`] implementations. This support also underlies its
[utility for testing user-defined assertion methods][`ExpectFailure`].

Note that AssertJ's soft assertions have some problems:

-   They don't work with chained assertions like `last()`. Specifically, they
    fall back to behaving as fail-fast assertions.
-   They [don't work on Android][soft-assertions-android].

These seem fixable in principle, but they demonstrate some of the reasons that
we chose to make `FailureStrategy` a fundamental part of Truth.

### Puzzler answers {#puzzler-answers}

(If you want to try to figure these out on your own, head back up to view
[the puzzlers without the answers](#puzzlers).)

```java
assertThat(uniqueIdGenerator.next()).isNotSameAs(uniqueIdGenerator.next());
```

This looks like it tests that each call to `next()` returns a different `long`.
However, it actually tests that each call returns a `long` _that autoboxes to a
distinct instance of `Long`_. Under a typical implementation of Java, this test
would pass even if `next()` were implemented as `return 12345;` because Java
will create a new `Long` instance after each invocation.

Truth reduces the chance of this bug by naming its method "isNotSameInstanceAs."

```java
assertThat(primaryColors).containsAll(RED, YELLOW, BLUE);
```

This looks like it tests that the primary colors are defined to be red, yellow,
and blue. However, it actually tests that the primary colors _include_ red,
yellow, and blue, along with possibly other colors.

Truth reduces the chance of this bug by naming its method "containsAtLeast."

```java
assertThat(Longs.tryParse(s)).isEqualTo(parsedValues.get(s));
```

This looks like it tests that the given string parses to the expected value.
However, if `parsedValues` is a `Map<String, Integer>` (perhaps because it's
shared with the tests of `Ints.tryParse`), then the test will always fail
because a `Long` is not equal to an `Integer`.

Truth reduces the chance of type-mismatch bugs by treating a `Long` as equal to
its equivalent `Integer`.

```java
assertThat(event.getText())
    .usingComparator(comparing(Object::toString))
    .contains("2 suggestions");
```

This looks like it tests that the `List<CharSequence>` returned by `getText()`
contains an element with content "2 suggestions." However, the `Comparator`
passed to `usingComparator` does not affect the `contains` call. (It affects
only calls like `isEqualTo`.) To apply a `Comparator` to `contains` and other
methods that operate on individual elements, AssertJ has a separate method,
`usingElementComparator`.

Truth avoids this problem by not permitting arbitrary assertions with a
`Comparator`.

```java
assertThat(defaults).has(new Condition<>(x -> x instanceof String, "a string"));
```

This looks like it tests that the `defaults` array contains a string. However,
it actually tests that `defaults` is _itself_ a string.

Truth avoids this problem by omitting support for `Condition`-style assertions
(except by using `Correspondence`, which is exposed only for assertions on
collection elements).

## A case for AssertJ {#for-assertj}

While we prefer Truth, we acknowledge that others may prefer AssertJ. Here are
some cases in which AssertJ offers advantages:

-   AssertJ offers a larger API.
    -   It offers more assertions. While we believe that Truth offers most of
        the most commonly needed assertions, some projects might make a lot of
        assertions about, say, `URI` objects, which Truth doesn't include
        built-in support for.
    -   It offers features like [reflective field comparisons]. We find that
        some of these can make code harder to maintain, but we grant that they
        can be convenient and safe in certain cases.
    -   It offers support for Hamcrest-style "[conditions]." We prefer to avoid
        this model for the same
        [reasons that we prefer Truth to Hamcrest](#vs-hamcrest).
-   While we prefer Truth's failure messages overall, there are cases in which
    AssertJ is still better. Consider this Truth message: `Not true that
    <{jan=1, feb=2, march=3}> contains exactly <{jan=1, march=33}>. It has the
    following entries with unexpected keys: {feb=2} and has the following
    entries with matching keys but different values: {march=(expected 33 but got
    3)}`
-   In order to support Android by default, Truth
    [makes it a little harder](faq#java8) to use assertions specific to Java 8
    types, like `Optional`.
-   Truth has a few puzzlers of its own. For example,
    `assertThat(listOfStrings).doesNotContain(integer)` passes, even though your
    test is probably buggy. Under AssertJ, it doesn't compile. (Truth's looser
    types are [occasionally useful][pull-575-thread], but they may be more
    [trouble][`CollectionIncompatibleType`] than they're worth.) We plan to add
    static analysis to [Error Prone] to catch such bugs.
-   If you're writing an extension, AssertJ offers [a tool][AssertJ-generator]
    to generate it for you.
-   AssertJ supports multiple assertion calls on the same object in the same
    statement: `assertThat(list).contains(x, y).doesNotContain(z);`. Truth does
    not. Both libraries support "chaining" in the sense of a method that returns
    a new asserter for a sub-object: For example, AssertJ supports
    `assertThat(list).last().isEqualTo(x);`. And Truth supports
    `assertThat(multimap).valuesForKey(x).containsExactly(y, z);`. Our
    philosophy has been that it's clearer to support only one kind of chaining,
    but we suspect that the AssertJ style is generally clear, too, and it can be
    convenient. Kotlin users of Truth can emulate AssertJ-style chaining by
    using [`apply`]: `assertThat(list).apply { contains(x, y) doesNotContain(z);
    }`
-   AssertJ provides [a tool][AssertJ-migrator] to automatically migrate from
    JUnit and other libraries to AssertJ. Truth has one, but it's only for
    JUnit, and it's currently only available inside Google.

<!-- References -->

[fluent]: https://en.wikipedia.org/wiki/Fluent_interface
[AssertJ]: https://joel-costigliola.github.io/assertj/
[FEST]: https://github.com/alexruiz/fest-assert-2.x
[Hamcrest]: http://hamcrest.org/JavaHamcrest/
[Polish prefix notation]: https://en.wikipedia.org/wiki/Polish_notation
[`Expect`]: https://truth.dev/api/latest/com/google/common/truth/Expect.html
["soft assertions"]: https://joel-costigliola.github.io/assertj/assertj-core-features-highlight.html#soft-assertions
[assumptions]: https://truth.dev/api/latest/com/google/common/truth/TruthJUnit.html#assume--
[`FailureStrategy`]: https://truth.dev/api/latest/com/google/common/truth/FailureStrategy.html
[`ExpectFailure`]: https://truth.dev/api/latest/com/google/common/truth/ExpectFailure.html
[Guava]: https://github.com/google/guava
[Protocol Buffers]: https://developers.google.com/protocol-buffers/
[GWT]: http://www.gwtproject.org/
[AssertJ-Android]: https://square.github.io/assertj-android/
[AssertJ-Android-deprecated]: https://github.com/square/assertj-android#deprecated
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
[soft-assertions-android]: https://github.com/joel-costigliola/assertj-core/issues/1493

