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

Truth adopts a fluent style (inspired by FEST) for 
test propositions, is extensible in several ways, supports IDE completion/discovery 
of available propositions, and supports different responses to un-true propositions. 
Truth can be used to declare JUnit-style assumptions (which skip the test on 
failure), assertions (interrupt the test on failure), and expectations (continue 
the test, but collect errors and report failure at the end).

Truth is open-source software licensed under Apache 2.0 license.  As such, 
you are free to use it or modify it subject only to the terms in that license.

## An example

{% highlight java %}
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
Set<Foo> foo = ...;
ASSERT.that(foo).isEmpty()
{% endhighlight %}

reports:

{% highlight text %}
org.truth0.FailureStrategy$ThrowableAssertionError: Not true that <[a]> is empty
    at org.truth0.FailureStrategy.fail(FailureStrategy.java:33)
    ...
{% endhighlight %}

Truth's propositions are intended to read (more or less) like English, and thereby be more
obvious in their intent, as well as report meaningful errors about the information.  

## Common and handy propositions

### Basics

{% highlight java %}
ASSERT.that(someInt).isEqualTo(5);
ASSERT.that(aString).isEqualTo("Blah Foo");
ASSERT.that(aString).contains("lah");
ASSERT.that(foo).isNotNull();
{% endhighlight %}

### Collections and Maps

{% highlight java %}
ASSERT.that(someCollection).has().allOf("a", "b", "c").inOrder(); // contents in order
ASSERT.that(someCollection).has().exactly("a", "b", "c", "d") // all and only these items
ASSERT.that(someCollection).has().noneOf("q", "r", "s") // none of these items
ASSERT.that(aMap).hasField("foo").withValue("bar"); // given field, with the given value. 
ASSERT.that(anIterable).iteratesAs("a", "b", "c"); // Uses the iterator.
{% endhighlight %}

### Custom Error Messages

{% highlight java %}
// Reports: The subject is unexpectedly false
ASSERT.that(myBooleanResult).isTrue;

// Reports: "hasError()" is unexpectedly false
ASSERT.that(myBooleanResult).named("hasError()").isTrue;

// Reports: My custom Message
ASSERT.withFailureMessage("My custom message").that(myBooleanResult).named("hasError()").isTrue;
{% endhighlight %}

## New types, new propositions

Truth is also designed to be exensible to new types, and developers can create custom "Subjects"
for these types, whose usage might look like this:

{% highlight java %}
// customType() return an adapter, or SubjectFactory
ASSERT.about(customType()).that(theObject).hasSomeComplexProperty(); // specialized assertion
ASSERT.about(customType()).that(theObject).isEqualTo(anotherObject); // overridden equality
{% endhighlight %}

# Setup

To prepare to use Truth, declare this dependency in maven or an equivalent:

{% highlight xml %}
<dependency>
  <groupId>org.truth0</groupId>
  <artifactId>truth</artifactId>
  <version>{{site.version}}</version>
</dependency>
{% endhighlight %}

or download the jar directly from the link below and add it to
your tests classpath

{% highlight text %}
http://search.maven.org/remotecontent?filepath=org/truth0/truth/{{site.version}}/truth-{{site.version}}.jar
{% endhighlight %}

# More Detailed Usage Information

For more information, check out the section [about how truth works](/usage#how-does-truth-work), 
and more [detailed inventory of built-in propositions](/usage#built-in-propositions).

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
