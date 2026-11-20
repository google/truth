---
layout: default
title: Benefits
---

## More readable test assertions {#readable-assertions}

Truth's fluent API allows users to write more readable test assertions. For
example:

<div class="c-codeselector__button c-codeselector__button_java">Java</div>
<div class="c-codeselector__button c-codeselector__button_kotlin">Kotlin</div>

```java
Optional<String> middleName = user.getMiddleName();
assertThat(middleName).isAbsent();
```
{: .c-codeselector__code .c-codeselector__code_java }

```kotlin
val middleName: Optional<String> = user.middleName
assertThat(middleName).isAbsent()
```
{: .c-codeselector__code .c-codeselector__code_kotlin }

With JUnit, you'd have to negate the assertion:

<div class="c-codeselector__button c-codeselector__button_java">Java</div>
<div class="c-codeselector__button c-codeselector__button_kotlin">Kotlin</div>

```java
Optional<String> middleName = user.getMiddleName();
assertFalse(middleName.isPresent());
```
{: .c-codeselector__code .c-codeselector__code_java }

```kotlin
val middleName: Optional<String> = user.middleName
assertFalse(middleName.isPresent)
```
{: .c-codeselector__code .c-codeselector__code_kotlin }

## More readable failure messages {#readable-messages}

Truth provides readable messages by default when your assertion fails. For
example:

<div class="c-codeselector__button c-codeselector__button_java">Java</div>
<div class="c-codeselector__button c-codeselector__button_kotlin">Kotlin</div>

```java
assertThat(googleColors).contains(PINK);
```
{: .c-codeselector__code .c-codeselector__code_java }

```kotlin
assertThat(googleColors).contains(PINK)
```
{: .c-codeselector__code .c-codeselector__code_kotlin }

If this assertion fails, you'll get the following message:

```
expected to contain: PINK
but was            : [BLUE, RED, YELLOW, BLUE, GREEN, RED]
```

With JUnit, most people would write:

<div class="c-codeselector__button c-codeselector__button_java">Java</div>
<div class="c-codeselector__button c-codeselector__button_kotlin">Kotlin</div>

```java
assertTrue(googleColors.contains(PINK));
```
{: .c-codeselector__code .c-codeselector__code_java }

```kotlin
assertTrue(googleColors.contains(PINK))
```
{: .c-codeselector__code .c-codeselector__code_kotlin }

However, if that assertion fails, JUnit will throw an `AssertionFailedError`
without *any* failure message! If you want a useful failure message with JUnit,
you're forced to duplicate the data under test in a custom failure message:

<div class="c-codeselector__button c-codeselector__button_java">Java</div>
<div class="c-codeselector__button c-codeselector__button_kotlin">Kotlin</div>

```java
assertTrue("expected to contain PINK but was " + googleColors, googleColors.contains(PINK));
```
{: .c-codeselector__code .c-codeselector__code_java }

```kotlin
assertTrue("expected to contain PINK but was $googleColors", googleColors.contains(PINK))
```
{: .c-codeselector__code .c-codeselector__code_kotlin }

## Readable complex assertions {#complex-assertions}

Truth allows you to express complex assertions in a readable way. For example,
with Truth you could write:

<div class="c-codeselector__button c-codeselector__button_java">Java</div>
<div class="c-codeselector__button c-codeselector__button_kotlin">Kotlin</div>

```java
assertThat(googleColors).containsNoneOf(PINK, BLACK, WHITE, ORANGE);
```
{: .c-codeselector__code .c-codeselector__code_java }

```kotlin
assertThat(googleColors).containsNoneOf(PINK, BLACK, WHITE, ORANGE)
```
{: .c-codeselector__code .c-codeselector__code_kotlin }

With JUnit, you'd have to write several assertions:

<div class="c-codeselector__button c-codeselector__button_java">Java</div>
<div class="c-codeselector__button c-codeselector__button_kotlin">Kotlin</div>

```java
assertFalse(googleColors.contains(PINK));
assertFalse(googleColors.contains(BLACK));
assertFalse(googleColors.contains(WHITE));
assertFalse(googleColors.contains(ORANGE));
```
{: .c-codeselector__code .c-codeselector__code_java }

```kotlin
assertFalse(googleColors.contains(PINK))
assertFalse(googleColors.contains(BLACK))
assertFalse(googleColors.contains(WHITE))
assertFalse(googleColors.contains(ORANGE))
```
{: .c-codeselector__code .c-codeselector__code_kotlin }
