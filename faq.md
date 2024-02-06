---
layout: default
title: Truth FAQ
---

1. auto-gen TOC:
{:toc}

## How do I use Truth with the Java 8 types? {#java8}

Assertions on Java 8
types are available through overloads of `Truth.assertThat`, as well as
overloads of the `that` method used in chains like
`assertWithMessage(...).that(stream).isEmpty()`.

This also means that you no longer need a dependency on
`com.google.truth.extensions:truth-java8-extension:`.

Assertions about other types (like protobuf types) still sometimes require that
you use [the full fluent chain](#full-chain).

## Why do I get a "`cannot find symbol .comparingExpectedFieldsOnly();`" error for a type that should have `comparingExpectedFieldsOnly`? {#missing-import}

You need to static import the `assertThat` method for that type. (In this
example, you probably need to import [`ProtoTruth.assertThat`].)

The error you're seeing is telling you that `comparingExpectedFieldsOnly`
doesn't exist on the base `Subject` class, the one returned when you pass a type
that Truth doesn't recognize to `Truth.assertThat`. Once you import the
`assertThat` method that you want, your call will resolve to that method, and
you'll have an instance of the type with the method you're looking for.

## What if I have an import conflict with another `assertThat()` method? {#imports}

In most cases, you can static import both methods, and the compiler will select
the right one for each call.

If not, you can use one of the methods without static import.

## Referencing a Truth subject directly is _generally_ an anti-pattern {#subject-references}

If you find yourself referencing a Truth subject type, there's a good chance
that there's a cleaner way to write your code. For example, we've often see
folks write test helper methods like this:

```java
// Please don't do this!
private static BooleanSubject assertThatBooleanWpcs(
    WebProperty webProperty, WebPropertyChannelSetting.Key key) {
  return assertThat(
      webProperty.getBooleanWebPropertyChannelSetting(
          /* channel */ null, key, /* inherit */ true, /* useDefault */ false));
}
```

Then in their tests, they call their helper method and chain `.isTrue()` or
`.isFalse()` onto the end of it. E.g.,

```java
// Please don't do this!
assertThatBooleanWpcs(primaryProperty, PUBLISHER_GENERAL_CATEGORY_FILTERING).isTrue();
assertThatBooleanWpcs(primaryProperty, PUBLISHER_CATEGORY_FILTERING).isFalse();
```

**In general, please avoid helper methods that return Truth subjects.**

Instead, return the *object under test* from your helper method:

```java
private static boolean getBooleanWpcs(
    WebProperty webProperty, WebPropertyChannelSetting.Key key) {
  return webProperty.getBooleanWebPropertyChannelSetting(
      /* channel */ null, key, /* inherit */ true, /* useDefault */ false);
}
```

...and then in your tests, call your helper method inside `assertThat(...)`:

```java
assertThat(getBooleanWpcs(primaryProperty, PUBLISHER_GENERAL_CATEGORY_FILTERING)).isTrue();
assertThat(getBooleanWpcs(primaryProperty, PUBLISHER_CATEGORY_FILTERING)).isFalse();
```

...or even go one step further and extract the *object-under-test* into a local
variable:

```java
boolean generalCategoryFilter = getBooleanWpcs(primaryProperty, PUBLISHER_GENERAL_CATEGORY_FILTERING);
assertThat(generalCategoryFilter).isTrue();

boolean categoryFilter = getBooleanWpcs(primaryProperty, PUBLISHER_CATEGORY_FILTERING);
assertThat(categoryFilter).isFalse();
```

A tell-tale sign that you might be violating this anti-pattern is if you're
importing a built-in Truth subject (e.g., `import
com.google.common.truth.BooleanSubject`). Instead of passing a Truth subject
around, you should be passing your *object-under-test*.

Note that this is just a guideline, not a hard and fast rule. If you're
including a failure message or configuring parameters on a subject (e.g.
`ProtoSubject`), then this may be an acceptable pattern.

Still, there are advantages to direct assertions over methods that return a
`Subject` -- and over helper methods that create a `Subject` and perform
assertions _without_ returning it. Here are some advantages of direct assertions
over various kinds of wrapping:

-   Truth can more often generate a useful "value of" line.
-   The components of the assertion (e.g., `button.getText()`, `contains`, and
    `OK`) are likely to appear near one other in code (and in the usual order).
-   Existing static analysis is more likely to detect bugs.
-   The "act" and "assert" phases of a test are easier to keep separate.

## When writing my own Truth extension, how should I name my assertion methods? {#assertion-naming}

*   If you're checking a property (e.g., `name`), use a `has` prefix (e.g.,
    `hasName(String name)`).
*   If you're testing a state (e.g., `isCeo`), use a `is` prefix (e.g.,
    `isCeo()` and `isNotCeo()`). Alternatively, you may wish to provide a single
    `isCeo(boolean isCeo)` method instead of writing two methods (however, this
    will generally make your statements slightly less readable).
*   If you're returning a new subject for a property of the instance under test,
    do not use a prefix (e.g., use `name()` for a method on `EmployeeSubject`
    that returns a `StringSubject`:
    `assertThat(employee).name().startsWith(...)`).
*   **When in doubt, prefer method names that make the assertion statements
    "sound natural".**

## What's the difference between `containsAtLeast` and `containsExactly` for iterables? {#exactly}

`containsAtLeast` asserts that the iterable contains _at least_ the expected
elements.

`containsExactly` asserts that the iterable contains all of the expected
elements __and nothing else__.

