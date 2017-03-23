---
subtitle: Fuzzy Truth
layout: default
url: /fuzzy
---

1. auto-gen TOC:
{:toc}

Fuzzy Truth extends Truth to allow you to make assertions about `Iterable`,
`Map`, and `Multimap` subjects, where the values are compared using something
other than object equality. The primary use cases are doubles and floats using
[approximate equality](floating_point), as well as [protocol buffers], but the
framework is quite general.

## Correspondence {#correspondence}

The primary concept of Fuzzy Truth is a `Correspondence`. A correspondence
determines whether an instance of type `A` corresponds in some way to an
instance of type `E`. The instances of type `A` are typically actual values from
a collection returned by the code under test; the instances of type `E` are
typically expected values with which the actual values are compared by the test.

Here's an example correspondence between strings and integers, which tests
whether the string parses as the integer.

Note that it's important to implement the `toString()` method for readable
failure messages. See the [javadoc][correspondence-tostring] for more
information.

```java
private static final Correspondence<String, Integer> STRING_PARSES_TO_INTEGER_CORRESPONDENCE =
    new Correspondence<String, Integer>() {
      @Override
      public boolean compare(@Nullable String actual, @Nullable Integer expected) {
        if (actual == null) {
          return expected == null;
        }
        try {
          return Integer.decode(actual).equals(expected);
        } catch (NumberFormatException e) {
          return false;
        }
      }
      @Override
      public String toString() {
        return "parse to";
      }
    };
```

## Iterable Example {#iterable}

```java
Iterable<String> actual = ImmutableList.of("+64", "+128", "+256", "0x80");
assertThat(actual)
    .comparingElementsUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
    .containsExactly(64, 128, 256, 128)
    .inOrder();
```

## Map Example {#map}

```java
Map<String, String> actual = ImmutableMap.of("abc", "123", "def", "456");
assertThat(actual)
    .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
    .containsExactly("def", 456, "abc", 123);
```

## Multimap Example {#multimap}

```java
Multimap<String, String> actual =
    ImmutableListMultimap.of("abc", "123", "def", "456", "def", "789");
assertThat(actual)
    .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
    .containsEntry("def", 789);
```


[protocol buffers]: https://developers.google.com/protocol-buffers/
[correspondence-tostring]: http://google.github.io/truth/api/latest/com/google/common/truth/Correspondence.html#toString()

