---
layout: default
title: Fuzzy Truth
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
instance of type `E`. Optionally, it can also [describe the
difference](#formatDiff) between two instances that do not correspond. A
`Correspondence<A, E>` is used in an assertion about a collection of elements of
type `A` (typically the collection actually returned by the code under test),
checking that it contains (or, occasionally, does not contain) certain expected
elements of type `E`.

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
        return "parses to";
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

N.B. The `Correspondence` applies to the values, not the keys, of the map. You
should almost always make assertions using equality of map keys, since equality
semantics are used for lookups.

```java
Map<String, String> actual = ImmutableMap.of("abc", "123", "def", "456");
assertThat(actual)
    .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
    .containsExactly("def", 456, "abc", 123);
```

## Multimap Example {#multimap}

N.B. The `Correspondence` applies to the values, not the keys, of the multimap.
You should almost always make assertions using equality of multimap keys, since
equality semantics are used for lookups.

```java
Multimap<String, String> actual =
    ImmutableListMultimap.of("abc", "123", "def", "456", "def", "789");
assertThat(actual)
    .comparingValuesUsing(STRING_PARSES_TO_INTEGER_CORRESPONDENCE)
    .containsEntry("def", 789);
```

## Getting better failure messages

When an assertion involving collections of objects with verbose `toString()`
representations (such as [value types]) fails, the failure messages can often be
hard to understand. There are a couple of things you can do to make debugging
failing tests easier.

### Enabling pairing of `Iterable` elements {#displayingDiffsPairedBy}

If you are making an assertion about an `Iterable`, and you know of key some
function which uniquely indexes the expected elements, then you can use the
`displayingDiffsPairedBy` method to tell Fuzzy Truth about it. For example, if
you have a type called `Record`, and you're making an assertion about an
`Iterable<Record>` using a `Correspondence` called `RECORD_EQUIVALENCE`, and the
expected records have unique IDs returned by a `getId()` method, then you could
write this:

```java
assertThat(actualRecords)
    .comparingElementsUsing(RECORD_EQUIVALENCE)
    .displayingDiffsPairedBy(Record::getId)
    .containsExactlyElementsIn(expectedRecords);
```

If this assertion fails, the failure message will pair up any missing and
unexpected elements by their IDs. For example, it might tell you that the actual
`Iterable` was missing an element with ID 2, that it had an unexpected element
with ID 3, or that the element with ID 4 wasn't equivalent to the one it
expected.

(If an assertion about a `Map` fails, the failure message will automatically
miss up any missing and unexpected entries using their keys. You can think of
the `displayingDiffsPairedBy` method as providing an equivalent for an assertion
about an `Iterable`. Note that this won't affect whether the test passes or
fails.)

### Enabling formatted diffs between elements {#formatDiff}

Your `Correspondence` subclass may optionally implement the `formatDiff` method,
which takes an actual and an expected element and returns a `String` describing
how they differ. For example, a `Correspondence` that describes whether two
instances of a value type are equivalent might implement `formatDiff` to
describe which properties of the value types are different.

When you do this, Fuzzy Truth will include these formatted diffs in failure
messages whenever it can usefully do so. For example, when an assertion about a
`Map` fails, and there is an entry which has the right key but the wrong value,
it will show a diff between the value it got and the one it expected. For this
to work for an assertion about an `Iterable` (with more than one missing value)
you need to [enable pairing, as shown above](#displayingDiffsPairedBy).


[protocol buffers]: https://developers.google.com/protocol-buffers/
[correspondence-tostring]: http://google.github.io/truth/api/latest/com/google/common/truth/Correspondence.html#toString()
[value types]: https://github.com/google/auto/blob/master/value/userguide/index.md