For example:

```java
ImmutableList<String> abc = ImmutableList.of("a", "b", "c");

assertThat(abc).containsAtLeast("a", "b", "c"); // passes
assertThat(abc).containsExactly("a", "b", "c"); // passes

assertThat(abc).containsAtLeast("a", "b");      // passes
assertThat(abc).containsExactly("a", "b");      // fails
```

Also note that both of the methods are *order-independent assertions*. If you
want order to be checked, you must chain `.inOrder()` onto the end of your
assertion. For example:

```java
ImmutableList<String> abc = ImmutableList.of("a", "b", "c");

assertThat(abc).containsAtLeast("c", "a", "b");           // passes
assertThat(abc).containsExactly("c", "a", "b");           // passes

assertThat(abc).containsAtLeast("a", "b").inOrder();      // passes

assertThat(abc).containsAtLeast("c", "a", "b").inOrder(); // fails
assertThat(abc).containsExactly("c", "a", "b").inOrder(); // fails
```

## Does it matter if I write `assertThat(a).isEqualTo(b)` or `assertThat(b).isEqualTo(a)`? {#order}

Yes! Truth's error messages will make more sense if you always use this pattern:

```java
assertThat(actualValue).isEqualTo(expectedValue);
```

In most cases, the **actual value** will be a call to the *code under test* and
the **expected value** will be a *literal value* or a *constant*. For example:

```java
assertThat(user.getId()).isEqualTo(42);      // literal value
assertThat(user.getId()).isEqualTo(USER_ID); // constant value
```

Please **do not** write the above assertion as

```java
assertThat(42).isEqualTo(user.getId())
```

Here's another example of two ways to write an assertion:

```java
assertThat(ImmutableList.of("red", "white", "blue")).contains(user.getFavoriteColor()); // BAD
```
```java
assertThat(user.getFavoriteColor()).isAnyOf("red", "white", "blue");                    // GOOD
```

You should always use the latter instead of the former.

Tip: Any time you see a *literal value* or *constant* inside the `assertThat` call,
you should stop and try to write the *inverse assertion*.

## How do I compare floating point numbers with Truth? {#floating-point}

See the [floating point comparisons](floating_point). For example:

```java
assertThat(actualDouble).isWithin(tolerance).of(expectedDouble);
```

## How do I assert on the contents of an `Iterable` using something other than equality of the elements? {#fuzzy}

See the ["Fuzzy Truth"](fuzzy) page.

## How is this different than JUnit, Hamcrest, Fest, AssertJ, etc.?

See the [comparison](comparison) page.

## How do I write my own Truth subject for my own type?

See the [extension](extension) page.

## How do I specify a custom message/failure behavior/`Subject` type? {#full-chain}

While you can usually call `assertThat`, advanced features require you to write
a longer chain of calls. We'll cover the most common shortcuts first, then the
general case, and finally the less common shortcuts.

**No custom parameters:** The simplest case is an assertion with no message on a type that Truth supports
natively. There, you can use `assertThat`:

```java
import static com.google.common.truth.Truth.assertThat;
...
assertThat(usernames).containsExactly("kak");
```

**Extension `Subject` only:** most [external `Subject` types](extension) provide
an `assertThat` overload you can import (and one should be
[added](extension#writing-your-own-truth-extension) if it's missing):

```java
import static com.google.common.truth.extension.EmployeeSubject.assertThat;
...
assertThat(kurt).hasLocation(NYC);
```

**Custom message only:** Use `assertWithMessage`. As you'll see later, `assertWithMessage` is the entry
point to use almost any time you want a custom message.

```java
import static com.google.common.truth.Truth.assertWithMessage;
...
assertWithMessage("findClosestMatch should have found user with given username")
    .that(db.findClosestMatch("kak"))
    .isEqualTo(kurt);
```

**The general case:** Once you've learned the common cases above, the easiest way to learn the rest is
to learn the full call chain. Even shortcuts like `assertThat` are implemented
using that chain. It looks like this:

```java
import static com.google.common.truth.extension.EmployeeSubject.employees;
...
expect // set what to do upon failure (that is, the FailureStrategy)
    .withMessage("findClosestMatch should have found user with given username") // set message
    .about(employees()) // set the type of value to test. The parameter is a Subject.Factory
    .that(db.findClosestMatch("kak")) // set the actual value under test
    .hasUsername("kak");
```

If you're curious why we chose that order, you can read [this design
doc](subject_builder_design). But most users will just be interested in the
shortcuts:

**Custom message and extension `Subject`:** `assertWithMessage(...).about(...).that(...)`

**Custom failure behavior:** `expect.that(...)`

For a list of built-in behaviors, see the docs on [`FailureStrategy`].

**Custom failure behavior and custom message:** `expect.withMessage(...).that(...)`

**Custom failure behavior and extension `Subject`:** `expect.about(...).that(...)`

**Custom `Subject` that doesn't expose an `assertThat` shortcut:** `assertAbout(...).that(...)`

## Any other questions?

[Ask your question here], or see more [contact info](index#more-information).

<!-- References -->

[Ask your question here]: http://stackoverflow.com/questions/ask?tags=google-truth
[`FailureStrategy`]: https://truth.dev/api/latest/com/google/common/truth/FailureStrategy.html
[`ProtoTruth.assertThat`]: https://truth.dev/api/latest/com/google/common/truth/extensions/proto/ProtoTruth.html#assertThat(com.google.protobuf.Message)
