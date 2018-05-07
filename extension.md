---
layout: default
title: Extension
---


## Extension points

Truth is configurable in two ways: custom failure behaviors and custom assertion
methods.

Configurable [failure behaviors][`FailureStrategy`] can be useful, particularly
[the alternative built-in behaviors][`FailureStrategy`]. Or you can [write your
own][`FailureStrategy.fail`].

But when people talk about Truth extensions, they're usually referring to custom
assertion methods, implemented on a custom [`Subject`] subclass. That's what
we'll cover in the rest of this page.

## Some commonly used custom subjects

Some subjects aren't part of core Truth but can be found in other parts of the
project. They include:

*   [`Truth8`] for java8 types such as `java.util.Optional`
*   [`ProtoSubject`] for `Message` style protocol buffers
    *   A [`LiteProtoSubject`] with only lite dependencies is also provided.

Other extensions that are not part of the Truth project itself include:

*   [Compile Testing] for testing annotation processors and compilation jobs


## Using custom subjects

The steps are nearly the same as [for using the core Truth assertions](index):

### 1. Add the appropriate dependency to your build file:

For example, for Protocol Buffers, `com.google.truth.extensions:truth-proto-extension:{{ site.version }}`. Of course, you can skip this step if you define the `Subject` in the same project as the tests that use it.


### 2. Add a static import:

```java
import static com.google.common.truth.extensions.proto.ProtoTruth.assertThat;
```

### 3. Write a test assertion:

```java
assertThat(myBuilder).hasAllRequiredFields();
```

If you need to set a failure message or use a custom [`FailureStrategy`], you'll
instead need to find the extension's `Subject.Factory`. For an extension named
`FooSubject`, the factory is usually `FooSubject.foos()`. In the case of the
Protocol Buffers extension, it's `ProtoTruth.protos()`. So, to use the
[`expect`] `FailureStrategy` and a custom message in a check about Protocol
Buffers, you would write:

```java
import static com.google.common.truth.extensions.proto.ProtoTruth.protos;
…
expect
    .withMessage("fields not copied from input %s", input)
    .about(protos())
    .that(myBuilder)
    .hasAllRequiredFields();
```

But in most cases you'll use shortcuts, either `assertThat(...)` or
`assertWithMessage(...).about(...).that(...)`. For more information about the
available shortcuts, see [this FAQ entry][shortcuts].

## Writing your own subject

For an example of how to support custom types in Truth, please see the [employee
example]. The rest of this doc will walk through each of the files, step by
step.

There are four parts to the example:

1.  [`Employee.java`]

    This is the *class under test*. There's really nothing exciting about this
    file. In this case, it's just a plain old Java object (*POJO*), implemented
    using [`@AutoValue`].

