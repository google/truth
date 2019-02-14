---
layout: default
title: Protobuf Extension
---


ProtoTruth is an extension of the Truth library which lets you make assertions
about [Protobufs], a rich data interchange format. To get started:

1.  Add the appropriate dependency to your build:

    **Maven:**

    ```xml
    <dependency>
      <groupId>com.google.truth.extensions</groupId>
      <artifactId>truth-proto-extension</artifactId>
      <version>0.31</version>
    </dependency>
    ```

    **Gradle:**

    ```groovy
    buildscript {
      repositories.mavenLocal()
    }
    dependencies {
      testCompile "com.google.truth.extensions:truth-proto-extension:0.31"
    }
    ```

    Use `truth-liteproto-extension` if you are using the lite version of
    Protobufs. Note that this extension lacks many of the features in the full
    extension due to the lack of runtime descriptors.  You'll also need to
    import LiteProtoTruth instead of ProtoTruth.


1.  Add a static import:

    ```java
    import static com.google.common.truth.extensions.proto.ProtoTruth.assertThat;
    ```

    NOTE: `ProtoTruth.assertThat` and `Truth.assertThat` can both be statically
    imported in the same file. Java handles overloaded static imports much like
    overloads defined in the same class.

1.  Assert away!

    ```java
    MyProto expected = MyProto.newBuilder().addBar(3).addBar(5).setFoo(“qux”).build();
    MyProto actual =   MyProto.newBuilder().addBar(5).addBar(3).setFoo(“quizzux”).build();
    assertThat(actual).ignoringRepeatedFieldOrder().isEqualTo(expected);
    ```

    This assertion yields the following failure message:

    ```shell
    Not true that messages compare equal.  Differences were found:
    modified: foo: ”qux” -> “quizzux”
    ```

## Supported Use Cases {#support}

Support exists for:

*   Assertions on singular, generic Proto 2 messages.
*   Assertions on singular, generic Proto 3 messages.
*   Assertions on Iterables, Maps, and Multimaps of such messages.
*   [Lite protos](#lite), though support is nominal

Support does not currently exist (yet) for:

*   Tailored assertions for extension fields for Proto 2.
*   Tailored assertions for the [`UnknownFieldSet`].
*   Tailored assertions for utility protos, including [`FieldMasks`], and the
    [`Any`] type, for Proto 3.

Support will not exist for:

*   [gRPC](http://www.grpc.io/), for which Truth is not an appropriate home.
    Mocking libraries are better suited to testing RPC interactions.


## Strict default behavior {#defaults}

By default, the extension is fairly strict and requires you to explicitly state
your test assumptions. It will:

*   _not ignore_ field absence; to change this behavior, use:
    `ignoringFieldAbsence()`
*   _not ignore_ repeated field order; to change this behavior, use:
    `ignoringRepeatedFieldOrder()`
*   _not report_ mismatches only; to change this behavior, use:
    `reportingMismatchesOnly()`
*   _use exact equality_ for floating-point fields; to change this behaviour,
    use: `usingDoubleTolerance` and/or `usingFloatTolerance`
*   check all fields; to change this behavior, use a combination of:
    *   `ignoringFields(int...)`
    *   `ignoringFieldDescriptors(FieldDescriptor...)`
    *   `withPartialScope(FieldScope)`
    *   `comparingExpectedFieldsOnly()`
    *   `ignoringFieldScope(FieldScope)`

In summary, the default `isEqualTo()` behavior of [`ProtoSubject`] is
behaviorally identical to [`Subject`] behavior. However, you will get much
better error messaging in the event of a failure.


## Getting better failure messages by enabling pairing of `Iterable` elements {#displayingDiffsPairedBy}

When an assertion involving an `Iterable` of protos fails, the failure messages
can often be hard to understand. If you know of some key function which uniquely
indexes the expected protos, you can use the `displayingDiffsPairedBy` method to
tell ProtoTruth about it. For example, if you have a proto called `Record`, and
you're making an assertion about an `Iterable<Record>`, and the expected records
have unique values of an `id` field, then you could write this:

```java
assertThat(actualRecordProtos)
    .displayingDiffsPairedBy(Record::getId)
    .containsExactlyElementsIn(expectedRecords);
```

If this assertion fails, the failure message will pair up any missing and
unexpected elements by their IDs. For example, it might tell you that the actual
`Iterable` was missing an element with ID 2, that it had an unexpected element
with ID 3, or that the element with ID 4 wasn't equivalent to the one it
expected... and in this last case, it will also show a field-by-field diff
between the proto it got and the one it expected.

(If an assertion about a `Map` fails, the failure message will automatically
match up any missing and unexpected entries using their keys. You can think of
the `displayingDiffsPairedBy` method as providing an equivalent for an assertion
about an `Iterable`. Note that this won't affect whether the test passes or
fails.)

<!-- References -->

[Protobufs]:         https://developers.google.com/protocol-buffers/docs/overview
[`UnknownFieldSet`]: https://developers.google.com/protocol-buffers/docs/reference/java/com/google/protobuf/UnknownFieldSet
[`FieldMasks`]:      https://github.com/google/protobuf/blob/master/src/google/protobuf/field_mask.proto
[`Any`]:             https://github.com/google/protobuf/blob/master/src/google/protobuf/any.proto
[`ProtoSubject`]:    https://github.com/google/truth/blob/master/extensions/proto/src/main/java/com/google/common/truth/extensions/proto/ProtoSubject.java
[`Subject`]:         https://github.com/google/truth/blob/master/extensions/proto/src/main/java/com/google/common/truth/Subject.java

