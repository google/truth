Truth
=====
***We've made failure a strategy***

**Continuous Integration:** [![Build Status](https://truth.ci.cloudbees.com/job/Truth0-truth-master/badge/icon)](https://truth.ci.cloudbees.com/job/Truth0-truth-master/)<br />
**Latest Release:** *0.8*<br />
**Latest Artifact:** *org.junit.contrib:truth:jar:0.8*<br />

**Note:** Truth is subject to change and prior to 1.0, may introduce 
breaking changes.  We're getting closer to "prime time" but please 
use with caution before release 1.0.  Consider Truth in alpha.

<!--- Generated TOC by http://doctoc.herokuapp.com/ -->

###***Table of Contents***

- [Introduction](#introduction)
- [Installation](#installation)
- [Using Truth](#using-truth)
	- [Super basics](#super-basics)
	- [Failure Strategy](#failure-strategy)
	- [How does Truth work?](#how-does-truth-work)
	- [Extensibility](#extensibility)
	- [Categorically testing the contents of collections](#categorically-testing-the-contents-of-collections)
	- [Some code examples](#some-code-examples)
		- [Basic objects](#basic-objects)
		- [Booleans](#booleans)
		- [Numerics](#numerics)
		- [Strings](#strings)
		- [Iterables, Collections, Sets, and the like.](#iterables-collections-sets-and-the-like)
- [Planned improvements and changes](#planned-improvements-and-changes)

Introduction
------------

Truth is a testing framework suitable for making assertions and assumptions
about code.  Truth adopts a fluent style for your test propositions, is 
extensible in several ways, supports IDE completion/discovery of available
propositions, and supports different responses to un-true propositions.
Truth can be used to declare assumptions (skip the test if they fail),
assertions (fail the test), and expectations (continue but report errors 
and fail at the end).

While intended to work with JUnit, Truth can be used with other testing
framework with minimal effort.  Truth is released as a maven artifact 
through a custom repository (it will be released to repo1.maven.org soon),
and is licensed with the Apache 2.0 open-source license.  As such, you are
free to use it or modify it subject only to the terms in that license.

Installation
------------

To prepare to use Truth, declare this dependency:

    <dependency>
      <groupId>truth</groupId>
      <artifactId>truth</artifactId>
      <version>0.8</version>
    </dependency>

and add this repository section to your pom or to a parent 
pom.xml file. (for now - this requirement will later be removed.

    <repositories>
      <repository>
        <id>truth-repo</id>
        <url>https://raw.github.com/truth0/repo/master</url>
      </repository>
    </repositories>

or download the jar directly from the link below and add it to
your tests classpath

    https://raw.github.com/truth0/repo/master/org/junit/contrib/truth/0.8/truth-0.8.jar

Using Truth
-----------
*A brief and basic tutorial*

###Super basics

Truth is used in a literate style.  One can use truth most simply 
to replace JUnit assertions (and to handle the dreaded "MoreAsserts")
problem where a team must increasingly create more static assert
libraries to handle complex cases.  

Most simply, replacing a JUnit assert like this:

    assertTrue(blah.isSomeBooleanValue());

can be done with the following:

    Truth.ASSERT.that(blah.isSomeBooleanValue()).isTrue();

... or with static imports, this can be shortened to the preferred usage:

    ASSERT.that(blah.isSomeBooleanValue()).isTrue();

This may not seem like a saving, but for two things.  One, it reads, in 
english, precisely what it means.  And secondly, many assertTrue() calls 
are hiding more meaningful assertions, like this:

    assertTrue(blah > 5);

In Truth, you would tend to extract all of these:

    ASSERT.that(blah).isMoreThan(5);

And where one might write:

    assertTrue(blah.isEmpty());

Truth would have you write:

    ASSERT.that(blah).isEmpty();

### Failure Strategy

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

### How does Truth work?

Truth presents you with a test verb (ASSERT and ASSUME are built in, and
EXPECT is supported with JUnit4 @Rules).  The verb is asserting on a subject,
the value to be considered.  

    ASSERT.that(thisSubject)

Once Truth has a subject, it has a known type, and can therefore reason
at compile time about what propositions are known about that subject. For
instance, integers, longs, booleans, strings, and various flavours of 
collections all have different kinds of things you want to know about them.
Because Truth knows about these types at compile time, it returns a "Subject"
wrapper around your value, which declares proposition methods such as "contains"
or "isMoreThan" or "isEmpty" etc.  These allow your IDE to suggest available
completions. 

### Extensibility

But what if a proposition isn't supported for the desired type?  Or the
type is not a part of Truth's basic types at all?  How can a developer
use Truth on "TheirCustomObject"?

Truth uses two means to do this.  The simplest is not recommended, but is
quick and dirty and can get the job done.  That's simply to extend AbstractVerb
or TestVerb and declare their own custom ASSERT field containing this custom
verb.  That verb can implement more "that(Sometype t)" overrides to support
custom types, or even to provide a different Subject wrapper for the already
supported types. (see [Extensibility through Subclassing][])

A more literate approach, and one which doesn't require creating a new
TestVerb (allowing reuse of ASSERT, ASSUME, and EXPECT) is to use this syntax:

    ASSERT.about(SOME_TYPE).that(thatValue).hasSomePropositionalValue();

SOME_TYPE here is actually a SubjectFactory - an interface which can be
implemented to provide a custom Subject wrapper.  Creating a SubjectFactory
for use in this approach is pretty easy, and you can follow the example
given it the [Extension through delegation][] example.

For convenience, you can create a static final SOME_TYPE field so you 
can use it less-verbosely in ASSERT.about();  Existing Subject subclasses
(e.g. IntegerSubject, StringSubject, etc.) all have static final
SubjectFactory fields named INTEGER, STRING, etc. You can also follow
their example. 

  [Extensibility through Subclassing]: https://github.com/cgruber/truth/tree/documentation/src/test/java/org/junit/contrib/truth/extensiontest
  [Extension through delegation]: https://github.com/cgruber/truth/tree/documentation/src/test/java/org/junit/contrib/truth/delegatetest

### Categorically testing the contents of collections
*coming in version 0.8*

Sometimes a test needs to look at the contents of a collection 
and ensure that characteristics of all a collection's contents
conform to certain constraints.  This can be done with a 
for-each-like approach.

    ASSERT.in(anIterable).thatEach(STRING).contains("foo");

This lets you pass in an iterable type, and provide a SubjectFactory
(it is advised to use the static final fields for this for readability).
When this is invoked, then <code>contains("foo")</code> will be invoked
on each element in the iterable in turn, reporting failure as if a 
separate ASSERT.that(anElement).contains("foo") had been invoked for
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

### Some code examples

These are not exhaustive examples, but commonly used propositions.

#### Basic objects

Equality is simply "is" in Truth.  

    ASSERT.that(this).is(that);

Type information is as usual:

    ASSERT.that(this).isA(MyObject.class);

Often propositions have negative forms:

    ASSERT.that(this).isNotA(String.class);

Nullness is checked simply with:

    ASSERT.that(something).isNull();
    ASSERT.that(somethingElse).isNotNull();

Fields' presence and their values can *(as of 0.8)* be checked with:

    ASSERT.that(something).hasField("foo").withValue("bar");
    
This should work even with private fields, and can be useful in testing 
generated properties created by some frameworks like Lombok and Tapestry.

#### Class objects
*coming in release 0.8*

    ASSERT.that(aClass).declaresField("foo");
    
 + <em>Note, do not use <strong>hasField()</strong> on Class objects, as you will be 
   testing whether Class.class itself has that field, not whether the 
   type it represents declares that field.  A deprecation warning should
   notify you of this usage, but be careful, and use <strong>declaresField("foo")</strong>
   instead.<em>

#### Booleans

    ASSERT.that(something).isTrue();
    ASSERT.that(something).isFalse();

#### Numerics

    ASSERT.that(5).isBetween(4, 5);
    ASSERT.that(5).isExclusivelyInRange(4, 6);

#### Strings

    ASSERT.that(aString).contains("blah");
    ASSERT.that(aString).startsWith("foo");
    ASSERT.that(aString).endsWith("bar");

#### Iterables, Collections, Sets, and the like.

##### Iterables

    ASSERT.that(anIterable).isEmpty();
    ASSERT.that(anIterable).iteratesOverSequence(a, b, c);

##### Collections

One can simply use object equality if you want to test collection 
equivalence, given the guarantees of Collections' implementations of 
.equals():

    ASSERT.that(colectionA).is(collectionB);

Or you can be more explicit:

    ASSERT.that(collectionA).contains(a, b, c);

optionally you can further constrain this:

    ASSERT.that(collectionA).contains(a, b, c).inOrder();

Or you can provide some limited "or" logic with:

    ASSERT.that(collectionA).containsAnyOf(b, c);

##### Lists

Specific collection properties can be proposed on lists, such as:

    ASSERT.that(myList).isOrdered(); // uses default ordering and is strict, no equal elements.
    ASSERT.that(myList).isPartiallyOrdered(); // like isOrdered, but equal elements may be present.

And custom comparators can be provided

    ASSERT.that(myList).isOrdered(aComparator); 
    ASSERT.that(myList).isPartiallyOrdered(aComparator);

Planned improvements and changes
--------------------------------

  * Subject wrappers for new types:
    * New subjects for Map<K,V>, Float/Double and other currently missing types.
	    * Support for Annotations and methods and field availability on classes.
    * Support for Protocol Buffers, JavaBeans (and other reflective types)
    * Support for Guava collections and types (Multimaps, Multisets, etc.)
  * New propositions on existing Subject wrappers:
    * StringSubject, IntegerSubject, etc.
