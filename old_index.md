---
layout: default
subtitle: Introduction
url: /old_index
---

* auto-gen TOC:
{:toc}


## Overview

Truth is a testing framework designed to make your tests and their error
messages more readable and discoverable, while being extensible to new types of
objects.

Truth adopts a fluent style for test checks, is extensible in several ways,
supports IDE completion/discovery of available checks, and supports different
responses to failing checks. Truth can be used to declare JUnit-style
assumptions (which skip the test on failure), assertions (interrupt the test on
failure), and expectations (continue the test, but collect errors and report
failure at the end).

Truth is open-source software licensed under Apache 2.0 license. As such, you
are free to use it or modify it subject only to the terms in that license.

### An example

```java
import static org.junit.Assert.assertTrue;

Set<Foo> foo = ...;
assertTrue(foo.isEmpty()); // or, shudder, foo.size() == 0
```

reports an unhelpful stack trace of:

```none
java.lang.AssertionError
    at org.junit.Assert.fail(Assert.java:92)
    at org.junit.Assert.assertTrue(Assert.java:43)
    ...
```

whereas:

```java
import static com.google.common.truth.Truth.assertThat;

Set<Foo> foo = ...;
assertThat(foo).isEmpty()
```

reports:

```none
org.truth0.FailureStrategy$ThrowableAssertionError: Not true that <[a]> is empty
    at org.truth0.FailureStrategy.fail(FailureStrategy.java:33)
    ...
```

Truth's checks are intended to read (more or less) like English, and thereby be
more obvious in their intent, as well as report meaningful errors about the
information.

## Acknowledgements

Thanks to Github and Travis-CI for having a strong commitment to open-source,
and providing us with tools so we can provide others with code. And thanks to
Google for [Guava][1], for the [compile-testing][2] extension to Truth, for
generous open-source contributions and for encouraging developers to try to
solve problems in better ways and share that with the world.

Also thanks to the authors of JUnit, TestNG, Hamcrest, FEST, and others for
creating testing tools that let us write high-quality code, for inspiring this
work and for moving the ball forward in the field of automated software testing.
This project works with, works alongside, and sometimes works in competition
with the above tools, but owes a debt that everyone owes to those gone before.
They paved the way, and we hope this contribution is helpful to the field.

[1]: http://code.google.com/p/guava-libraries
[2]: http://github.com/google/compile-testing
[maven central]: https://repo1.maven.org/maven2/com/google/truth/truth/
