---
layout: default
title: SubjectBuilder design doc
---


This doc is intended for Truth developers (or curious users) who have experience
with the API and are interested in the internals that back it. New users should
refer to [the main usage docs](index), [the FAQ](faq) (including [this question
about the call chain][shortcuts]) and [the extension docs](extension).

## `assertThat`

Most Truth assertions use one of its `assertThat` methods:

```java
assertThat(colors).containsExactly(RED, GREEN, BLUE);
```

Each `assertThat` method returns a [`Subject`]―here, an [`IterableSubject`]. So
you could imagine this `assertThat` method has an implementation like the
following:

```java
public static IterableSubject assertThat(Iterable<?> actual) {
  return new IterableSubject(actual);
}
```

The static method is a cleaner API: It lets users write `assertThat(...)` rather
than `new IterableSubject(...)`, `new StringSubject(...)`, etc. But there is
more to Truth's API than `assertThat`. In the following sections, I'll explain
the rest of the API.

## Other parameters to `Subject`

A `Subject` contains more than just the value under test. Thus, `assertThat`
doesn't just delegate to the constructor; it also fills in sensible defaults for
the constructor's other parameters. You could imagine that its implementation
looks like this:

```java
public static IterableSubject assertThat(Iterable<?> actual) {
  return new IterableSubject(        // kind of Subject to create
      actual,                        // value under test
      throwAssertionErrorStrategy(), // default FailureStrategy
      "");                           // default message
}
```

This raises a question: How do callers specify values other than the defaults?
The answer is that they use Truth's full `Subject`-builder chain:

```java
expect                                                        // FailureStrategy
    .withMessage("relocate call did not update location")     // message
    .about(employees())                                       // kind of Subject to create
    .that(db.get(KURT.id())                                   // value under test
    .hasLocation(MTV);
```

And in fact, even [`assertThat`] is implemented with such a call chain, not with
my hypothetical direct call to [the constructor][`IterableSubject`] (a
constructor that is likewise slightly different than my hypothetical example).

The intermediate objects in the chain are, depending on the exact methods
called, instances of [`StandardSubjectBuilder`], [`SimpleSubjectBuilder`], and
[`CustomSubjectBuilder`]. But the interesting thing isn't the class names so
much as the order in which the values are specified.

## Order of chained calls

Why did we choose this order? I'll go over the steps of the chain in order,
explaining why they appear where they do.

### [`FailureStrategy`]

Quick refresher: The failure strategy tells Truth what to do what a check fails.
The most common strategy is [`assert_`], which means to throw an
`AssertionError`, but others exist (e.g., [`expect`], [`assume`]).

It's natural to put the strategy at the beginning of the chain. This makes all
assertions start with "`assert`," including those that use shortcuts like
`assertThat`.

The other natural choice would be to put the message first. But then we'd need a
special API for assertions with no custom message. That API is likely to be
verbose and to require parameters, which IDEs don't autocomplete as well as they
do with chained method calls:

```java
withoutMessage(assert_())
    .that(kurt)
    .hasLocation(MTV);
```

The alternative is to make `assert_()` itself work not just as a parameter but
also as a way of starting the chain:

```java
assert_()
    .that(kurt)
    .hasLocation(MTV);
```

But if we go that far, it's probably clearest to make it start the chain in all
cases, rather than serve as both a parameter and a standalone entry point.

Additionally, we'd like to permit chaining subjects to add messages but not to
change the `FailureStrategy` (except in limited ways). To permit this, we need
to expose an object to subject implemeters that permits setting a message but
has a `FailureStrategy` already set. This chaining model,
`check().withMessage(...)`, fits perfectly with the `assert_().withMessage(...)`
model.

There's one more reason that it's useful to have an object that supports
`withMessage` calls: We may someday add other methods that let callers to add to
the failure message. If so, we'll want for users to be able to call both
`withMessage` and those new methods, so both need to be possible to call in the
middle of the chain. Even today, we support multiple `withMessage` calls (with
the expectation that one might happen in a helper method and the other in the
test itself).

### Message

The message doesn't go at the beginning, as discussed above. We chose to make it
the second call, rather than putting it after `about` or after `that`:

After `about` isn't a good option because many assertion chains don't call
`about`. Additionally, supporting it after `about` would mean supporting it on
the `CustomSubjectBuilder` type, which would mean giving that type a self type,
an additional complication on a type that's already difficult to understand.

After `that` isn't a good option for three main reasons:

First, messages are often long, so they break the flow of the assertion chain.
We'd rather keep them out of the core of the assertion, `that(foo).isBar()`.

