---
subtitle: SubjectBuilder design doc
layout: default
url: /subject_builder_design
---


## `assertThat`

Most Truth assertions use one of its `assertThat` methods:

```java
assertThat(colors).containsExactly(RED, GREEN, BLUE);
```

Each `assertThat` method returns a [`Subject`]―here, an [`IterableSubject`]. So you could imagine this `assertThat` method has an implementation like the following:

```java
public static IterableSubject assertThat(Iterable<?> actual) {
  return new IterableSubject(actual);
}
```

The static method is a cleaner API: It lets users write `assertThat(...)` rather than `new IterableSubject(...)`, `new StringSubject(...)`, etc. But that's not the whole story.

## Other parameters to `Subject`

A `Subject` contains more than just the value under test. Thus, `assertThat` doesn't just delegate to the constructor; it also fills in sensible defaults for the constructor's other parameters. You could imagine that its implementation looks like this:

```java
public static IterableSubject assertThat(Iterable<?> actual) {
  return new IterableSubject(    // kind of Subject to create
      actual,                    // value under test
      throwAssertionError(),     // default FailureStrategy
      "");                       // default message
}
```

This raises a question: How do callers specify values other than the defaults? The answer is that they use Truth's full `Subject`-builder chain:

```java
expect                                                        // FailureStrategy
    .withMessage("fields not copied from input %s", input)    // message
    .about(protos())                                          // kind of Subject to create
    .that(myBuilder)                                          // value under test
    .hasAllRequiredFields();
```

The intermediate objects in the chain are, depending on the exact methods called, instances of [`StandardSubjectBuilder`], [`SimpleSubjectBuilder`], and [`CustomSubjectBuilder`]. But the interesting thing isn't the class names so much as the order in which the values are specified.

## Order of chained calls

### `FailureStrategy`

It's natural to put the strategy (`assert`, `expect`, `assume`, etc.) at the beginning of the chain. This makes all assertions start with `assert`, including those that use shortcuts like `assertThat`.

The other natural choice would be to put the message first. However, if we were to do that, we'd need to expose `FailureStrategy` instances to users so that they can write:

```java
withMessage("fields not copied from input %s", input)
    .check(assert_())
    .about(protos())
    .that(myBuilder)
    .hasAllRequiredFields();
```

Or perhaps:

```java
withMessage(assert_(), "fields not copied from input %s", input)
    .about(protos())
    .that(myBuilder)
    .hasAllRequiredFields();
```

And we'd rather not expose `FailureStrategy` in this way. First, we'd prefer that users not need to know about `FailureStrategy` the class, only the general idea of "choosing a failure strategy." And second, we'd prefer to avoid parameters in general, since IDEs typically don't autocomplete them as well as they do with chained method calls.

We'd also need to provide a way to skip the message. That's not hard, but the obvious solution is verbose:

```java
withoutMessage(assert_())
    .about(protos())
    .that(myBuilder)
    .hasAllRequiredFields();
```

And the alternative is to make `assert_()` itself work as a way of starting the chain as well as as a parameter:

```java
assert_()
    .about(protos())
    .that(myBuilder)
    .hasAllRequiredFields();
```

But if we go that far, it's probably clearest to make it start the chain in all cases, rather than serve as both a parameter and a standalone entry point.

Furthermore, we'd like to permit chaining subjects to add messages but not to change the `FailureStrategy` (except in limited ways). To permit this, we need to expose an object to subject implemeters that permits setting a message but has a `FailureStrategy` already set. This chaining model, `check().withMessage(...)`, fits perfectly with the `assert_().withMessage(...)` model.

There's another reason that it's useful to have an object that supports `withMessage` calls: We may someday add a `withContext` operation. If so, we'll want for users to be able to call both `withMessage` and `withContext`, so both need to be possible to call in the middle of the chain. Even today, we support multiple `withMessage` calls (with the expectation that one might happen in a helper method and the other in the test itself).

### Message

The message doesn't go at the beginning, as discussed above, so the only other options are after `about` and after `that`.

After `about` isn't a good option because many assertion chains don't call `about`. Additionally, supporting it after `about` would mean supporting it on the `CustomSubjectBuilder` type, which would mean giving that type a self type, an additional complication on a type that's already difficult to understand.

That leaves only putting it after `that`. There are three main problems with this:

First, messages are often long, so they break the flow of the assertion chain. We'd rather keep them out of the core of the assertion, `that(foo).isBar()`.

Second, putting the message after `that` would require `Subject` to have a self type. Now, `Subject` *does* have a self type, but most subjects don't use it correctly, including many of Truth's built-in subjects. Additionally, it adds complexity. So we may still remove it.

Third, we'd need to avoid some of the other problems with `Subject.named`. It currently mutates the `Subject`, supports only a single name (rather than the multiple messages supported by `withMessage`), and is ignored by ~25% of assertion implementations. This may be fixable, probably by passing a `SubjectFactory` parameter to the `Subject` constructor, but it would further complicate things.

### Kind of `Subject` to create

At this point, the only question is whether the order is `that(...).about(...)` or `about(...).that(...)`. The latter keeps the core of the assertion together: `that(foo).isBar()`. It also means that we can include shortcuts like `assert_().that(...)` that directly return a `Subject`, not just some kind of `SubjectBuilder`. (An alternative would be for the `that` shortcut to return a `Subject` and for `Subject` to contain the `about` method so that callers can change the `Subject` type afterward. But we'd need a way to check that the given actual value is compatible with the factory passed to `about`. To do so, we'd have to rely on many `Subject` subclasses to declare and use a type parameter for the actual value.)

### Actual value

That leaves the actual value to come last.

## Propagating values through the chain

This is easiest to consider in reverse.

The actual value is passed to the `Subject` constructor through the `SubjectFactory`. It doesn't need to be stored anywhere.

The `SubjectFactory` is stored in the `SimpleSubjectBuilder` returned by `about`. Once it's invoked by `that`, it's never needed again.

The message and `FailureStrategy` are the interesting part. Currently, we store the message by creating a wrapper `FailureStrategy` that prepends the message before calling the original strategy. We pass this `FailureStrategy` through the chain, all the way into the `Subject` and even into any derived subjects. Someday, we're likely to replace the `FailureStrategy` with a richer `FailureContext` type that is opaque to users.

## Shortcuts

Most users are satisfied with the defaults: no failure message, a `FailureStrategy` of "assert," and the `Subject` types supported by core Truth and other `assertThat` methods. For this reason, we provide various shortcuts:

<object data="images/truthassertionflowchart.svg" type="image/svg+xml"></object>

<!-- References -->

[`IterableOfProtosSubject`]:         http://github.com/google/truth/blob/master/extensions/proto/src/main/java/com/google/common/truth/extensions/proto/IterableOfProtosSubject.java
[`ProtoTruth`]:         http://github.com/google/truth/blob/master/extensions/proto/src/main/java/com/google/common/truth/extensions/proto/ProtoTruth.java
[`CustomSubjectBuilder`]:    https://github.com/google/truth/blob/master/core/src/main/java/com/google/common/truth/CustomSubjectBuilder.java
[`CustomSubjectBuilderFactory`]:    https://github.com/google/truth/blob/master/core/src/main/java/com/google/common/truth/CustomSubjectBuilderFactory.java
[`SimpleSubjectBuilder`]:    https://github.com/google/truth/blob/master/core/src/main/java/com/google/common/truth/SimpleSubjectBuilder.java
