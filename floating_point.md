---
layout: default
title: Truth for floating point types
---

1. auto-gen TOC:
{:toc}

## Why floating point types are different

The `double` and `float` types have a few unusual characteristics which have
implications for writing tests involving them.

*   Floating point types are inherently approximations of the concepts they are
    used to model (i.e. real numbers). For example, the decimal number 0.1 has
    no exact `double` or `float` representation: when you write the `double`
    value `0.1` you actually get
    0.1000000000000000055511151231257827021181583404541015625.
*   Floating point arithmetic is therefore also approximate. For example, with
    `double` arithmetic `0.1 * 0.1` is equal to `0.010000000000000002` and not
    `0.01` as you might expect.
*   Normal mathematical rules don't apply exactly to floating point arithmetic.
    For example, you can't rely on `(a+b)+c` being exactly equal to `a+(b+c)`.
*   Even for a given expression like `(a+b)+c`, the value is not exactly
    specified (unless [strictfp][strictfp definition] is in force). For example,
    even though `double` values are 64-bit, on an x86 architecture the JVM may
    decide to do the whole computation at 80-bit precision on the FPU, keeping
    the intermediate result `a+b` in an 80-bit register… or it may not. And that
    decision can change the result.

[strictfp definition]: https://docs.oracle.com/javase/specs/jls/se8/html/jls-15.html#jls-15.4

It follows that there is rarely one exact correct result for any method doing
floating point arithmetic, and so if your tests assert on the exact result then
they will be fragile: they might break if someone changes the code to do
something that is mathematically equivalent; they might break when run on a
different system; and they might even break when run on the same system under
different load conditions.

## What you should test

### Should I assert exact or approximate equality?

Suitable uses for exact equality include cases where the contract of the code
under test specifies...

*   ...that it will copy values from the input to the output without doing any
    arithmetic on them.
*   ...that it will return fixed values specified as exact `double` or `float`
    values, such as class constants or literals.

Suitable uses for approximate equality include cases where the contract of the
code under test specifies...

*   ...that it will do any kind of arithmetic.
*   ...that it will return fixed values specified as mathematical values, such
    as 1/10 or π.

### For approximate equality, what tolerance should I use?

You should aim to accept values within a range which is large enough that you
don't risk false failures where floating point errors exceed the tolerance, but
small enough that you don't risk false passes where a bug in the code produces
an error smaller than the tolerance.

*   For `double` this typically gives you a lot of leeway. As a very rough rule
    of thumb, you can often use a tolerance of 1 part in ~10^10 (i.e. 10 decimal
    orders of magnitude smaller than the numbers involved in the
    test).[^why-double-tolerance]
*   For `float`, you have to be more careful. As a very rough rule of thumb, you
    can often use a tolerance of 1 part in ~10^5 (i.e. 5 decimal orders of
    magnitude smaller than the numbers involved in the test), but you should
    give it some thought.[^why-float-tolerance]

[^why-double-tolerance]: The error in representing an arbitrary real number as a
    `double` is at most 1 part in ~10^16; in most cases,
    accumulated error from operations like addition and
    multiplication will be a small multiple of this
    (although beware pathological cases such as subtracting
    two very similar large numbers to get a very small
    number, which magnifies the relative errors
    significantly: you should try to avoid these in your
    code wherever possible anyway). By using a tolerance of
    1 part in 10^10 you only risk a false pass if a bug
    introduces an error into the 10th significant figure of
    the result, and only risk a false failure if the
    relative numerical errors are magnified by a factor of
    ~10^6.

[^why-float-tolerance]: For `float` the error is at most 1 part in ~10^7. By
    using a tolerance of 1 part in 10^5 you risk a false
    pass if a bug introduces an error into the 5th
    significant figure of the result, and risk a false
    failure if the relative numerical errors are magnified
    by a factor of ~100.

## How to write floating point assertions in Truth

This sections gives examples of some common use-cases. See the javadoc on the
subjects for full documentation. These examples all use `double`/`Double` but
there are equivalents for `float`/`Float`.

### Exact assertions about `double` values

```java
Cuboid cuboid = Cuboid.ofDimensions(1.2, 3.4, 5.6);
assertThat(cuboid.getWidth()).isEqualTo(3.4);
```

Note: All the exact assertions define equality like `Double.equals` does: each
of the values `POSITIVE_INFINITY`, `NEGATIVE_INFINITY`, and `NaN` is equal to
itself, and `-0.0` is *not* equal to `0.0`. This is appropriate for the case
where the code under test is meant to pass values through without touching them.

### Approximate assertions about `double` values

```java
Cuboid cuboid = Cuboid.ofDimensions(1.2, 3.4, 5.6);
assertThat(cuboid.getVolume()).isWithin(1.0e-10).of(1.2 * 3.4 * 5.6);
```

Note: All the approximate assertions consider `-0.0` to be within any tolerance
of `0.0` and do *not* consider each of the values `POSITIVE_INFINITY`,
`NEGATIVE_INFINITY`, and `NaN` to be within any tolerance of
itself.[^infinity-philosophy] You should treat these as special cases and use
the dedicated methods (where applicable) or exact equality for such values.

