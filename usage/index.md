---
subtitle: Basic Usage
layout: default
url: /usage/
---

* auto-gen TOC:
{:toc}

# Super basics

Truth is used in a literate, or fluent programming style.

Most simply, replacing a JUnit assert like this:

    assertTrue(blah.isSomeBooleanValue());

can be done with the following:

    ASSERT.that(blah.isSomeBooleanValue()).isTrue();

**But wait!!!**

This may not seem like a saving, but for two things.  One, it reads, in 
english, precisely what it means.  And secondly, many assertTrue() calls 
are hiding more meaningful assertions.

    assertTrue(blah > 5);

In Truth, you would tend to extract all of these:

    ASSERT.that(blah).isMoreThan(5);

And where one might write:

    assertTrue(blah.isEmpty());

Truth would have you write:

    ASSERT.that(blah).isEmpty();
    
# The Guts


## How does Truth work?

Truth presents you with a test verb (ASSERT and ASSUME are built in, and
EXPECT is supported with JUnit4 @Rules).  The verb is asserting on a subject,
the value or object under test.  

    ASSERT.that(thisSubject)...

Once Truth has a subject, it has a known type, and can therefore reason
at compile time about what propositions are known about that subject. For
instance, integers, longs, booleans, strings, and various flavours of 
collections all have different kinds of things you want to know about them.
Because Truth knows about these types at compile time, it returns a "Subject"
wrapper around your value, which declares proposition methods such as "contains"
or "isMoreThan" or "isEmpty" etc.  These allow your IDE to suggest available
completions. 

## Failure Strategy

Truth treats failure as data - a proposition was not true... so now what?
Well, that depends on the failure strategy.  Truth supports a few different
strategies for handling failure, and has different strategies baked in to
pre-built TestVerbs exposed as static final fields.  The standard strategies 
are:

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
    <td>Truth.ASSERT</td>
    <td>Aborts and fails test, reports failure</td>
    <td>JUnit, TestNG, others (untested)</td>
    <td />
  </tr>
  <tr>
    <td>Assumption</td>
    <td>Truth.ASSUME</td>
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

*Note:* These different styles can let a developer build more supple tests,
though the Truth team recommends mostly using ASSERT in unit tests,
and very careful consideration of ASSUME and EXPECT.  These can make 
one's tests quite expressive and clear, but ASSUME can cause tests 
to not be run (unexpectedly), and EXPECT can encourage the developer
to test propositions about way too many things, causing big heavy 
tests, rather than lots of small, clear tests.   

## Extensibility

Truth is extensible in a couple of ways - new assertions, and alternative behaviors on failure.  

### Supporting new types or alternative proposition for known types.

