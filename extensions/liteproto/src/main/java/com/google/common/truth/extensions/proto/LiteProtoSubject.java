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
import static com.google.common.truth.Fact.fact;
import static com.google.common.truth.Fact.simpleFact;

import com.google.common.base.Objects;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.IntegerSubject;
import com.google.common.truth.Subject;
import com.google.errorprone.annotations.CheckReturnValue;
import com.google.protobuf.MessageLite;
import java.util.regex.Pattern;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Truth subjects for the Lite version of Protocol Buffers.
 *
 * <p>LiteProtoSubject supports versions 2 and 3 of Protocol Buffers. Due to the lack of runtime
 * descriptors, its functionality is limited compared to ProtoSubject, in particular in performing
 * detailed comparisons between messages.
 */
@CheckReturnValue
public class LiteProtoSubject extends Subject {

  /**
   * Returns a {@code Subject.Factory} for {@link MessageLite} subjects which you can use to assert
   * things about Lite Protobuf properties.
   */
  static Factory<LiteProtoSubject, MessageLite> liteProtos() {
    return LiteProtoSubjectFactory.INSTANCE;
  }

  /*
   * Storing a FailureMetadata instance in a Subject subclass is generally a bad practice: The
   * FailureMetadata instance contains the chain of subjects used to create this one (as in
   * assertThat(value).hasFooThat().hasBarThat()). Reusing that instance for a different Subject
   * (like one produced by a hasBazThat() method) would result in a Subject with the wrong chain,
   * potentially producing misleading failure messages. To get the messages right, Subject
   * subclasses should instead use check(...).
   *
   * However... here we are using the FailureMetadata instance only to create a Subject for an
   * "equivalent" object. Thus, it will still be accurate for the failure message to be written as
   * if it's talking about the proto itself.
   *
   * TODO(b/127819891): Use a better API for this if one is addded.
   */
  private final FailureMetadata metadata;
  private final MessageLite actual;

  protected LiteProtoSubject(FailureMetadata failureMetadata, @Nullable MessageLite messageLite) {
    super(failureMetadata, messageLite);
    this.metadata = failureMetadata;
    this.actual = messageLite;
  }

  // It is wrong to compare protos using their string representations. The MessageLite runtime
  // deliberately prefixes debug strings with their Object.toString() to discourage string
  // comparison. However, this reads poorly in tests, and makes it harder to identify differences
  // from the strings alone. So, we manually strip this prefix.
  // In case the class names are actually relevant, Subject.isEqualTo() will add them back for us.
  // TODO(user): Maybe get a way to do this upstream.
  static String getTrimmedToString(@Nullable MessageLite messageLite) {
    String subjectString = String.valueOf(messageLite);
    String trimmedSubjectString = subjectString.trim();
    if (trimmedSubjectString.startsWith("# ")) {
      String objectToString =
          String.format(
              "# %s@%s",
              messageLite.getClass().getName(), Integer.toHexString(messageLite.hashCode()));
      if (trimmedSubjectString.startsWith(objectToString)) {
        subjectString = trimmedSubjectString.replaceFirst(Pattern.quote(objectToString), "").trim();
      }
    }

    return subjectString.isEmpty() ? "[empty proto]" : subjectString;
  }

  @Override
  protected String actualCustomStringRepresentation() {
    return actualCustomStringRepresentationForProtoPackageMembersToCall();
  }

  final String actualCustomStringRepresentationForProtoPackageMembersToCall() {
    return getTrimmedToString(actual);
  }

  /**
   * Checks whether the MessageLite is equivalent to the argument, using the standard equals()
   * implementation.
   */
  @Override
  public void isEqualTo(@Nullable Object expected) {
    // TODO(user): Do better here when MessageLite descriptors are available.
    if (Objects.equal(actual, expected)) {
      return;
    }

    if (actual == null || expected == null) {
      super.isEqualTo(expected);
    } else if (actual.getClass() != expected.getClass()) {
      failWithoutActual(
          simpleFact(
              lenientFormat(
                  "Not true that (%s) proto is equal to the expected (%s) object. "
                      + "They are not of the same class.",
                  actual.getClass().getName(), expected.getClass().getName())));
    } else {
      /*
       * TODO(cpovirk): If we someday let subjects override formatActualOrExpected(), change this
       * class to do so, and make this code path always delegate to super.isEqualTo().
       */
      String ourString = getTrimmedToString(actual);
      String theirString = getTrimmedToString((MessageLite) expected);
      if (!ourString.equals(theirString)) {
        new LiteProtoAsStringSubject(metadata, ourString).isEqualTo(theirString); // fails
      } else {
        // This will include the Object.toString() headers.
        super.isEqualTo(expected);
      }
    }
  }

