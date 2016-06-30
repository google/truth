---
subtitle: Known Types
layout: default
url: /known_types
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

*   Other JDK types

    *   [`Object`]
    *   [`String`]
    *   [`Comparable`] - this can be used for any Comparable type (Integers,
        Ranges, etc.)
    *   [`Iterable`] - this can be used for any Iterable type (Lists, Sets,
        Collections, etc.)
    *   [`Map`]
    *   [`Throwable`]
    *   [`Class`]
    *   [`BigDecimal`]

*   Guava types

    *   [`Optional`]
    *   [`Multimap`]
        *   [`ListMultimap`]
        *   [`SetMultimap`]
    *   [`Multiset`]
    *   [`Table`]

<!-- References -->

[`Boolean`]: http://google.github.io/truth/api/latest/com/google/common/truth/BooleanSubject
[`Double`]: http://google.github.io/truth/api/latest/com/google/common/truth/DoubleSubject
[`Float`]: http://google.github.io/truth/api/latest/com/google/common/truth/FloatSubject
[`Integer`]: http://google.github.io/truth/api/latest/com/google/common/truth/IntegerSubject
[`Long`]: http://google.github.io/truth/api/latest/com/google/common/truth/LongSubject
[ObjectArray]: http://google.github.io/truth/api/latest/com/google/common/truth/ObjectArraySubject
[BooleanArray]: http://google.github.io/truth/api/latest/com/google/common/truth/PrimitiveBooleanArraySubject
[ByteArray]: http://google.github.io/truth/api/latest/com/google/common/truth/PrimitiveByteArraySubject
[CharacterArray]: http://google.github.io/truth/api/latest/com/google/common/truth/PrimitiveCharArraySubject
[DoubleArray]: http://google.github.io/truth/api/latest/com/google/common/truth/PrimitiveDoubleArraySubject
[FloatArray]: http://google.github.io/truth/api/latest/com/google/common/truth/PrimitiveFloatArraySubject
[IntegerArray]: http://google.github.io/truth/api/latest/com/google/common/truth/PrimitiveIntArraySubject
[LongArray]: http://google.github.io/truth/api/latest/com/google/common/truth/PrimitiveLongArraySubject
[ShortArray]: http://google.github.io/truth/api/latest/com/google/common/truth/PrimitiveShortArraySubject
[`Object`]: http://google.github.io/truth/api/latest/com/google/common/truth/Subject
[`String`]: http://google.github.io/truth/api/latest/com/google/common/truth/StringSubject
[`Comparable`]: http://google.github.io/truth/api/latest/com/google/common/truth/ComparableSubject
[`Iterable`]: http://google.github.io/truth/api/latest/com/google/common/truth/IterableSubject
[`Map`]: http://google.github.io/truth/api/latest/com/google/common/truth/MapSubject
[`Throwable`]: http://google.github.io/truth/api/latest/com/google/common/truth/ThrowableSubject
[`Class`]: http://google.github.io/truth/api/latest/com/google/common/truth/ClassSubject
[`BigDecimal`]: http://google.github.io/truth/api/latest/com/google/common/truth/BigDecimalSubject
[`Optional`]: http://google.github.io/truth/api/latest/com/google/common/truth/GuavaOptionalSubject
[`Multimap`]: http://google.github.io/truth/api/latest/com/google/common/truth/MultimapSubject
[`ListMultimap`]: http://google.github.io/truth/api/latest/com/google/common/truth/ListMultimapSubject
[`SetMultimap`]: http://google.github.io/truth/api/latest/com/google/common/truth/SetMultimapSubject
[`Multiset`]: http://google.github.io/truth/api/latest/com/google/common/truth/MultisetSubject
[`Table`]: http://google.github.io/truth/api/latest/com/google/common/truth/TableSubject