One can add additional (or alternative) sets of propositions via subtypes of Subject and SubjectFactory and the about() method. An example of this can be found in [this test of about() delegation](https://github.com/google/truth/blob/master/core/src/test/java/com/google/common/truth/delegation/DelegationTest.java).

One could add a new Subject for one's custom type, or treat a type as if it were another, say, to treat strings as if they were URIs, etc.  SubjectFactory provides a means by which one can link a type to a given Subject which contains propositions about that type.

### Interpretations of proposition failure

Additional or alternative strategies for failure can be created by implementing a FailureStrategy and providing a hook for it.  Examples of different failure strategies are Assertion, (junit-style) Assumption, or c-style expectation (fail-at-end error gathering).  Another example of alternative uses include providing a custom exception type to permit more easy expected exception trapping where AssertionError is expected from the code under test, which is done by the [compile-testing](http://github.com/google/compile-testing) library. 

## Categorically testing the contents of collections

Sometimes a test needs to look at the contents of a collection 
and ensure that characteristics of all a collection's contents
conform to certain constraints.  This can be done with a 
for-each-like approach.

    ASSERT.in(anIterable).thatEach(STRING).startsWith("foo");

This lets you pass in an iterable type, and provide a SubjectFactory
(it is advised to use the static final fields for this for readability).
When this is invoked, then <code>startsWith("foo")</code> will be invoked
on each element in the iterable in turn, reporting failure as if a 
separate ASSERT.that(anElement).startsWith("foo") had been invoked for
each element.  Naturally, ASSERT will fail on the first failing element,
ASSUME will skip the test on any failing element, and EXPECT will 
gather all failures and report them at the end of the test.

This approach can be used for custom types too, so long as they have
a SubjectFactory

    public static final SubjectFactory<MyCustomSubject, MyType> MY_TYPE = ...;
    ... 
    ASSERT.in(someIterable).thatEach(MY_TYPE).doesSomething();

The same extensibility provided in <code>ASSERT.about(MY_TYPE).that()...</code>
is available to the developer over iterables of that type.

## Re-labeling a Subject

There are times when a subject under test has a toString() representation that results in 
awkward error messages.  For instance: 

	boolean calculationResult = ...; // Assume the result of the calculation is false.
    ASSERT.that(calculationResult).isTrue();

This would result in an error message of:

    <false> was expected be true, but was false

Truth lets you re-label a subject, so the error message is more meaningful.
Taking the example above:

	boolean calculationResult = ...; // Assume the result of the calculation is false.
    ASSERT.that(calculationResult).named("result").isTrue();

This would result in an error message of:

    "result" was expected to be true, but was false

A developer can then far more effectively describe the subject of the test to permit 
readable error messages with proper context.

## Replacing the failure message

Sometimes the raw error message is inappropriate in context, and simply relabelling the subject
is insufficient.  In those cases, the entire failure message can be replaced with:

	Set<Stuff> calculationResult = ...; // Assume the result of the calculation is false.
    ASSERT.withFailureMessage("Calculations should always have at least one entry, but had none.")
        .that(calculationResult).isNotEmpty();

# Built-in Propositions

## Basic objects

Equality is simply "is" in Truth.  

    ASSERT.that(this).isEqualTo(that);

Type information is as usual:

    ASSERT.that(this).isInstanceOf(MyObject.class);

Often propositions have negative forms:

    ASSERT.that(this).isNotInstanceOf(String.class);

Nullness is checked simply with:

    ASSERT.that(something).isNull();
    ASSERT.that(somethingElse).isNotNull();

Fields' presence and their values can be checked with:

    ASSERT.that(something).hasField("foo");
    ASSERT.that(something).hasField("bar").withValue("blah");
    
This should work even with private fields, and can be useful in testing 
generated properties created by some frameworks like Lombok and Tapestry.

## Basic operations on any object

### Labeling the subject

     Foo foo = null;
     ASSERT.that(something).named("foo").isNotNull();
     
results in a more descriptive message:

     Not true that null reference "foo" is not null.

### Applying propositions to objects in a collection

     Set<String> strings = asList("Aaron", "Abigail", "Christian", "Jason");
     ASSERT.in(strings).thatEach(STRING).contains("a"); // this will fail

## Class objects

    ASSERT.that(aClass).declaresField("foo");
    
 + <em>Note, do not use <strong>hasField()</strong> on Class objects, as you will be 
   testing whether Class.class itself has that field, not whether the 
   type it represents declares that field.  A deprecation warning should
   notify you of this usage, but be careful, and use <strong>declaresField("foo")</strong>
   instead.<em>

## Booleans

    ASSERT.that(something).isTrue();
    ASSERT.that(something).isFalse();

## Numerics and Comparables

Numerics like Integer can be compared for equality like other types

    ASSERT.that(foo).isEqualTo(5);
    ASSERT.that(bar).isNotEqualTo(5);

Comparables (including numerics) can be compared

    int foo = 5;
    ASSERT.that(foo).isGreaterThan(4);
    ASSERT.that(foo).isAtLeast(4);
    ASSERT.that(foo).isAtLeast(5);
    ASSERT.that(5).isLessThan(6);
    ASSERT.that(5).isAtMost(6);
    ASSERT.that(5).isAtMost(5);

This works with any comparable, including strings

    ASSERT.that("foo").isGreaterThan("aoo");

More complicated comparisons can be achieved using Range   

    ASSERT.that(foo).isIn(Range.open(4, 6));
    ASSERT.that("foo").isIn(Range.open("eoo", "goo"));

## Strings

    ASSERT.that(aString).contains("blah");
    ASSERT.that(aString).startsWith("foo");
    ASSERT.that(aString).endsWith("bar");

## Iterables, Collections, Sets, and the like.

### Iterables and Collections

One can simply use object equality if you want to test collection 
equivalence, given the guarantees of Collections' implementations of 
.equals():

    ASSERT.that(colectionA).isEqualTo(collectionB);

Testing properties like size should be done like so:

    ASSERT.that(collection).hasSize(5);
    ASSERT.that(anIterable).isEmpty();

Or you can test that a specific item is present present:

    ASSERT.that(collectionA).contains(q);

Or you can test that all provided items are present:

    ASSERT.that(collectionA).containsAllOf(a, b, c);

Or you can be *even* more explicit and test that all ***and only*** the provided items are present
(but may have them in any order):

    ASSERT.that(collectionA).containsExactly(a, b, c, d);

optionally you can further constrain these to be ordered expectations:

    ASSERT.that(collectionA).containsAllOf(a, b, c).inOrder();
    ASSERT.that(collectionA).containsExactly(a, b, c, d).inOrder();

Or you can assert using a (very) limited "or" logic with:

    ASSERT.that(collectionA).containsAnyOf(b, c);

You can also pass in collections as containers of expected results, like so:

    ASSERT.that(collectionA).containsAllIn(collectionB);
    ASSERT.that(collectionA).containsAnyIn(collectionB);
    ASSERT.that(collectionA).containsExactlyElementsIn(collectionB).inOrder();

### Lists

Specific properties can be proposed on lists, such as:

    ASSERT.that(myList).isOrdered(); // uses default ordering and is strict, no equal elements.
    ASSERT.that(myList).isPartiallyOrdered(); // like isOrdered, but equal elements may be present.

And custom comparators can be provided

    ASSERT.that(myList).isOrdered(aComparator); 
    ASSERT.that(myList).isPartiallyOrdered(aComparator);

### Maps

Presence of keys, keys for values, or values can be asserted

    ASSERT.that(map).containsKey("foo");
    ASSERT.that(map).containsEntry("foo", "bar");
    ASSERT.that(map).doesNotContainKey("foo");

Naturally, also:

    ASSERT.that(map).isEmpty();
    ASSERT.that(map).isNotEmpty();
    ASSERT.that(map).hasSize(5); 

