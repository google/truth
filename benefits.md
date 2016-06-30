---
layout: default
subtitle: Benefits
url: /benefits
---

## More readable test assertions {#readable-assertions}

Truth's fluent API allows users to write more readable test assertions. For
example:

```java
Optional<String> middleName = user.getMiddleName();
assertThat(middleName).isAbsent();
```

With JUnit, you'd have to negate the assertion:

```java
Optional<String> middleName = user.getMiddleName();
assertFalse(middleName.isPresent());
```

## More readable failure messages {#readable-messages}

Truth provides readable messages by default when your assertion fails. For
example:

```java
assertThat(googleColors).contains(PINK);
```

If this assertion fails, you'll get the following message: `<[BLUE, RED, YELLOW,
BLUE, GREEN, RED]> should have contained <PINK>`

With JUnit, most people would write:

```java
assertTrue(googleColors.contains(PINK));
```

However, if that assertion fails, JUnit will throw an `AssertionFailedError`
without *any* failure message! If you want a useful failure message with JUnit,
you're forced to duplicate the data under test in a custom failure message:

```java
assertTrue(googleColors + " should have contained PINK", googleColors.contains(PINK));
```

## Readable complex assertions {#complex-assertions}

Truth allows you to express complex assertions in a readable way. For example,
with Truth you could write:

```java
assertThat(googleColors).containsNoneOf(PINK, BLACK, WHITE, ORANGE);
```

With JUnit, you'd have to write several assertions:

```java
assertFalse(googleColors.contains(PINK));
assertFalse(googleColors.contains(BLACK));
assertFalse(googleColors.contains(WHITE));
assertFalse(googleColors.contains(ORANGE));
```
