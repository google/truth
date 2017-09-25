---
subtitle: Basic Usage
layout: default
url: /usage
---

* auto-gen TOC:
{:toc}


> ***NOTE:*** *This documentation is possibly out of date*

## Super basics

Truth is used in a literate, or fluent programming style.

Most simply, replacing a JUnit assert like this:

    assertTrue(blah.isSomeBooleanValue());

can be done with the following:

    assert_().that(blah.isSomeBooleanValue()).isTrue();

**But wait!!!**

This may not seem like a saving, but for two things. One, it reads, in english,
precisely what it means. And secondly, many assertTrue() calls are hiding more
meaningful assertions.

    assertTrue(blah > 5);

In Truth, you would tend to extract all of these:

    assert_().that(blah).isMoreThan(5);

And where one might write:

    assertTrue(blah.isEmpty());

Truth would have you write:

    assert_().that(blah).isEmpty();

## The Guts

### How does Truth work?

Truth lets you choose a "failure strategy" (assert_() and assume() are built in,
and Expect is supported with JUnit4 @Rules). From there, you can perform checks
on the value under test.

    assert_().that(thisSubject)...

Once Truth has a subject, it has a known type, and can therefore reason at
compile time about what checks are supported for that subject. For instance,
integers, longs, booleans, strings, and various flavours of collections all have
different kinds of things you want to know about them. Because Truth knows about
these types at compile time, it returns a "Subject" wrapper around your value,
which declares methods such as "contains" or "isGreaterThan" or "isEmpty" etc.
These allow your IDE to suggest available completions.

### Failure Strategy

Truth treats failure as data - a check failed... so now what? Well, that depends
on the failure strategy. Truth supports a few different strategies for handling
failure. The standard strategies are:

<table>
  <tr>
    <th>Strategy</th>
    <th>Constant</th>
    <th>Behaviour</th>
    <th>Framework supported</th>
    <th>Notes</th>
  </tr>
  <tr>
    <td>Assertion</td>
    <td>Truth.assert_()</td>
    <td>Aborts and fails test, reports failure</td>
    <td>JUnit, TestNG, others (untested)</td>
    <td />
  </tr>
  <tr>
    <td>Assumption</td>
    <td>TruthJUnit.assume()</td>
    <td>Aborts and ignores/skips test</td>
    <td>JUnit</td>
    <td />
  </tr>
  <tr>
    <td>Expectation</td>
    <td>Expect.create()</td>
    <td>Continues test, reports errors and failure upon test completion</td>
    <td>JUnit</td>
    <td>You must declare an @Rule per the ExpectTest</td>
  </tr>
</table>

*Note:* These different styles can let a developer build more flexible tests,
though the Truth team recommends mostly using assertThat() in unit tests, and
very careful consideration of assume() and Expect. These can make one's tests
quite expressive and clear, but assume() can cause tests to not be run
(unexpectedly), and Expect can encourage the developer to check way too many
things, causing big heavy tests, rather than lots of small, clear tests.

### Extensibility

Truth is extensible in a couple of ways - new assertions, and alternative
behaviors on failure.

#### Supporting new types or alternative check for known types.

One can add additional (or alternative) sets of checks via subtypes of Subject
and Subject.Factory and the about() method. An example of this can be found in
[this test of about()
delegation](https://github.com/google/truth/blob/master/core/src/test/java/com/google/common/truth/delegation/DelegationTest.java).

One could add a new Subject for one's custom type, or treat a type as if it were
another, say, to treat strings as if they were URIs, etc. Subject.Factory
provides a means by which one can link a type to a given Subject which contains
checks about that type.

#### Interpretations of check failure

Additional or alternative strategies for failure can be created by implementing
a FailureStrategy and providing a hook for it. Examples of different failure
strategies are Assertion, (junit-style) Assumption, or c-style expectation
(fail-at-end error gathering). Another example of alternative uses include
providing a custom exception type to permit more easy expected exception
trapping where AssertionError is expected from the code under test, which is
done by the [compile-testing](http://github.com/google/compile-testing) library.

### Re-labeling a Subject

There are times when a subject under test has a toString() representation that
results in awkward error messages. For instance:

    boolean calculationResult = ...; // Assume the result of the calculation is false.
    assert_().that(calculationResult).isTrue();

This would result in an error message of:

    <false> was expected be true, but was false

Truth lets you re-label a subject, so the error message is more meaningful.
Taking the example above:

    boolean calculationResult = ...; // Assume the result of the calculation is false.
    assert_().that(calculationResult).named("result").isTrue();

This would result in an error message of:

    "result" was expected to be true, but was false

A developer can then far more effectively describe the subject of the test to
permit readable error messages with proper context.

### Replacing the failure message

Sometimes the raw error message is inappropriate in context, and simply
relabelling the subject is insufficient. In those cases, the entire failure
message can be replaced with:

    Set<Stuff> calculationResult = ...; // Assume the result of the calculation is false.
    assert_().withFailureMessage("Calculations should always have at least one entry, but had none.")
        .that(calculationResult).isNotEmpty();

## Basic operations on any object

### Labeling the subject

     Foo foo = null;
     assert_().that(something).named("foo").isNotNull();

results in a more descriptive message:

     Not true that null reference "foo" is not null.

## More Information

For additional information read more in the list of [known types](known_types).
