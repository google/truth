---
layout: default
title: Fuzzy Truth
---

1. auto-gen TOC:
{:toc}

Fuzzy Truth extends Truth to allow you to make assertions about `Iterable`,
`Map`, and `Multimap` subjects, where the values are compared using something
other than object equality. This mechanism is integral to the APIs for comparing
collections of doubles and floats using [approximate equality](floating_point)
and of [protocol buffers](protobufs), but the framework is quite general.

You can also use it to
[improve failure messages while still using object equality](#formatDiffWithEquality).

## Correspondence {#correspondence}

The primary concept of Fuzzy Truth is a
[`Correspondence`][correspondence-class]. A correspondence determines whether an
instance of type `A` corresponds in some way to an instance of type `E`.
Optionally, it can also [describe the difference](#formatDiff) between two
instances that do not correspond. A `Correspondence<A, E>` is used in an
assertion about a collection of elements of type `A` (typically the collection
actually returned by the code under test), checking that it contains (or,
occasionally, does not contain) certain expected elements of type `E`.

You use a `Correspondence` by passing it into a method such as
[`comparingElementsUsing`][iterablesubject-comparingelementsusing],
as shown in the [examples below](#iterable).

Here's an example correspondence between strings, which tests whether the actual
strings contain the expected substrings:

```java
private static final Correspondence<String, String> CONTAINS_SUBSTRING =
    Correspondence.from(String::contains, "contains");
```

Here's an example correspondence between strings and integers, which tests
whether the string parses as the integer:

```java
class ThisTest {
  private static final Correspondence<String, Integer> STRING_PARSES_TO_INTEGER =
      Correspondence.from(ThisTest::stringParsesToInteger, "parses to");

  private static boolean stringParsesToInteger(
      @Nullable String actual, @Nullable Integer expected) {
    if (actual == null) {
      return expected == null;
    }
    try {
      return Integer.decode(actual).equals(expected);
    } catch (NumberFormatException e) {
      return false;
    }
  }

  // ...tests using STRING_PARSES_TO_INTEGER...
}
```

The most general factory method for `Correspondence` instances is
[`Correspondence.from`][correspondence-from]. Other factory methods are
available for convenience in specific cases, such as
[`Correspondence.transforming`][correspondence-transforming] for testing the
elements for equality after some transformation. Here's an example
correspondence between instances of some `Record` class and integers, which
tests whether calling `getId` on the record returns the integer:

```java
private static final Correspondence<MyRecord, Integer> RECORD_HAS_ID =
    Correspondence.transforming(Record::getId, "has an ID of");
```

You may want to think about handling of null elements. In the examples above,
`STRING_PARSES_TO_INTEGER` has explicit null handling such that a null actual
string corresponds to a null expected integer; `CONTAINS_SUBSTRING` does not,
and any test which sees a null string will fail.

## Iterable Example {#iterable}

```java
Iterable<String> actual = ImmutableList.of("+64", "+128", "+256", "0x80");
assertThat(actual)
    .comparingElementsUsing(STRING_PARSES_TO_INTEGER)
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
    .comparingValuesUsing(STRING_PARSES_TO_INTEGER)
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
    .comparingValuesUsing(STRING_PARSES_TO_INTEGER)
    .containsEntry("def", 789);
```

## Getting better failure messages

When an assertion involving collections of objects with verbose `toString()`
representations (such as [value types]) fails, the failure messages can often be
hard to understand. There are a couple of things you can do to make debugging
failing tests easier.

### Enabling pairing of `Iterable` elements {#displayingDiffsPairedBy}

If you are making an assertion about an `Iterable`, and you know of some key
function which uniquely indexes the expected elements, then you can use the
[`displayingDiffsPairedBy`][iterable-displaying-diffs] method to tell Fuzzy
Truth about it. For example, if you have a type called `Record`, and you're
making an assertion about an `Iterable<Record>` using a `Correspondence` called
`RECORD_EQUIVALENCE`, and the expected records have unique IDs returned by a
`getId()` method, then you could write this:

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
match up any missing and unexpected entries using their keys. You can think of
the `displayingDiffsPairedBy` method as providing an equivalent for an assertion
about an `Iterable`. Note that this won't affect whether the test passes or
fails.)

### Enabling formatted diffs between elements {#formatDiff}

Your `Correspondence` instance may optionally provide functionality which takes
an actual and an expected element and returns a `String` describing how they
differ. For example, a `Correspondence` that describes whether two instances of
a value type are equivalent might provide diff-formatting to describe which
properties of the value types are different. You can supply this functionality
using the [`formattingDiffsUsing`][correspondence-formatting-diffs].

Here's an example correspondence between `Record` instances:

```java
class RecordTestHelper {

  static final Correspondence<Record, Record> RECORD_EQUIVALENCE =
      Correspondence.from(MyRecordTestHelper::recordsEquivalent, "is equivalent to")
          .formattingDiffsUsing(MyRecordTestHelper::formatRecordDiff);

  static boolean recordsEquivalent(@Nullable MyRecord actual, @Nullable MyRecord expected) {
    // code to check whether records should be considered equivalent for testing purposes
  }
  static String formatRecordDiff(@Nullable MyRecord actual, @Nullable MyRecord expected) {
    // code to format the diff between the records
  }
}
```

When you do this, Fuzzy Truth will include these formatted diffs in failure
messages whenever it can usefully do so. For example, when an assertion about a
`Map` fails, and there is an entry which has the right key but the wrong value,
it will show a diff between the value it got and the one it expected. For this
to work for an assertion about an `Iterable` (with more than one missing value)
you need to [enable pairing, as shown above](#displayingDiffsPairedBy).

### Enabling formatted diffs while still using object equality for comparisons {#formatDiffWithEquality}

You can get the same diff-formatting functionality while using object equality
for comparisons (i.e. making the same assertion as you would get with non-Fuzzy
Truth) by using a method such as
[`formattingDiffsUsing`][iterablesubject-formattingdiffsusing]. For example:

```java
assertThat(actualRecords)
    .formattingDiffsUsing(MyRecordTestHelper::formatRecordDiff)
    .displayingDiffsPairedBy(Record::getId)
    .containsExactlyElementsIn(expectedRecords);
```

## Protocol buffers

If you want to assert about a proto, an `Iterable` of protos, or a `Map` or
`Multimap` with proto values, you should normally use [Proto Truth](protobufs).
It is built on top of Fuzzy Truth and allows you to express most assertions more
concisely.

[correspondence-class]: https://truth.dev/api/latest/com/google/common/truth/Correspondence.html
[correspondence-from]: https://truth.dev/api/latest/com/google/common/truth/Correspondence.html#from-com.google.common.truth.Correspondence.BinaryPredicate-java.lang.String-
[correspondence-transforming]: https://truth.dev/api/latest/com/google/common/truth/Correspondence.html#transforming-com.google.common.base.Function-java-lang-String-
[correspondence-formatting-diffs]: https://truth.dev/api/latest/com/google/common/truth/Correspondence.html#formattingDiffsUsing-com.google.common.truth.Correspondence.DiffFormatter-
[iterablesubject-comparingelementsusing]: https://truth.dev/api/latest/com/google/common/truth/IterableSubject.html#comparingElementsUsing(com.google.common.truth.Correspondence)
[iterable-displaying-diffs]: https://truth.dev/api/latest/com/google/common/truth/IterableSubject.UsingCorrespondence.html#displayingDiffsPairedBy-com.google.common.base.Function-
[iterablesubject-formattingdiffsusing]: https://truth.dev/api/latest/com/google/common/truth/IterableSubject.html#formattingDiffsUsing(com.google.common.truth.Correspondence.DiffFormatter)
[value types]: https://github.com/google/auto/blob/master/value/userguide/index.md
