---
subtitle: Comparison
layout: default
url: /comparison
---


## Overview

Truth is a fluent assertion framework, which has a lot of similarities to
[AssertJ], which was forked from [FEST]. Truth is significantly different from
[Hamcrest], which uses [Polish prefix notation] to compose assertions.


Some typical examples that can highlight the similarities and differences of the
common assertions can be found below.

## Equality

Framework | Code example
--------- | -----------------------------------------
Truth     | `assertThat(actual).isEqualTo(expected);`
AssertJ   | `assertThat(actual).isEqualTo(expected);`
Hamcrest  | `assertThat(actual, equalTo(expected));`
JUnit     | `assertEquals(expected, actual);`

## Custom error messages

Framework | Code example
--------- | --------------------------------------------------
Truth     | `assertWithMessage("custom msg").that(actual).isEqualTo(expected);`
AssertJ   | `assertThat(actual).overridingErrorMessage("custom msg").isEqualTo(expected);`
Hamcrest  | `assertThat("custom msg", actual, equalTo(expected));`
JUnit     | `assertEquals("custom msg", expected, actual);`

## Custom labeling

Framework | Code example
--------- | ------------------------------------------------------
Truth     | `assertThat(actual).named("foo").isEqualTo(expected);`
AssertJ   | `assertThat(actual).as("foo").isEqualTo(expected);`
Hamcrest  | n/a
JUnit     | n/a

## Null checking

Framework | Code example
--------- | ----------------------------------
Truth     | `assertThat(actual).isNull();`
AssertJ   | `assertThat(actual).isNull();`
Hamcrest  | `assertThat(actual, nullValue());`
JUnit     | `assertNull(actual);`

## Boolean checks

Framework | Code example
--------- | -------------------------------
Truth     | `assertThat(actual).isTrue();`
AssertJ   | `assertThat(actual).isTrue();`
Hamcrest  | `assertThat(actual, is(true));`
JUnit     | `assertTrue(actual);`

## Double comparisons {#floating-point}

Framework | Code example
--------- | ----------------------------------------------------------------------------
Truth     | `assertThat(actualDouble).isWithin(tolerance).of(expectedDouble);`
AssertJ   | `assertThat(actualDouble).isCloseTo(expectedDouble, Offset.offset(offset));`
Hamcrest  | `assertThat(actualDouble, closeTo(expectedDouble, error));`
JUnit     | `assertEquals(expectedDouble, actualDouble, delta);`

## Float comparisons

Framework | Code example
--------- | ----------------------------------------------------------------------------
Truth     | `assertThat(actualFloat).isWithin(tolerance).of(expectedFloat);`
AssertJ   | `assertThat(actualFloat).isCloseTo(expectedFloat, Offset.offset(offset));`
Hamcrest  | `assertThat(actualFloat, closeTo(expectedFloat, error));`
JUnit     | `assertEquals(expectedFloat, actualFloat, delta);`

## Assume/Assumption (JUnit's skipping behavior)

Framework | Code example
--------- | --------------------------------------------
Truth     | `assume().that(actual).isEqualTo(expected);`
AssertJ   | n/a
Hamcrest  | `assumeThat(actual, equalTo(expected));`
JUnit     | `assumeEquals(expected, actual);`

## Expect (fail-at-end)

Framework | Code example
--------- | ---------------------------------------------------------------
Truth     | `expect.that(actual).isEqualTo(expected); // supplied by @Rule`
AssertJ   | n/a
Hamcrest  | n/a
JUnit     | n/a

<!-- References -->

[AssertJ]: http://joel-costigliola.github.io/assertj/
[FEST]: https://github.com/alexruiz/fest-assert-2.x
[Hamcrest]: https://code.google.com/p/hamcrest/
[Polish prefix notation]: http://en.wikipedia.org/wiki/Polish_notation

