---
layout: default
title: "Extensions: CustomSubjectBuilder"
---


Most custom `Subject` classes follow [a simple pattern](extension). However, in
rare cases, you can provide a better API by defining your own `that` methods in
a subclass of [`CustomSubjectBuilder`]. We're aware of two cases in which this
is useful. Both of them affect only users who write a longhand assertion, like
`assertWithMessage(...).about(...).that(...)`, as opposed to the shortcut
`assertThat(...)`.

## Problem: Parameterized `Subject` classes

One use case for `CustomSubjectBuilder` is a `Subject` subclass with its own
type parameters. For example, [`IterableOfProtosSubject`] has a type parameter
`<M extends Message>`. Callers need a way to specify the type of `M` in
expressions like `assertWithMessage(...).about(...).that(...)`. The way to do
that is for the `that` method to have a type parameter: `<M>
IterableOfProtosSubject<M> that(M message)`. But [`SimpleSubjectBuilder`]'s
`that` method doesn't have a type parameter, so it won't work.

## Problem: Multiple `Subject` classes

Another case addresses a problem that's less severe but more common: what to do
when your library defines multiple `Subject` classes. The usual solution is to
declare a `Subject.Factory` for each:

```java
import static com.google.common.truth.extension.EmployeeSubject.employees;
import static com.google.common.truth.extension.TeamSubject.teams;
…
assertWithMessage("converter should look up username")
    .about(employees())
    .that(kurt)
    .hasUsername("kak");
…
assertWithMessage("query result should include skip-level employees")
    .about(teams())
    .that(javaTeam)
    .hasMember(kurt);
```

But this forces some users to import multiple factories. And it adds noise to
the assertion calls: Why draw readers' attention to the fact that the first
assertion is about an `Employee` and the second is about a `Team`? We'd like to
be able to write:

```java
import static com.google.common.truth.extension.HumanResourcesTruth.humanResources;
…
assertWithMessage("converter should look up username")
    .about(humanResources())
    .that(kurt)
    .hasUsername("kak");
…
assertWithMessage("query result should include skip-level employees")
    .about(humanResources())
    .that(javaTeam)
    .hasMember(kurt);
```

<!-- TODO(cpovirk): How do we feel about a multi-argument `that` method? -->

## Solution: How to declare custom `that` methods

Rather than create a `Subject.Factory` as described in step 2 of [the simple
pattern](extension), do the following:

1.  Declare a subclass of `CustomSubjectBuilder`:

    ```java
    public final class ProtoSubjectBuilder extends CustomSubjectBuilder {…}
    ```

    The class must be accessible to the tests that will use it―usually `public`.

    The class should usually be `final`. (Anyone who wants to provide access to
    additional `Subject` classes or other behavior can define a separate
    `CustomSubjectBuilder` or other API to expose it.)

    <!-- TODO(cpovirk): Would we recommend nesting this class inside a Subject
         if it built only one kind of Subject? -->

1.  Declare a constructor that takes a `FailureMetadata`:

    ```java
      ProtoSubjectBuilder(FailureMetadata metadata) {
        super(metadata);
      }
    ```

    The constructor need not be visible to users. They will create instances
    through a factory you'll create.

1.  Declare one or more `that` methods:

    ```java
      public <M extends Message> IterableOfProtosSubject<M> that(
          @Nullable Iterable<M> actual) {
        return new IterableOfProtosSubject<>(metadata(), actual);
      }
    ```

    The methods must be accessible to the tests that will use them―usually
    `public`.

    (You may need to make your `Subject` constructor package-private so that it
    is accessible from the builder.)

1.  Declare a static factory method that returns a
    [`CustomSubjectBuilder.Factory`].

    ```java
    public static CustomSubjectBuilder.Factory<ProtoSubjectBuilder> iterablesOfProtos() {
      return ProtoSubjectBuilder::new;
    }
    ```

    The `CustomSubjectBuilder.Factory` should usually be implemented with a
    method reference, as shown above. Even if it's not, your method should
    declare a return type of `CustomSubjectBuilder.Factory<YourType>`, not your
    implementation class.

    The static factory method must be accessible to the tests that will use
    it―typically, `public`.

    We recommend naming this method in the *plural form* (e.g.,
    `EmployeeSubject.employees()`, `PersonSubject.people()`, etc.).

    We recommend putting this method on your `Subject` class itself. Or, if your
    library defines multiple `Subject` subclasses, you may wish to create a
    single `CustomSubjectBuilder` with multiple `that` methods, one for each. If
    you go that direction, you'll probably create a single class (like
    [`ProtoTruth`]) that contains the `protos()` factory method that supports
    all the types. (And you'll use this same class to define all your
    `assertThat` methods.)

    Now users can treat your `CustomSubjectBuilder.Factory` just like a
    `Subject.Factory`:

    ```java
    import static com.google.common.truth.extension.proto.ProtoTruth.protos;
    …
    assertWithMessage("parser should have used last of multiple values")
        .about(protos())
        .that(parser.parse(bytesWithMultipleValues))
        .isEqualTo(expected);
    ```

    (But, as with the `Subject.Factory` approach, most users will use your
    `assertThat` shortcut instead.)

<!-- References -->

[`IterableOfProtosSubject`]:         https://google.github.io/truth/api/latest/com/google/common/truth/extensions/proto/IterableOfProtosSubject.html
[`ProtoTruth`]:         https://google.github.io/truth/api/latest/com/google/common/truth/extensions/proto/ProtoTruth.html
[`CustomSubjectBuilder`]:    https://google.github.io/truth/api/latest/com/google/common/truth/CustomSubjectBuilder.html
[`CustomSubjectBuilder.Factory`]:    https://google.github.io/truth/api/latest/com/google/common/truth/CustomSubjectBuilder.Factory.html
[`SimpleSubjectBuilder`]:    https://google.github.io/truth/api/latest/com/google/common/truth/SimpleSubjectBuilder.html

