---
layout: default
subtitle: We've made failure a strategy
url: /truth
---

* auto-gen TOC:
{:toc}

# Introduction

Truth is a testing framework designed to make your tests and their error messages 
more readable and discoverable, while being extensible to new types of objects.  

Truth adopts a fluent style for 
test propositions, is extensible in several ways, supports IDE completion/discovery 
of available propositions, and supports different responses to un-true propositions. 
Truth can be used to declare JUnit-style assumptions (which skip the test on 
failure), assertions (interrupt the test on failure), and expectations (continue 
the test, but collect errors and report failure at the end).

Truth is open-source software licensed under Apache 2.0 license.  As such, 
you are free to use it or modify it subject only to the terms in that license.

## An example

{% highlight java %}
import static org.junit.Assert.assertTrue;

Set<Foo> foo = ...;
assertTrue(foo.isEmpty()); // or, shudder, foo.size() == 0
{% endhighlight %}

reports an unhelpful stack trace of: 

{% highlight text %}
java.lang.AssertionError
    at org.junit.Assert.fail(Assert.java:92)
    at org.junit.Assert.assertTrue(Assert.java:43)
    ...
{% endhighlight %}

whereas:

{% highlight java %}
import static com.google.common.truth.Truth.assertThat;

Set<Foo> foo = ...;
assertThat(foo).isEmpty()
{% endhighlight %}

reports:

{% highlight text %}
org.truth0.FailureStrategy$ThrowableAssertionError: Not true that <[a]> is empty
    at org.truth0.FailureStrategy.fail(FailureStrategy.java:33)
    ...
{% endhighlight %}

Truth's propositions are intended to read (more or less) like English, and thereby be more
obvious in their intent, as well as report meaningful errors about the information.  

## Alternate ways to call in to Truth

There are two ways to access Truth.  Truth orients around a verb (assert, assume, expect) 
which implies a different behavior for failure.  Because assert is the most common, and it
is a java keyword, a single method short-hand similar to hamcrest and FEST is there for
normal assertion usage:

{% highlight java %}
assertThat(someInt).isEqualTo(5);     // Convenience method to access assert_().that()
{% endhighlight %}

However, for non-assert cases, or where functionality requires access to the "TestVerb",
a form that returns the raw verb object is available:

{% highlight java %}
assert_().that(someInt).isEqualTo(5); // Assert is a keyword in java, so no assert().that()
assert_().about(...).that(...)...     // Extensibility (see later in this page)
assume().that(someValue).isNotNull(); // JUnit-style Assume behavior.

{% endhighlight %}

Truth's documentation will use `assertThat(...)` for all examples of assertion, unless 
otherwise required by functionality, though `assert_().that(...)` would also work.

Any operation that can be done with `assert_()` can be done with `assume()` or any other
way to get the TestVerb.

## Common and handy propositions

### Before you start

`assertThat` is on the `Truth` class, so be sure to `import static com.google.common.truth.Truth.assertThat;`

### Basics

{% highlight java %}
assertThat(someInt).isEqualTo(5);
assertThat(aString).isEqualTo("Blah Foo");
assertThat(aString).contains("lah");
assertThat(foo).isNotNull();
{% endhighlight %}

### Collections and Maps

{% highlight java %}
assertThat(someCollection).contains("a");                       // contains this item
assertThat(someCollection).containsAllOf("a", "b").inOrder();   // contains items in the given order
assertThat(someCollection).containsExactly("a", "b", "c", "d"); // contains all and only these items
assertThat(someCollection).containsNoneOf("q", "r", "s");       // contains none of these items
assertThat(aMap).containsKey("foo");                            // has a key
assertThat(aMap).containsEntry("foo", "bar");                   // has a key, with given value
assertThat(aMap).doesNotContainEntry("foo", "Bar");             // does not have the given entry
{% endhighlight %}

### Custom Error Messages

{% highlight java %}
// Reports: The subject is unexpectedly false
assertThat(myBooleanResult).isTrue();

// Reports: "hasError()" is unexpectedly false
assertThat(myBooleanResult).named("hasError()").isTrue();

// Reports: My custom Message
assertWithMessage("My arbitrary custom message")
    .that(myBooleanResult).named("hasError()").isTrue();
{% endhighlight %}

## New types, new propositions

Truth is also designed to be exensible to new types, and developers can create
custom "Subjects" for these types, whose usage might look like this:

{% highlight java %}
// customType() returns an adapter (SubjectFactory).
assertAbout(customType()).that(theObject).hasSomeComplexProperty(); // specialized assertion
assertAbout(customType()).that(theObject).isEqualTo(anotherObject); // overridden equality
{% endhighlight %}

# Setup

To prepare to use Truth, declare this dependency in maven or an equivalent:

{% highlight xml %}
<dependency>
  <groupId>com.google.truth</groupId>
  <artifactId>truth</artifactId>
  <version>{{site.version}}</version>
</dependency>
{% endhighlight %}

or download the jar directly from the link below and add it to
your tests classpath

{% highlight text %}
http://search.maven.org/remotecontent?filepath=com/google/truth/truth/{{site.version}}/truth-{{site.version}}.jar
{% endhighlight %}

# More Detailed Usage Information

For more information, check out the section [about how truth works](/truth/usage#how-does-truth-work), 
and more [detailed inventory of built-in propositions](/truth/usage#built-in-propositions).

# Acknowledgements

Thanks to Github and Travis-CI for having a strong commitment to open-source, and 
providing us with tools so we can provide others with code.  And thanks to Google 
for [Guava][1], for the [compile-testing][2] extension to Truth, for generous 
open-source contributions and for encouraging developers to try to solve problems
in better ways and share that with the world.

Also thanks to the authors of JUnit, TestNG, Hamcrest, FEST, and others for creating
testing tools that let us write high-quality code, for inspiring this work and for 
moving the ball forward in the field of automated software testing.  This project
works with, works alongside, and sometimes works in competition with the above
tools, but owes a debt that everyone owes to those gone before.  They paved the 
way, and we hope this contribution is helpful to the field.

[1]: http://code.google.com/p/guava-libraries
[2]: http://github.com/google/compile-testing