2.  [`EmployeeSubject.java`]

    This is the custom Truth subject, and the most interesting part of this
    example. There are several
    important things to note about this file:

    1.  Every custom subject must extend from [`Subject`] or one of its
        subclasses. Your class definition will usually look like this:

        ```java
        public final class EmployeeSubject extends Subject<EmployeeSubject, Employee> {…}
        ```

        The class must be accessible to the tests that will use it. Typically
        that means making it `public`.

        We suggest making the class `final` if possible. If you attempt to
        subclass a `Subject`, you will likely run into problems with generics.
        We hope to make `Subject` class hierarchies work better in the future.

        Tip: What if your `Subject` class has a type parameter, like
        [`ComparableSubject`]? Follow our instructions for
        [`CustomSubjectBuilder`](custom_subject_builder) instead of step 2
        below, and then return to the instructions at step 3.

    2.  A subject also needs to define a [`Subject.Factory`], exposed through a
        static method. The definition is usually boilerplate:

        ```java
        public static Subject.Factory<EmployeeSubject, Employee> employees() {
          return EmployeeSubject::new;
        }
        ```

        Like your `Subject` class itself, this static method must be accessible
        to the tests that will use it―typically, `public`.

        We recommend naming this method in the *plural form* (e.g.,
        `EmployeeSubject.employees()`, `PersonSubject.people()`, etc.).

        We recommend putting this method on your `Subject` class itself.

        The `Subject.Factory` should usually be a method reference, as shown
        above. In particular, you should *not* expose the *type* to users.
        Instead, you expose only an *instance* through a static factory method.

        By passing your `Subject.Factory` to an `about()` method, users can
        perform all the operations that they expect of a built-in `Subject`
        type. For example, they can set a failure message:

        ```java
        import static com.google.common.truth.extension.EmployeeSubject.employees;
        …
        assertWithMessage("findClosestMatch should have found user with given username")
            .about(employees())
            .that(db.findClosestMatch("kak"))
            .hasUsername("kak");
        ```

        But users won't need those operations most of the time, so offer them a
        shortcut:

    3.  For users' convenience, define a static `assertThat(Employee)` shortcut
        method:

        ```java
        public static EmployeeSubject assertThat(@Nullable Employee actual) {
          return assertAbout(employees()).that(actual);
        }
        ```

        Like your `Subject`, your `assertThat` method must be accessible to the
        tests that will use it―typically, `public`.

        We recommend putting this method on your `Subject` class itself. Or, if
        your library defines multiple `Subject` subclasses, you may wish to
        create a single class (like [`ProtoTruth`]) that contains all your
        `assertThat` methods so that users can access them all with a single
        static import.

        Users can statically import your method alongside Truth's `assertThat`
        methods. Static imports with the same name follow the same
        overload-resolution rules as normal Java overloads, so the imports can
        coexist unless in a file unless it makes a call that's ambiguous.

        (If your users do end up with an ambiguous reference, they can instead
        use the `Subject.Factory` (`assertAbout(employees()).that(...)`) or use
        the `assertThat` method without static imports
        (`EmployeeSubject.assertThat(...)`).)

    4.  Your custom `Subject` class must have a constructor that accepts a
        [`FailureMetadata`] and a reference to the *instance under test*. Pass
        both to the superclass constructor:

        ```java
        private EmployeeSubject(FailureMetadata metadata, @Nullable Employee actual) {
          super(metadata, actual);
        }
        ```

        If your `Subject` is `final`, the constructor can be `private`. But even
        if you want an extensible `Subject`, there's no reason for the
        constructor to be `public`, only package-private or `protected`: No one
        should call the constructor directly except the `Subject.Factory` and
        subclasses.

    5.  Finally, you define your test assertion API on the custom `Subject`.
        Since you're defining the API, you can write it however you'd like.
        However, we recommend method names that will make the assertions read
        *as close to English as possible*. For some advice on naming assertion
        methods, please see [here](faq#assertion-naming).

        As for the implementation: Most assertion implementations employ one of
        the two basic approaches. The simpler approach is to delegate to an
        existing assertion. To do so, use `Subject.check()`, which preserves the
        caller-specified `FailureStrategy` and other context. For example:

        ```java
        public void hasName(String name) {
          check("name()").that(actual().name()).isEqualTo(name);
        }
        ```

        This gives Truth enough information to construct a failure message like:

        ```none
        value of    : employee.name()
        expected    : Sundar Pichai
        but was     : Kurt Alfred Kluever
        employee was: Employee{username=kak, name=Kurt Alfred Kluever, isCeo=false}
        ```

        But sometimes you can't delegate to an existing assertion. Or you could,
        but the failure message would be poor. In such cases, you can take the
        other approach -- manually perform your check and report a failure. For
        example:

        ```java
        public void isCeo() {
          if (!actual().isCeo()) {
            failWithActual(simpleFact("expected to be CEO"));
          }
        }
        ```

        This lets Truth produce a failure message like:

        ```none
        expected to be CEO
        but was: Employee{username=kak, name=Kurt Alfred Kluever, isCeo=false}
        ```

        This message contains all the relevant information, yet it's much
        shorter than the `hasName(…)` example above. This is possible because
        `isCeo()` is testing a boolean property. Boolean properties are good
        candidates for the manual check-and-fail approach.

        One advanced point: Custom `Subject` types sometimes declare "chaining"
        methods that return an instance of another `Subject`. For example,
        instead of providing `hasName(…)`, `EmployeeSubject` might declare a
        `EmployeeSubject.name()` method that returns a `StringSubject` for the
        value of `employee.name()`. To create such "chained" subjects, use
        `Subject.check()`, as above, and return the `Subject` you create:

        ```java
        public StringSubject name() {
          return check("name()").that(actual().name());
        }
        ```

        Now callers can write:

        ```java
        assertThat(kak).name().matches("Kurt .*Kluever");
        ```

3.  [`EmployeeSubjectTest.java`]

    This is a test of the subject, which tests that its assertions pass when
    they should and fail when they should:

    ```java
    import static com.google.common.truth.ExpectFailure.expectFailureAbout;
    import static com.google.common.truth.extension.EmployeeSubject.assertThat;
    import static com.google.common.truth.extension.EmployeeSubject.employees;

    import com.google.common.truth.ExpectFailure.SimpleSubjectBuilderCallback;

    @RunWith(JUnit4.class)
    public final class EmployeeSubjectTest {
      private static final Employee KURT =
          Employee.create("kak", 37802, "Kurt Alfred Kluever", Location.NYC, false);

      @Test
      public void id() {
        assertThat(KURT).hasId(37802);
        expectFailure(whenTesting -> whenTesting.that(KURT).hasId(12345));
      }
      …
      private static AssertionError expectFailure(
          SimpleSubjectBuilderCallback<EmployeeSubject, Employee> callback) {
        return expectFailureAbout(employees(), callback);
      }
    }
    ```

    Testing that assertions pass is easy: Just make assertions with your
    `assertThat` method.

    Testing that assertions fail is fairly easy, too, with Truth's
    [`ExpectFailure`] utility. The example above shows how to define a helper
    method to make your `ExpectFailure` calls even shorter.

    If you want to test more thoroughly, `expectFailure` returns an
    `AssertionError`, so you can write:

    ```java
    import static com.google.common.truth.ExpectFailure.assertThat;

    assertThat(failure).factValue("value of").isEqualTo("employee.username()");
    ```

    `ExpectFailure` also provides an API for users who can't use lambdas. See
    its docs for details.

4.  [`FakeHrDatabaseTest.java`]

    This is an example of how your unit tests will look using your custom Truth
    subject. The important thing to note is that you import both
    `Truth.assertThat` and `EmployeeSubject.assertThat`:

    ```java
    import static com.google.common.truth.Truth.assertThat;
    import static com.google.common.truth.extension.EmployeeSubject.assertThat;
    ```

    You can use your subject alongside the core Truth subjects:

    ```java
    // "normal" Truth
    assertThat(db.get(SUNDAR.id())).isNull();

    // uses the custom EmployeeSubject
    db.relocate(KURT.id(), MTV);
    assertThat(db.get(KURT.id())).hasLocation(MTV);
    ```

<!-- References -->

[`@AutoValue`]:           http://github.com/google/auto/tree/master/value
[`Truth8`]:                  http://github.com/google/truth/blob/master/extensions/java8/src/main/java/com/google/common/truth/Truth8.java
[`Re2jSubjects`]:         http://github.com/google/truth/blob/master/extensions/re2j/src/main/java/com/google/common/truth/extensions/re2j/Re2jSubjects.java
[`LiteProtoSubject`]:     http://github.com/google/truth/blob/master/extensions/liteproto/src/main/java/com/google/common/truth/extensions/proto/LiteProtoSubject.java
[`ProtoSubject`]:         http://github.com/google/truth/blob/master/extensions/proto/src/main/java/com/google/common/truth/extensions/proto/ProtoSubject.java
[`ProtoTruth`]:         http://github.com/google/truth/blob/master/extensions/proto/src/main/java/com/google/common/truth/extensions/proto/ProtoTruth.java
[Compile Testing]:        http://github.com/google/compile-testing
[employee example]:       http://github.com/google/truth/blob/master/core/src/test/java/com/google/common/truth/extension/
[`Employee.java`]:        http://github.com/google/truth/blob/master/core/src/test/java/com/google/common/truth/extension/Employee.java
[`EmployeeSubjectTest.java`]:    http://github.com/google/truth/blob/master/core/src/test/java/com/google/common/truth/extension/EmployeeSubjectTest.java
[`EmployeeSubject.java`]: http://github.com/google/truth/blob/master/core/src/test/java/com/google/common/truth/extension/EmployeeSubject.java
[`FakeHrDatabaseTest.java`]: http://github.com/google/truth/blob/master/core/src/test/java/com/google/common/truth/extension/FakeHrDatabaseTest.java
[`ComparableSubject`]:    https://github.com/google/truth/blob/master/core/src/main/java/com/google/common/truth/ComparableSubject.java
[`Subject`]:    https://github.com/google/truth/blob/master/core/src/main/java/com/google/common/truth/Subject.java
[`Subject.Factory`]:    https://github.com/google/truth/blob/master/core/src/main/java/com/google/common/truth/Subject.java
[`FailureMetadata`]:    https://github.com/google/truth/blob/master/core/src/main/java/com/google/common/truth/FailureMetadata.java
[`FailureStrategy`]:    https://github.com/google/truth/blob/master/core/src/main/java/com/google/common/truth/FailureStrategy.java
[`expect`]:               https://google.github.io/truth/api/latest/com/google/common/truth/Expect.html
[`ExpectFailure`]:               https://google.github.io/truth/api/latest/com/google/common/truth/ExpectFailure.html
[shortcuts]: faq#full-chain
[`FailureStrategy.fail`]:    https://google.github.io/truth/api/latest/com/google/common/truth/FailureStrategy.html#fail-java.lang.AssertionError-

