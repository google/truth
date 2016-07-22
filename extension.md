---
subtitle: Extension
layout: default
url: /extension
---


## Known extensions


Projects which are not part of core to limit Truth's dependency graph or for
other grouping reasons, but are considered part of the overall truth effort are
found in the /extensions folder, including:

*   [Java8] for java8 types such as `java.util.Optional`
*   [`Re2jSubjects`] for `com.google.re2j.Pattern`
*   [`ProtoLiteSubject`] for `MessageLite` style protocol buffers (unreleased)

Other extensions that are not part of the Truth project itself include:

*   [Compile Testing] for testing annotation processors and compilation jobs


## Writing your own Truth extension

For an example of how to support custom types in Truth, please see the [employee
example]. The rest of this doc will walk through each of the files, step by
step.

There are basically three parts to the example:

1.  [`Employee.java`]

    This is the *class under test*. There's really nothing exciting about this
    file. In this case, it's just a plain old Java object (*POJO*), implemented
    using [`@AutoValue`].

2.  [`EmployeeSubject.java`]

    This is the custom Truth subject, and the most interesting part of this
    example. There are several
    important things to note about this file:

    1.  Every custom subject needs to extend from `Subject`. Your class
        definition will generally look like this:

        ```java
        public final class EmployeeSubject extends Subject<EmployeeSubject, Employee> {…}
        ```

    2.  Subjects also need to define a `SubjectFactory`. The `SubjectFactory`
        instructs Truth how to create instances of your custom `Subject`. For
        most users, this will be fairly boilerplate (unless you're passing in
        additional state to your subject):

        ```java
        private static final SubjectFactory<EmployeeSubject, Employee> EMPLOYEE_SUBJECT_FACTORY =
            new SubjectFactory<EmployeeSubject, Employee>() {
              @Override
              public EmployeeSubject getSubject(FailureStrategy failureStrategy, @Nullable Employee target) {
                return new EmployeeSubject(failureStrategy, target);
              }
            };
        ```

    3.  For consistency with Truth's entry point, we define a static
        `assertThat(Employee)` shortcut method (entry point). This allows users
        to statically import both `Truth.assertThat()` and
        `EmployeeSubject.assertThat()` at the same time:

        ```java
        public static EmployeeSubject assertThat(@Nullable Employee employee) {
          return assertAbout(EMPLOYEE_SUBJECT_FACTORY).that(employee);
        }
        ```

        Static imports with the same name follow the same rules as normal Java
        overloads. That means that you can statically import as many
        `assertThat` methods as you want, as long as it doesn't create an
        ambiguous reference.

    4.  If you do end up with an ambiguous reference, the solution is to expose
        a static method that returns the `SubjectFactory` constant:

        ```java
        public static SubjectFactory<EmployeeSubject, Employee> employees() {
          return EMPLOYEE_SUBJECT_FACTORY;
        }
        ```

        This allows users to statically import that method and use the longhand
        technique for accessing the custom `Subject`:

        ```java
        import static com.google.common.truth.extension.EmployeeSubject.employees;
        …
        assertAbout(employees()).that(employee)
        ```

        Note that it's generally preferable to expose a static method (e.g.,
        `EmployeeSubject.employees()`) that returns a reference to the
        `SubjectFactory` constant, rather exposing the constant directly (e.g.,
        `EmployeeSubject.EMPLOYEE`).

        We generally recommend naming this method in the *plural form* (e.g.,
        `EmployeeSubject.employees()`, `PersonSubject.people()`, etc.) for more
        readable test assertions.

    5.  You also need to define a (private) constructor that accepts a
        `FailureStrategy` and a reference to the *instance under test*. These
        should both be passed to the parent class:

        ```java
        private EmployeeSubject(FailureStrategy failureStrategy, @Nullable Employee subject) {
          super(failureStrategy, subject);
        }
        ```

    6.  The final piece of a custom subject is to define your test assertion
        API. Since you're defining the API, you can write it however you'd like.
        However, we generally recommend you try to use method names that will
        make the test assertions read *as close to natural language as
        possible*. For some advice on naming assertion methods, please see
        [here](faq#assertion-naming). For example:

        ```java
        public void hasName(String name) {
          if (!getSubject().name().equals(name)) {
            fail("has name", name);
          }
        }
        ```

        Inside the method, you simply do the check you're testing, and `fail()`
        if it is not true.

3.  [`EmployeeTest.java`]

    This is an example of how you unit tests will look using your custom Truth
    subject (described in the previous section). The important thing to note, is
    that both `Truth.assertThat` and `EmployeeSubject.assertThat` are statically
    imported:

    ```java
    import static com.google.common.truth.Truth.assertThat;
    import static com.google.common.truth.extension.EmployeeSubject.assertThat;
    ```

    The `EmployeeSubject` can be used right along side "normal" Truth:

    ```java
    // "normal" Truth
    assertThat("kurt alfred kluever").contains("alfred");

    // uses the custom EmployeeSubject
    assertThat(KURT).hasUsername("kak");
    assertThat(KURT).hasName("Kurt Alfred Kluever");
    ```

<!-- References -->

[`@AutoValue`]:           http://github.com/google/auto/tree/master/value
[Java8]:                  http://github.com/google/truth/blob/master/extensions/java8/src/main/java/com/google/common/truth/Truth8.java
[`Re2jSubjects`]:         http://github.com/google/truth/blob/master/extensions/re2j/src/main/java/com/google/common/truth/extensions/re2j/Re2jSubjects.java
[`ProtoLiteSubject`]:     http://github.com/google/truth/blob/master/extensions/protolite/src/main/java/com/google/common/truth/extensions/proto/ProtoLiteSubject.java
[Compile Testing]:        http://github.com/google/compile-testing
[employee example]:       http://github.com/google/truth/blob/master/core/src/test/java/com/google/common/truth/extension/
[`Employee.java`]:        http://github.com/google/truth/blob/master/core/src/test/java/com/google/common/truth/extension/Employee.java
[`EmployeeTest.java`]:    http://github.com/google/truth/blob/master/core/src/test/java/com/google/common/truth/extension/EmployeeTest.java
[`EmployeeSubject.java`]: http://github.com/google/truth/blob/master/core/src/test/java/com/google/common/truth/extension/EmployeeSubject.java