  /**
   * @deprecated A Builder can never compare equal to a MessageLite instance. Use {@code build()},
   *     or {@code buildPartial()} on the argument to get a MessageLite for comparison instead. Or,
   *     if you are passing {@code null}, use {@link #isNull()}.
   */
  /*
   * TODO(cpovirk): Consider @DoNotCall -- or probably some other static analysis, given the problem
   * discussed in the rest of this comment.
   *
   * The problem: isEqualTo(null) resolves to this overload (since this overload is more specific
   * than isEqualTo(Object)), so @DoNotCall would break all assertions of that form.
   *
   * To address that, we could try also adding something like `<NullT extends Impossible &
   * MessageLite.Builder> void isEqualTo(NullT)` and hoping that isEqualTo(null) would resolve to
   * that instead. That would also have the benefit of making isEqualTo(null) not produce a
   * deprecation warning (though of course people "should" use isNull(): b/17294077). But yuck.
   *
   * Given the null issue, maybe we should never have added this overload in the first place,
   * instead adding static analysis specific to MessageLite-MessageLite.Builder comparisons. (Sadly,
   * we can't remove it now without breaking binary compatibility.)
   *
   * Still, we could add static analysis to produce a compile error for isEqualTo(Builder) this even
   * today, even without using @DoNotCall. And then we could consider removing @Deprecated to stop
   * spamming the people who call isEqualTo(null).
   */
  @Deprecated
  public void isEqualTo(MessageLite.@Nullable Builder builder) {
    isEqualTo((Object) builder);
  }

  private static final class LiteProtoAsStringSubject extends Subject {
    LiteProtoAsStringSubject(FailureMetadata metadata, @Nullable String actual) {
      super(metadata, actual);
    }
  }

  @Override
  public void isNotEqualTo(@Nullable Object expected) {
    if (Objects.equal(actual, expected)) {
      if (actual == null) {
        super.isNotEqualTo(expected);
      } else {
        failWithoutActual(
            simpleFact(
                lenientFormat(
                    "Not true that protos are different. Both are (%s) <%s>.",
                    actual.getClass().getName(), getTrimmedToString(actual))));
      }
    }
  }

  /**
   * @deprecated A Builder will never compare equal to a MessageLite instance. Use {@code build()},
   *     or {@code buildPartial()} on the argument to get a MessageLite for comparison instead. Or,
   *     if you are passing {@code null}, use {@link #isNotNull()}.
   */
  // TODO(cpovirk): Consider @DoNotCall or other static analysis. (See isEqualTo(Builder).)
  @Deprecated
  public void isNotEqualTo(MessageLite.@Nullable Builder builder) {
    isNotEqualTo((Object) builder);
  }

  /** Checks whether the subject is a {@link MessageLite} with no fields set. */
  public void isEqualToDefaultInstance() {
    if (actual == null) {
      failWithoutActual(
          simpleFact(
              lenientFormat(
                  "Not true that <%s> is a default proto instance. It is null.",
                  actualCustomStringRepresentationForProtoPackageMembersToCall())));
    } else if (!actual.equals(actual.getDefaultInstanceForType())) {
      failWithoutActual(
          simpleFact(
              lenientFormat(
                  "Not true that <%s> is a default proto instance. It has set values.",
                  actualCustomStringRepresentationForProtoPackageMembersToCall())));
    }
  }

  /** Checks whether the subject is not equivalent to a {@link MessageLite} with no fields set. */
  public void isNotEqualToDefaultInstance() {
    if (actual != null && actual.equals(actual.getDefaultInstanceForType())) {
      failWithoutActual(
          simpleFact(
              lenientFormat(
                  "Not true that (%s) <%s> is not a default proto instance. It has no set values.",
                  actual.getClass().getName(),
                  actualCustomStringRepresentationForProtoPackageMembersToCall())));
    }
  }

  /**
   * Checks whether the subject has all required fields set. Cannot fail for a proto built with
   * {@code build()}, which itself fails if required fields aren't set.
   */
  public void hasAllRequiredFields() {
    if (!actual.isInitialized()) {
      // MessageLite doesn't support reflection so this is the best we can do.
      failWithoutActual(
          simpleFact("expected to have all required fields set"),
          fact("but was", actualCustomStringRepresentationForProtoPackageMembersToCall()),
          simpleFact("(Lite runtime could not determine which fields were missing.)"));
    }
  }

  /**
   * Returns an {@link IntegerSubject} on the serialized size of the MessageLite.
   *
   * <p>Assertions can then be changed on the serialized size, to support checks such as {@code
   * assertThat(myProto).serializedSize().isAtLeast(16)}, etc.
   */
  public IntegerSubject serializedSize() {
    return check("getSerializedSize()").that(actual.getSerializedSize());
  }

  private static final class LiteProtoSubjectFactory
      implements Factory<LiteProtoSubject, MessageLite> {
    private static final LiteProtoSubjectFactory INSTANCE = new LiteProtoSubjectFactory();

    @Override
    public LiteProtoSubject createSubject(
        FailureMetadata failureMetadata, @Nullable MessageLite messageLite) {
      return new LiteProtoSubject(failureMetadata, messageLite);
    }
  }
}
