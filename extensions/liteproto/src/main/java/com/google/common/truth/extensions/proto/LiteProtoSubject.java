/*
 * Copyright (c) 2016 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.common.truth.extensions.proto;

import static com.google.common.base.Strings.lenientFormat;
import static com.google.common.truth.Fact.simpleFact;

import com.google.common.base.Objects;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.IntegerSubject;
import com.google.common.truth.Subject;
import com.google.common.truth.Truth;
import com.google.errorprone.annotations.CheckReturnValue;
import com.google.protobuf.MessageLite;
import java.util.regex.Pattern;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

/**
 * Truth subjects for the Lite version of Protocol Buffers.
 *
 * <p>LiteProtoSubject supports versions 2 and 3 of Protocol Buffers. Due to the lack of runtime
 * descriptors, its functionality is limited compared to ProtoSubject, in particular in performing
 * detailed comparisons between messages.
 *
 * @param <S> Subject class type.
 * @param <M> MessageLite type.
 */
@CheckReturnValue
public class LiteProtoSubject<S extends LiteProtoSubject<S, M>, M extends MessageLite>
    extends Subject<S, M> {

  /**
   * Typed extension of {@link Subject.Factory}.
   *
   * <p>The existence of this class is necessary in order to satisfy the generic constraints of
   * {@link Truth#assertAbout(Subject.Factory)}, whilst also hiding the Untyped classes which are
   * not meant to be exposed.
   */
  public abstract static class Factory<S extends LiteProtoSubject<S, M>, M extends MessageLite>
      implements Subject.Factory<S, M> {}

  /**
   * Returns a SubjectFactory for {@link MessageLite} subjects which you can use to assert things
   * about Lite Protobuf properties.
   */
  static Factory<?, MessageLite> liteProtos() {
    return MessageLiteSubjectFactory.INSTANCE;
  }

  protected LiteProtoSubject(FailureMetadata failureMetadata, @NullableDecl M messageLite) {
    super(failureMetadata, messageLite);
  }

  // It is wrong to compare protos using their string representations. The MessageLite runtime
  // deliberately prefixes debug strings with their Object.toString() to discourage string
  // comparison. However, this reads poorly in tests, and makes it harder to identify differences
  // from the strings alone. So, we manually strip this prefix.
  // In case the class names are actually relevant, Subject.isEqualTo() will add them back for us.
  // TODO(user): Maybe get a way to do this upstream.
  static String getTrimmedToString(@NullableDecl MessageLite messageLite) {
    String subjectString = String.valueOf(messageLite).trim();
    if (subjectString.startsWith("# ")) {
      String objectToString =
          String.format(
              "# %s@%s",
              messageLite.getClass().getName(), Integer.toHexString(messageLite.hashCode()));
      if (subjectString.startsWith(objectToString)) {
        subjectString = subjectString.replaceFirst(Pattern.quote(objectToString), "").trim();
      }
    }

    return subjectString.isEmpty() ? "[empty proto]" : subjectString;
  }

  @Override
  protected String actualCustomStringRepresentation() {
    return getTrimmedToString(actual());
  }

  /**
   * Checks whether the MessageLite is equivalent to the argument, using the standard equals()
   * implementation.
   */
  @Override
  public void isEqualTo(@NullableDecl Object expected) {
    // TODO(user): Do better here when MessageLite descriptors are available.
    if (Objects.equal(actual(), expected)) {
      return;
    }

    if (actual() == null || expected == null) {
      super.isEqualTo(expected);
    } else if (actual().getClass() != expected.getClass()) {
      failWithoutActual(
          simpleFact(
              lenientFormat(
                  "Not true that (%s) %s is equal to the expected (%s) object. "
                      + "They are not of the same class.",
                  actual().getClass().getName(),
                  internalCustomName() != null ? internalCustomName() + " (proto)" : "proto",
                  expected.getClass().getName())));
    } else {
      /*
       * TODO(cpovirk): If we someday let subjects override formatActualOrExpected(), change this
       * class to do so, and make this code path always delegate to super.isEqualTo().
       */
      String ourString = getTrimmedToString(actual());
      String theirString = getTrimmedToString((MessageLite) expected);
      if (!ourString.equals(theirString)) {
        check().that(ourString).isEqualTo(theirString); // fails
      } else {
        // This will include the Object.toString() headers.
        super.isEqualTo(expected);
      }
    }
  }

  /**
   * @deprecated A Builder can never compare equal to a MessageLite instance. Use {@code build()},
   *     or {@code buildPartial()} on the argument to get a MessageLite for comparison instead.
   */
  @Deprecated
  public void isEqualTo(@NullableDecl MessageLite.Builder builder) {
    isEqualTo((Object) builder);
  }

  @Override
  public void isNotEqualTo(@NullableDecl Object expected) {
    if (Objects.equal(actual(), expected)) {
      if (actual() == null) {
        super.isNotEqualTo(expected);
      } else {
        failWithoutActual(
            simpleFact(
                lenientFormat(
                    "Not true that protos are different. Both are (%s) <%s>.",
                    actual().getClass().getName(), getTrimmedToString(actual()))));
      }
    }
  }

  /**
   * @deprecated A Builder will never compare equal to a MessageLite instance. Use {@code build()},
   *     or {@code buildPartial()} on the argument to get a MessageLite for comparison instead.
   */
  @Deprecated
  public void isNotEqualTo(@NullableDecl MessageLite.Builder builder) {
    isNotEqualTo((Object) builder);
  }

  /** Checks whether the subject is a {@link MessageLite} with no fields set. */
  public void isEqualToDefaultInstance() {
    if (actual() == null) {
      failWithoutActual(
          simpleFact(
              lenientFormat(
                  "Not true that %s is a default proto instance. It is null.", actualAsString())));
    } else if (!actual().equals(actual().getDefaultInstanceForType())) {
      failWithoutActual(
          simpleFact(
              lenientFormat(
                  "Not true that %s is a default proto instance. It has set values.",
                  actualAsString())));
    }
  }

  /** Checks whether the subject is not equivalent to a {@link MessageLite} with no fields set. */
  public void isNotEqualToDefaultInstance() {
    if (actual() != null && actual().equals(actual().getDefaultInstanceForType())) {
      failWithoutActual(
          simpleFact(
              lenientFormat(
                  "Not true that (%s) %s is not a default proto instance. It has no set values.",
                  actual().getClass().getName(), actualAsString())));
    }
  }

  /**
   * Checks whether the subject has all required fields set. Cannot fail for a proto built with
   * {@code build()}, which itself fails if required fields aren't set.
   */
  public void hasAllRequiredFields() {
    if (!actual().isInitialized()) {
      // MessageLite doesn't support reflection so this is the best we can do.
      failWithoutActual(
          simpleFact(
              lenientFormat(
                  "Not true that %s has all required fields set. "
                      + "(Lite runtime could not determine which fields were missing.)",
                  actualAsString())));
    }
  }

  /**
   * Returns an {@link IntegerSubject} on the serialized size of the MessageLite.
   *
   * <p>Assertions can then be changed on the serialized size, to support checks such as {@code
   * assertThat(myProto).serializedSize().isAtLeast(16)}, etc.
   */
  public IntegerSubject serializedSize() {
    return check("getSerializedSize()").that(actual().getSerializedSize());
  }

  static final class MessageLiteSubject extends LiteProtoSubject<MessageLiteSubject, MessageLite> {
    MessageLiteSubject(FailureMetadata failureMetadata, @NullableDecl MessageLite messageLite) {
      super(failureMetadata, messageLite);
    }
  }

  private static final class MessageLiteSubjectFactory
      extends Factory<MessageLiteSubject, MessageLite> {
    private static final MessageLiteSubjectFactory INSTANCE = new MessageLiteSubjectFactory();

    @Override
    public MessageLiteSubject createSubject(
        FailureMetadata failureMetadata, @NullableDecl MessageLite messageLite) {
      return new MessageLiteSubject(failureMetadata, messageLite);
    }
  }
}
