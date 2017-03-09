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

*   Java 8 types (see go/truth/faq#java8)

    *   [`Optional`] - and `OptionalDouble`, `OptionalLong`, `OptionalInt`
    *   [`Stream`]

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

    *   [`Optional`](http://google.github.io/truth/api/latest/com/google/common/truth/GuavaOptionalSubject)
    *   [`Multimap`]
        *   [`ListMultimap`]
        *   [`SetMultimap`]
    *   [`Multiset`]
    *   [`Table`]

<!-- References -->

<!-- TODO(kak): Update the 2 Java 8 links once they have public javadocs -->

[`BigDecimal`]: http://google.github.io/truth/api/latest/com/google/common/truth/BigDecimalSubject
[`Boolean`]: http://google.github.io/truth/api/latest/com/google/common/truth/BooleanSubject
[BooleanArray]: http://google.github.io/truth/api/latest/com/google/common/truth/PrimitiveBooleanArraySubject
[ByteArray]: http://google.github.io/truth/api/latest/com/google/common/truth/PrimitiveByteArraySubject
[CharacterArray]: http://google.github.io/truth/api/latest/com/google/common/truth/PrimitiveCharArraySubject
[`Class`]: http://google.github.io/truth/api/latest/com/google/common/truth/ClassSubject
[`Comparable`]: http://google.github.io/truth/api/latest/com/google/common/truth/ComparableSubject
[`Double`]: http://google.github.io/truth/api/latest/com/google/common/truth/DoubleSubject
[DoubleArray]: http://google.github.io/truth/api/latest/com/google/common/truth/PrimitiveDoubleArraySubject
[`Float`]: http://google.github.io/truth/api/latest/com/google/common/truth/FloatSubject
[FloatArray]: http://google.github.io/truth/api/latest/com/google/common/truth/PrimitiveFloatArraySubject
[`Integer`]: http://google.github.io/truth/api/latest/com/google/common/truth/IntegerSubject
[IntegerArray]: http://google.github.io/truth/api/latest/com/google/common/truth/PrimitiveIntArraySubject
[`Iterable`]: http://google.github.io/truth/api/latest/com/google/common/truth/IterableSubject
[`ListMultimap`]: http://google.github.io/truth/api/latest/com/google/common/truth/ListMultimapSubject
[`Long`]: http://google.github.io/truth/api/latest/com/google/common/truth/LongSubject
[LongArray]: http://google.github.io/truth/api/latest/com/google/common/truth/PrimitiveLongArraySubject
[`Map`]: http://google.github.io/truth/api/latest/com/google/common/truth/MapSubject
[`Multimap`]: http://google.github.io/truth/api/latest/com/google/common/truth/MultimapSubject
[`Multiset`]: http://google.github.io/truth/api/latest/com/google/common/truth/MultisetSubject
[`Object`]: http://google.github.io/truth/api/latest/com/google/common/truth/Subject
[ObjectArray]: http://google.github.io/truth/api/latest/com/google/common/truth/ObjectArraySubject
[`Optional`]: https://github.com/google/truth/blob/master/extensions/java8/src/main/java/com/google/common/truth/OptionalSubject.java
[`SetMultimap`]: http://google.github.io/truth/api/latest/com/google/common/truth/SetMultimapSubject
[ShortArray]: http://google.github.io/truth/api/latest/com/google/common/truth/PrimitiveShortArraySubject
[`Stream`]: https://github.com/google/truth/blob/master/extensions/java8/src/main/java/com/google/common/truth/StreamSubject.java
[`String`]: http://google.github.io/truth/api/latest/com/google/common/truth/StringSubject
[`Table`]: http://google.github.io/truth/api/latest/com/google/common/truth/TableSubject
[`Throwable`]: http://google.github.io/truth/api/latest/com/google/common/truth/ThrowableSubject
