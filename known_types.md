---
layout: default
title: Known Types
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

    *   [`Optional`](http://google.github.io/truth/api/latest/com/google/common/truth/GuavaOptionalSubject)
    *   [`Multimap`]
    *   [`Multiset`]
    *   [`Table`]
    *   [`AtomicLongMap`]

Truth is [extensible](extension.md), so if you don't see a type you need to make
assertions on in this list, you can
[write your own](extension.md#writing-your-own-custom-subject).

<!-- References -->

<!-- TODO(kak): Update the 2 Java 8 links once they have public javadocs -->

[BooleanArray]: http://google.github.io/truth/api/latest/com/google/common/truth/PrimitiveBooleanArraySubject
[ByteArray]: http://google.github.io/truth/api/latest/com/google/common/truth/PrimitiveByteArraySubject
[CharacterArray]: http://google.github.io/truth/api/latest/com/google/common/truth/PrimitiveCharArraySubject
[DoubleArray]: http://google.github.io/truth/api/latest/com/google/common/truth/PrimitiveDoubleArraySubject
[FloatArray]: http://google.github.io/truth/api/latest/com/google/common/truth/PrimitiveFloatArraySubject
[IntegerArray]: http://google.github.io/truth/api/latest/com/google/common/truth/PrimitiveIntArraySubject
[LongArray]: http://google.github.io/truth/api/latest/com/google/common/truth/PrimitiveLongArraySubject
[ObjectArray]: http://google.github.io/truth/api/latest/com/google/common/truth/ObjectArraySubject
[ShortArray]: http://google.github.io/truth/api/latest/com/google/common/truth/PrimitiveShortArraySubject
[`AtomicLongMap`]: http://google.github.io/truth/api/latest/com/google/common/truth/AtomicLongMapSubject
[`BigDecimal`]: http://google.github.io/truth/api/latest/com/google/common/truth/BigDecimalSubject
[`Boolean`]: http://google.github.io/truth/api/latest/com/google/common/truth/BooleanSubject
[`Class`]: http://google.github.io/truth/api/latest/com/google/common/truth/ClassSubject
[`Comparable`]: http://google.github.io/truth/api/latest/com/google/common/truth/ComparableSubject
[`Double`]: http://google.github.io/truth/api/latest/com/google/common/truth/DoubleSubject
[`Float`]: http://google.github.io/truth/api/latest/com/google/common/truth/FloatSubject
[`Integer`]: http://google.github.io/truth/api/latest/com/google/common/truth/IntegerSubject
[`Iterable`]: http://google.github.io/truth/api/latest/com/google/common/truth/IterableSubject
[`Long`]: http://google.github.io/truth/api/latest/com/google/common/truth/LongSubject
[`Map`]: http://google.github.io/truth/api/latest/com/google/common/truth/MapSubject
[`Multimap`]: http://google.github.io/truth/api/latest/com/google/common/truth/MultimapSubject
[`Multiset`]: http://google.github.io/truth/api/latest/com/google/common/truth/MultisetSubject
[`Object`]: http://google.github.io/truth/api/latest/com/google/common/truth/Subject
[`Optional`]: https://truth.dev/api/latest/com/google/common/truth/OptionalSubject.html
[`SortedMap`]: http://google.github.io/truth/api/latest/com/google/common/truth/SortedMapSubject
[`SortedSet`]: http://google.github.io/truth/api/latest/com/google/common/truth/SortedSetSubject
[`Stream`]: https://truth.dev/api/latest/com/google/common/truth/StreamSubject.html
[`String`]: http://google.github.io/truth/api/latest/com/google/common/truth/StringSubject
[`Table`]: http://google.github.io/truth/api/latest/com/google/common/truth/TableSubject
[`Throwable`]: http://google.github.io/truth/api/latest/com/google/common/truth/ThrowableSubject
[Java 8 types]: https://google.github.io/truth/faq#java8

