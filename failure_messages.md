---
layout: default
title: Writing failure messages for a Truth Subject
---

This page contains guidelines for writing Truth failure messages.

## Facts

Truth failure messages are composed of key-value pairs, which we call "facts."
For example, here's a message with 2 facts:

```none
expected:  0.0
but was : -0.0
```

And here's another with the same 2 keys but different values. Truth displays it
differently because the values contain newlines:

```none
expected:
    <!doctype html>
    <html>
    </html>
but was:
    <html>
    </html>
```

## Keys vs. values

The general pattern is: Keys are fixed strings, and values are runtime values.

### Keys are fixed strings

When a `Subject` implementation reports a failure, it generally passes a fixed
set of keys.

In the normal case, there will be one fact describing the expected value. Its
name is often similar to the name of the assertion method:

```none
expected to be greater than:  // for isGreaterThan(...)
expected to contain:          // for contains(...)
expected:                     // for isEqualTo(...)
```

That fact is usually followed by a "but was" fact, as in the examples at the top
of the page. (Occasionally, there's no need for "but was." For example, take
"expected not to be null." Adding "but was null" would add no information.)

### Values are runtime values

Typically, they're the parameters the test passed in. For example:

-   The value of "expected" is the parameter passed to `isEqualTo(...)`.
-   The value of "but was" is the parameter passed to `assertThat(...)`.

Sometimes the facts contain more than just the `toString()` representation of a
parameter. For example:

```none
expected            : true
an instance of class: java.lang.Boolean
but was             : (non-equal value with same toString())
an instance of class: java.lang.String
```

Or:

```none
expected instance of: java.lang.CharSequence
but was instance of : java.lang.Double
with value          : 4.5
```

But even in unusual cases, the first fact usually describes the expected value.
Its key usually includes "expected," and its value is usually a parameter to the
assertion method.

### Don't mix the two

This key-value pattern is easy to extend to most assertions:

```none
expected to start with: abc
but was               : abd
```

```none
expected to start with:
    <html>
    <body>
but was:
    <html>
    <head></head>
    <body></body>
    </html>
```

The main point is to separate fixed strings from runtime values. If you don't,
users may have more trouble identifying the actual and expected values. For
example:

```none
// NOT RECOMMENDED!
expected: to start with abc
but was : abd
```

```none
// NOT RECOMMENDED!
expected:
    to start with <html>
    <body>
but was:
    <html>
    <head></head>
    <body></body>
    </html>
```

Because the fixed string "to start with" is part of the value instead of the
key, the expected prefix is no longer aligned with the actual value.
Additionally, the first key is now named "expected," a name that is normally
reserved for equality assertions. A user might initially read these failure
messages as saying that the values were expected to literally be "to start with
abc" and "to start with &lt;html&gt;\n&lt;body&gt;." And to top it all off, the
code in your `Subject` implementation has to be slightly more complex because
it's concatenating strings.

The main downside of this pattern is that the keys can sometimes be long.

## Assertions about a derived object

Some assertion methods test a specific part of the original object. For example:

```java
assertThat(list).hasSize(5);
assertThat(optional).hasValue("foo");
```

These methods should delegate to a `Subject` for the derived property:

```java
public void hasSize(int size) {
  // ... check that the actual value isn't null ...
  check("size()").that(actual.size()).isEqualTo(size);
}

public void hasValue(Object value) {
  // ... check that the actual value isn't null, check isPresent() ...
  check("get()").that(actual.get()).isEqualTo(value);
}
```

Such code produces messages like:

```none
value of: optional.get()
expected: boo
but was : foo
```

Delegating to another `Subject` offers several advantages:

-   You don't need to reimplement the failure messages of methods like
    `isEqualTo()`, which contains special cases for objects with identical
    `toString()` values, different classes, etc.
-   For `isEqualTo()` in particular, you automatically get a `ComparisonFailure`
    with diff-style output when appropriate.
-   You don't need to reimplement the logic of the checks. In particular, you
    don't need to handle unusual cases like `null`, `double` equality, and array
    equality.
-   The failure message focuses on the "expected" and "but was" values of the
    sub-object, with context available but on a separate line, merged with any
    context from other derived steps.

<!-- TODO(cpovirk): One thing that could go either way: If I implement my own assertion, I control how the actual and expected sub-objects are converted to display strings. That can be convenient if their toString() representations are poor. But an even better solution might be to implement a Subject subclass for them. That way, my display-string logic can be reused. -->

## Phrasing of keys

In addition to the points above, we suggest:

### Consider omitting "to be"

In some of the examples above, the key implicitly contains "to be." For example:

```none
expected: 0.0
expected instance of: java.lang.CharSequence
```

These could be written as:

```none
expected to be: 0.0
expected to be an instance of: java.lang.CharSequence
```

We recommend omitting "to be" when the key is clear without it. This keeps keys
short, which encourages users to read them fully in case they contain more
interesting information. (We omit "an" in "an instance of" for similar reasons.)

In some cases, omitting words is less clear of a win. For example:

```none
// somewhat discouraged
expected starts with: abc
```

This phrasing might suggest that the test supplied a complete expected value but
that Truth is displaying only the begining of it. We suggest:

```none
expected to start with: abc
```

Additionally, negative assertions seem to read better with "expected _not to
be_..." than just "expected _not_...."

### "expected to be empty" vs. "expected an empty string"

Most `Subject` implementations requre a certain kind of object -- in this
example, a string. Saying "expected an empty string" suggests that the test had
the ability to pass a non-string to the assertion, such as:

```java
assertThat(someInt).isEmpty();
```

But such a call wouldn't compile. Thus, failure messages shouldn't emphasize the
_type_ of value that was expected so much as the _properties_ of the value.

(It may still be useful to _mention_ the type, as in "expected string to be
empty." We don't have a recommendation for whether to prefer this or "expected
to be empty.")

There's one notable exception: when the value under test is unexpectedly null.
In this case, the value isn't really of the expected type. Thus, it makes sense
to say "expected a string that... but was null."

### Occasionally use a key with no value

In some cases, there is nothing more to say:

```none
expected a present optional
```

This could be written as:

```none
// NOT RECOMMENDED!
expected: a present optional
```

But usually "expected" is reserved for values, not descriptions of properties
that the values are expected to have.

### Don't say "expected to be equal to"

Just say "expected." "to be equal to" is assumed.

Similarly, don't say "expected to be equal to one of." Say "expected to be one
of."

The default assumption is that values are compared using equality. Including
"equal to" boilerplate makes it harder to users to spot unusual cases when they
do arise, like "expected specific instance."
