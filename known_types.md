---
layout: default
title: "Truth: Supported Types"
---


Truth has built in support for the following types:

*   Primitives

    *   [`Boolean`]
    *   [`Double`]
    *   [`Float`]
    *   [`Integer`]
    *   [`Long`]

*   Arrays

    *   [`Object[]`][ObjectArray]
    *   [`boolean[]`][BooleanArray]
    *   [`byte[]`][ByteArray]
    *   [`char[]`][CharacterArray]
    *   [`double[]`][DoubleArray]
    *   [`float[]`][FloatArray]
    *   [`int[]`][IntegerArray]
    *   [`long[]`][LongArray]
    *   [`short[]`][ShortArray]

*   [Java 8 types]

    *   [`Optional`] - and `OptionalInt`, `OptionalLong`, and `OptionalDouble`
    *   [`Stream`] - and `IntStream`, `LongStream` (and maybe someday
        `DoubleStream`)

*   Other JDK types

    *   [`Object`] - since all types extend `Object`, you can make simple
        assertions such as `.isEqualTo()` on any type
    *   [`String`]
    *   [`Comparable`] - this can be used for any `Comparable` type (`Instant`,
        `BigInteger`, etc.)
    *   [`Iterable`] - this can be used for any `Iterable` type (`List`, `Set`,
        etc.)
    *   [`Map`]
    *   [`Throwable`]
    *   [`Class`]
    *   [`BigDecimal`]

*   Guava types

    *   [`Optional`](https://truth.dev/api/latest/com/google/common/truth/GuavaOptionalSubject)
    *   [`Multimap`]
    *   [`Multiset`]
    *   [`Table`]

Truth is [extensible](extension.md), so if you don't see a type you need to make
assertions on in this list, you can
[write your own](extension.md#writing-your-own-custom-subject).

<!-- References -->

<!-- TODO(kak): Update the 2 Java 8 links once they have public javadocs -->

[BooleanArray]: https://truth.dev/api/latest/com/google/common/truth/PrimitiveBooleanArraySubject
[ByteArray]: https://truth.dev/api/latest/com/google/common/truth/PrimitiveByteArraySubject
[CharacterArray]: https://truth.dev/api/latest/com/google/common/truth/PrimitiveCharArraySubject
[DoubleArray]: https://truth.dev/api/latest/com/google/common/truth/PrimitiveDoubleArraySubject
[FloatArray]: https://truth.dev/api/latest/com/google/common/truth/PrimitiveFloatArraySubject
[IntegerArray]: https://truth.dev/api/latest/com/google/common/truth/PrimitiveIntArraySubject
[LongArray]: https://truth.dev/api/latest/com/google/common/truth/PrimitiveLongArraySubject
[ObjectArray]: https://truth.dev/api/latest/com/google/common/truth/ObjectArraySubject
[ShortArray]: https://truth.dev/api/latest/com/google/common/truth/PrimitiveShortArraySubject
[`BigDecimal`]: https://truth.dev/api/latest/com/google/common/truth/BigDecimalSubject
[`Boolean`]: https://truth.dev/api/latest/com/google/common/truth/BooleanSubject
[`Class`]: https://truth.dev/api/latest/com/google/common/truth/ClassSubject
[`Comparable`]: https://truth.dev/api/latest/com/google/common/truth/ComparableSubject
[`Double`]: https://truth.dev/api/latest/com/google/common/truth/DoubleSubject
[`Float`]: https://truth.dev/api/latest/com/google/common/truth/FloatSubject
[`Integer`]: https://truth.dev/api/latest/com/google/common/truth/IntegerSubject
[`Iterable`]: https://truth.dev/api/latest/com/google/common/truth/IterableSubject
[`Long`]: https://truth.dev/api/latest/com/google/common/truth/LongSubject
[`Map`]: https://truth.dev/api/latest/com/google/common/truth/MapSubject
[`Multimap`]: https://truth.dev/api/latest/com/google/common/truth/MultimapSubject
[`Multiset`]: https://truth.dev/api/latest/com/google/common/truth/MultisetSubject
[`Object`]: https://truth.dev/api/latest/com/google/common/truth/Subject
[`Optional`]: https://truth.dev/api/latest/com/google/common/truth/OptionalSubject.html
[`SortedMap`]: https://truth.dev/api/latest/com/google/common/truth/SortedMapSubject
[`SortedSet`]: https://truth.dev/api/latest/com/google/common/truth/SortedSetSubject
[`Stream`]: https://truth.dev/api/latest/com/google/common/truth/StreamSubject.html
[`String`]: https://truth.dev/api/latest/com/google/common/truth/StringSubject
[`Table`]: https://truth.dev/api/latest/com/google/common/truth/TableSubject
[`Throwable`]: https://truth.dev/api/latest/com/google/common/truth/ThrowableSubject
[Java 8 types]: https://truth.dev/faq#java8

