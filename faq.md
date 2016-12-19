---
subtitle: Questions and Answers
layout: default
url: /faq
---

1. auto-gen TOC:
{:toc}

## How do I use Truth with the new Java 8 types? {#java8}

First, make sure you're depending on `//third_party/java/truth:truth8`

Next, you will need to add *both* of the following static imports to your class:

```java
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;
...
// This assertion uses the Truth8.assertThat(java.util.Optional) overload.
Optional<String> javaUtilOptional = someStream.map(...).filter(...).findFirst();
assertThat(javaUtilOptional).hasValue("duke");

// This assertion uses the Truth.assertThat(com.google.common.base.Optional) overload.
Optional<String> guavaOptional = user.getMiddleName();
assertThat(guavaOptional).hasValue("alfred");
```

## Why do I get a "`cannot find symbol .someMethod("foo");`" error when testing a custom type? {#missing-import}

This is a symptom that you are passing the object being tested to the main
`Truth.assertThat(...)` overloads, which don't include the type you are testing,
and so you are matching `assertThat(Object)`.

To resolve this, either find the appropriate `assertThat(YourType)` overload and
statically import it ([such as you would have to do for Java8 types](#java8)),
or use the `assertAbout(someType())` mechanism, i.e.

```java
assertAbout(gwtHasVisibility()).that(view).isVisible();
```

## What if I have an import conflict with another `assertThat()` method? {#imports}

We recommend static importing Truth's `assertThat()` shortcut method. However,
if that causes an ambiguous method conflict, you can use the longhand form:
`assert_().that()`. The following statements are identical:

```java
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assert_;
...
String string = "google";
assertThat(string).endsWith("gle");
assert_().that(string).endsWith("gle");
```

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

**In general, please avoid helper methods that return Truth subjects**

Instead, return the *value-under-test* from your helper method:

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

A tell-tale sign that you might be violating this anti-pattern is if you are
importing a built-in Truth subject (e.g., `import
com.google.common.truth.BooleanSubject`). Instead of passing a Truth subject
around, you should be passing your *value-under-test*.

Note that this is just a guideline, not a hard and fast rule. If you're
including a failure message or configuring parameters on a subject (e.g.
`ProtoSubject`), then this may be an acceptable pattern.

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

## What's the difference between `containsAll` and `containsExactly` for iterables? {#exactly}

`containsAll` asserts that the iterable contains all of the expected elements.

`containsExactly` asserts that the iterable contains all of the expected
elements __and nothing else__.

For example:

```java
ImmutableList<String> abc = ImmutableList.of("a", "b", "c");

assertThat(abc).containsAllOf("a", "b", "c");   // passes
assertThat(abc).containsExactly("a", "b", "c"); // passes

assertThat(abc).containsAllOf("a", "b");        // passes
assertThat(abc).containsExactly("a", "b");      // fails
```

Also note that both of the methods are *order-independent assertions*. If you
want order to be checked, you must chain `.inOrder()` onto the end of your
assertion. For example:

```java
ImmutableList<String> abc = ImmutableList.of("a", "b", "c");

assertThat(abc).containsAllOf("c", "a", "b");             // passes
assertThat(abc).containsExactly("c", "a", "b");           // passes

assertThat(abc).containsAllOf("a", "b").inOrder();        // passes

assertThat(abc).containsAllOf("c", "a", "b").inOrder();   // fails
assertThat(abc).containsExactly("c", "a", "b").inOrder(); // fails
```

For more information, see b/18222873

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
`assertThat(42).isEqualTo(user.getId())`.

Here's another example of two ways to write an assertion:

```java
assertThat(ImmutableList.of("red", "white", "blue")).contains(user.getFavoriteColor()); // BAD
assertThat(user.getFavoriteColor()).isAnyOf("red", "white", "blue");                    // GOOD
```

You should always use the latter instead of the former.

Any time you see a *literal value* or *constant* inside the `assertThat` call,
you should stop and try to write the *inverse assertion*.

## How do I compare floating point numbers with Truth? {#floating-point}

See the [floating point comparisons](comparison#floating-point). For example:

```java
assertThat(actualDouble).isWithin(tolerance).of(expectedDouble);
```

## How is this different than JUnit, Hamcrest, Fest, AssertJ, etc.?

See the [comparison](comparison) page.

## How do I write my own Truth subject for my own type?

See the [extension](extension) page.

## Any other questions?

Please [contact us](index#more-information) or [ask a question]

<!-- References -->

[ask a question]: http://stackoverflow.com/questions/ask?tags=google-truth