Second, putting the message after `that` would require `Subject` to have a self
type. Now, `Subject` *does* have a self type, but most subjects don't use it
correctly, including many of Truth's built-in subjects. Additionally, it adds
complexity. So we may still remove it.

Third, we'd need to avoid some of the other problems with `Subject.named`. It
currently mutates the `Subject`, supports only a single name (rather than the
multiple messages supported by `withMessage`), and is ignored by ~25% of
assertion implementations. This may be fixable, probably by passing a
`Subject.Factory` parameter to the `Subject` constructor, but it would further
complicate things.

Note that the message can't come after the assertion itself because, if the
assertion throws an exception, the `withMessage` call would never happen. (We
could delay throwing the exception until the `withMessage` call, but that would
require users to call `withMessage` even when they don't want a message. And if
they forget to call it, their tests will wrongly pass.)

### Kind of `Subject` to create

At this point, the only question is whether the order is `that(...).about(...)`
or `about(...).that(...)`. The latter keeps the core of the assertion together:
`that(foo).isBar()`. It also means that we can include shortcuts like
`assert_().that(...)` that directly return a `Subject`, not just some kind of
`SubjectBuilder`. (An alternative would be for the `that` shortcut to return a
`Subject` and for `Subject` to contain the `about` method so that callers can
change the `Subject` type afterward. But we'd need a way to check that the given
actual value is compatible with the factory passed to `about`. To do so, we'd
have to rely on many `Subject` subclasses to declare and use a type parameter
for the actual value.)

### Actual value

The only thing left to specify before the assertion itself is the actual value.
As noted above, this keeps the core of the assertion together:
`that(foo).isBar()`.

## Propagating values through the chain

This is easiest to consider in reverse.

The actual value is passed to the `Subject` constructor through the
`Subject.Factory`. It doesn't need to be stored anywhere.

The `Subject.Factory` is stored in the `SimpleSubjectBuilder` returned by
`about`. Once it's invoked by `that`, it's never needed again.

The message and `FailureStrategy` are the interesting part. Currently, we store
the message by creating a wrapper `FailureStrategy` that prepends the message
before calling the original strategy. We pass this `FailureStrategy` through the
chain, all the way into the `Subject` and even into any derived subjects.
Someday, we're likely to replace the `FailureStrategy` with a richer
`FailureContext` type that is opaque to users.

## Shortcuts

Most users are satisfied with the defaults: no failure message, a
`FailureStrategy` of "assert," and the `Subject` types supported by core Truth
and other `assertThat` methods. For this reason, we provide various [shortcuts]:

<object data="images/truthassertionflowchart.svg" type="image/svg+xml"></object>

<!-- References -->

[`Subject`]:    https://github.com/google/truth/blob/master/core/src/main/java/com/google/common/truth/Subject.java
[`IterableSubject`]:    https://github.com/google/truth/blob/master/core/src/main/java/com/google/common/truth/IterableSubject.java
[`IterableOfProtosSubject`]:         http://github.com/google/truth/blob/master/extensions/proto/src/main/java/com/google/common/truth/extensions/proto/IterableOfProtosSubject.java
[`ProtoTruth`]:         http://github.com/google/truth/blob/master/extensions/proto/src/main/java/com/google/common/truth/extensions/proto/ProtoTruth.java
[`CustomSubjectBuilder`]:    https://github.com/google/truth/blob/master/core/src/main/java/com/google/common/truth/CustomSubjectBuilder.java
[`CustomSubjectBuilder.Factory`]:    https://github.com/google/truth/blob/master/core/src/main/java/com/google/common/truth/CustomSubjectBuilder.java
[`StandardSubjectBuilder`]:    https://github.com/google/truth/blob/master/core/src/main/java/com/google/common/truth/StandardSubjectBuilder.java
[`SimpleSubjectBuilder`]:    https://github.com/google/truth/blob/master/core/src/main/java/com/google/common/truth/SimpleSubjectBuilder.java
[`assertThat`]: https://github.com/google/truth/blob/master/core/src/main/java/com/google/common/truth/Truth.java
[`FailureStrategy`]: https://github.com/google/truth/blob/master/core/src/main/java/com/google/common/truth/FailureStrategy.java
[`assert_`]: https://github.com/google/truth/blob/master/core/src/main/java/com/google/common/truth/Truth.java
[`expect`]: https://github.com/google/truth/blob/master/core/src/main/java/com/google/common/truth/Expect.java
[`assume`]: https://github.com/google/truth/blob/master/core/src/main/java/com/google/common/truth/TruthJUnit.java
[shortcuts]: faq#full-chain