[^infinity-philosophy]: Philosophical aside: Infinity has [complicated
    mathematical
    properties](https://en.wikipedia.org/wiki/Infinity#Mathematics)
    and cannot be treated as a regular number in arithmetic.
    For example, `1.0 / 0.0` and `2.0 / 0.0` are both
    `POSITIVE_INFINITY`: the question of whether they are
    "approximately equal" is debatable, at best. In Truth,
    we take the approach that the safest thing is to
    consider them not to be. The same applies even more
    clearly to `NaN`.

### Special-valued assertions about `double` values

```java
assertThat(reciprocal(0.0)).isPositiveInfinity();
assertThat(ratio(0.0, 0.0)).isNaN();
assertThat(randomFiniteDouble()).isFinite();
```

### Exact assertions about `double[]` values

```java
double[] original = {1.1, 2.2, 3.3};
double[] shuffled = shuffler.shuffledCopy(original);
// Assert that shuffled contains the same elements as original in any order:
assertThat(shuffled).usingExactEquality().containsExactly(1.1, 2.2, 3.3);
// Assert that calling shuffledCopy did not modify original:
assertThat(original)
    .usingExactEquality()
    .containsExactly(1.1, 2.2, 3.3)
    .inOrder();
```

### Approximate assertions about `double[]` values

```java
double[] original = {1.1, 2.2, 3.3};
double[] squares = squareArrayValues(original);
assertThat(squares)
    .usingTolerance(1.0e-10)
    .containsExactly(1.21, 4.84, 10.89)
    .inOrder();
```

### Assertions about `Iterable<Double>` values

Exact equality is the default, you can just proceed like any other `Iterable`.
For approximate equality:

```java
List<Double> original = ImmutableList.of(1.1, 2.2, 3.3);
List<Double> squares = squareListValues(original);
assertThat(squares)
    .comparingElementsUsing(tolerance(1.0e-10))
    .containsExactly(1.21, 4.84, 10.89)
    .inOrder();
```

where the `tolerance` method is imported as:

```java
import static com.google.common.truth.Correspondence.tolerance;
```

### Assertions about `Map<?, Double>` or `Multimap<?, Double>` values

Exact equality is the default, you can just proceed like any other `Map` or
`Multimap`. For approximate equality:

```java
Map<String, Double> scoresById = scorer.getScoresOutOfTenById();
assertThat(scores)
    .comparingValuesUsing(tolerance(1.0e-10))
    .containsExactly(
        "good-thing", 9.6,
        "bad-thing", 1.3333333333);
```

where the `tolerance` method is imported as:

```java
import static com.google.common.truth.Correspondence.tolerance;
```

Note that there is no facility for doing approximate equality of map keys: since
lookups will always be done using exact equality (by the definition of `Map`)
this doesn't really make sense, and floating point keys are generally not
recommended.

### Assertions about protocol buffers with `double` properties

[`ProtoTruth`] supports approximate comparison of floats and doubles through
methods `usingFloatTolerance(float)` and `usingDoubleTolerance(double)`
respectively. This applies to all floats/doubles within the protocol buffers
being compared, so you don't have to specify which fields you want to test if
you don't want to.

`Iterables`, `Maps`, and `Multimaps` also have equivalent tolerance methods for
protocol buffer values within the containers (not keys!).

```java
import static com.google.common.truth.extensions.proto.ProtoTruth.assertThat;

...

assertThat(actualProto)
    .usingDoubleTolerance(1.0e-10)
    .isEqualTo(MyProto.newBuilder().setScore(1.023).build());
assertThat(mapOfProtos)
    .usingDoubleTolerance(1.0e-10)
    .containsExactly(
        "low",
        MyProto.newBuilder().setScore(0.001).build(),
        "high",
        MyProto.newBuilder().setScore(156.17).build());
```

### Assertions about other data structures with `double` properties

The best approach is normally to make assertions about the fields directly. For
example, suppose that `Report` is a value type and you want to use approximate
equality for its `score` property and regular equality for all it's other
properties. Assuming a proto-like API you could write this:

```java
assertThat(actualReport.getScore())
    .isWithin(1.0e-10)
    .of(expectedReport.getScore());
assertThat(actualReport.toBuilder().clearScore().build())
    .isEqualTo(expectedReport.toBuilder().clearScore().build());
```

If you do this quite a bit, you might want to write a helper method. If you do
it a lot, you might want to write a [custom subject](extension.md).

### Assertions about `Iterable`, `Map`, and `Multimap` values containing other data structures with `double` properties

Write your own `Correspondence` implementation and use [Fuzzy Truth](fuzzy.md).

```java
assertThat(actualReports)
    .comparingElementsUsing(REPORT_CORRESPONDENCE)
    .containsExactlyElementsIn(expectedReports);
```

<!-- References -->

[`ProtoTruth`]: https://truth.dev/protobufs

