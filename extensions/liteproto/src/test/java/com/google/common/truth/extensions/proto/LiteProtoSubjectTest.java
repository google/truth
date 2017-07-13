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

import static com.google.common.truth.extensions.proto.LiteProtoTruth.assertThat;
import static org.junit.Assert.fail;

import com.google.auto.value.AutoValue;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.truth.Expect;
import com.google.common.truth.Subject;
import com.google.protobuf.MessageLite;
import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/** Unit tests for {@link LiteProtoSubject}. */
@RunWith(Parameterized.class)
public class LiteProtoSubjectTest {

  /**
   * We run (almost) all the tests for both proto2 and proto3 implementations. This class organizes
   * the parameters much more cleanly than a raw Object[].
   */
  @AutoValue
  public abstract static class Config {
    abstract MessageLite nonEmptyMessage();

    abstract MessageLite equivalentNonEmptyMessage();

    abstract MessageLite nonEmptyMessageOfOtherValue();

    abstract MessageLite nonEmptyMessageOfOtherType();

    abstract MessageLite defaultInstance();

    abstract MessageLite defaultInstanceOfOtherType();

    abstract Optional<MessageLite> messageWithoutRequiredFields();

    public static Builder newBuilder() {
      return new AutoValue_LiteProtoSubjectTest_Config.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
      abstract Builder setNonEmptyMessage(MessageLite messageLite);

      abstract Builder setEquivalentNonEmptyMessage(MessageLite messageLite);

      abstract Builder setNonEmptyMessageOfOtherValue(MessageLite messageLite);

      abstract Builder setNonEmptyMessageOfOtherType(MessageLite messageLite);

      abstract Builder setDefaultInstance(MessageLite messageLite);

      abstract Builder setDefaultInstanceOfOtherType(MessageLite messageLite);

      abstract Builder setMessageWithoutRequiredFields(MessageLite messageLite);

      abstract Config build();
    }
  }

  @Parameters(name = "{0}")
  public static Collection<Object[]> data() {
    // Missing a required_int field.
    TestMessageLite2WithRequiredFields withoutRequiredFields =
        TestMessageLite2WithRequiredFields.newBuilder().setOptionalString("foo").buildPartial();

    Config proto2Config =
        Config.newBuilder()
            .setNonEmptyMessage(TestMessageLite2.newBuilder().setOptionalInt(3).build())
            .setEquivalentNonEmptyMessage(TestMessageLite2.newBuilder().setOptionalInt(3).build())
            .setNonEmptyMessageOfOtherValue(
                TestMessageLite2.newBuilder()
                    .setOptionalInt(3)
                    .setSubMessage(
                        TestMessageLite2.SubMessage.newBuilder().setOptionalString("foo"))
                    .build())
            .setNonEmptyMessageOfOtherType(
                OtherTestMessageLite2.newBuilder().setOptionalInt(3).build())
            .setDefaultInstance(TestMessageLite2.newBuilder().buildPartial())
            .setDefaultInstanceOfOtherType(OtherTestMessageLite2.newBuilder().buildPartial())
            .setMessageWithoutRequiredFields(withoutRequiredFields)
            .build();
    Config proto3Config =
        Config.newBuilder()
            .setNonEmptyMessage(TestMessageLite3.newBuilder().setOptionalInt(3).build())
            .setEquivalentNonEmptyMessage(TestMessageLite3.newBuilder().setOptionalInt(3).build())
            .setNonEmptyMessageOfOtherValue(
                TestMessageLite3.newBuilder()
                    .setOptionalInt(3)
                    .setSubMessage(
                        TestMessageLite3.SubMessage.newBuilder().setOptionalString("foo"))
                    .build())
            .setNonEmptyMessageOfOtherType(
                OtherTestMessageLite3.newBuilder().setOptionalInt(3).build())
            .setDefaultInstance(TestMessageLite3.newBuilder().buildPartial())
            .setDefaultInstanceOfOtherType(OtherTestMessageLite3.newBuilder().buildPartial())
            .build();
    return ImmutableList.of(
        new Object[] {"Proto 2", proto2Config}, new Object[] {"Proto 3", proto3Config});
  }

  @Rule public final Expect expect = Expect.create();
  private final Config config;

  public LiteProtoSubjectTest(@SuppressWarnings("unused") String name, Config config) {
    this.config = config;
  }

  private LiteProtoSubject<?, MessageLite> expectThat(@Nullable MessageLite m) {
    return expect.about(LiteProtoTruth.liteProtos()).that(m);
  }

  private Subject<?, ?> expectThat(@Nullable Object o) {
    return expect.that(o);
  }

  @Test
  public void testSubjectMethods() {
    expectThat(config.nonEmptyMessage()).isSameAs(config.nonEmptyMessage());
    expectThat(config.nonEmptyMessage().toBuilder()).isNotSameAs(config.nonEmptyMessage());

    expectThat(config.nonEmptyMessage()).isInstanceOf(MessageLite.class);
    expectThat(config.nonEmptyMessage().toBuilder()).isInstanceOf(MessageLite.Builder.class);
    expectThat(config.nonEmptyMessage()).isNotInstanceOf(MessageLite.Builder.class);
    expectThat(config.nonEmptyMessage().toBuilder()).isNotInstanceOf(MessageLite.class);

    expectThat(config.nonEmptyMessage()).isIn(Arrays.asList(config.nonEmptyMessage()));
    expectThat(config.nonEmptyMessage())
        .isNotIn(Arrays.asList(config.nonEmptyMessageOfOtherValue()));
    expectThat(config.nonEmptyMessage())
        .isAnyOf(config.nonEmptyMessage(), config.nonEmptyMessageOfOtherValue());
    expectThat(config.nonEmptyMessage())
        .isNoneOf(config.nonEmptyMessageOfOtherValue(), config.nonEmptyMessageOfOtherType());
  }

  @Test
  public void testIsEqualTo_success() {
    expectThat(null).isEqualTo(null);
    expectThat(null).isNull();

    expectThat(config.nonEmptyMessage()).isEqualTo(config.nonEmptyMessage());
    expectThat(config.nonEmptyMessage()).isEqualTo(config.equivalentNonEmptyMessage());
    expectThat(config.nonEmptyMessage()).isNotEqualTo(config.nonEmptyMessage().toBuilder());

    assertThat(config.defaultInstance()).isNotEqualTo(config.defaultInstanceOfOtherType());
    assertThat(config.nonEmptyMessage()).isNotEqualTo(config.nonEmptyMessageOfOtherType());
    assertThat(config.nonEmptyMessage()).isNotEqualTo(config.nonEmptyMessageOfOtherValue());
  }

  @Test
  public void testIsEqualTo_failure() {
    try {
      assertThat(config.nonEmptyMessage()).isEqualTo(config.nonEmptyMessageOfOtherValue());
      fail("Should have failed.");
    } catch (AssertionError e) {
      expectRegex(e, ".*expected:.*\"foo\".*");
      expectNoRegex(e, ".*but was:.*\"foo\".*");
    }

    try {
      assertThat(config.nonEmptyMessage()).isEqualTo(config.nonEmptyMessageOfOtherType());
      fail("Should have failed.");
    } catch (AssertionError e) {
      expectRegex(
          e,
          "Not true that \\(.*\\) proto is equal to the expected \\(.*\\) object\\.\\s*"
              + "They are not of the same class\\.");
    }

    try {
      assertThat(config.nonEmptyMessage()).isNotEqualTo(config.equivalentNonEmptyMessage());
      fail("Should have failed.");
    } catch (AssertionError e) {
      expectRegex(
          e,
          String.format(
              "Not true that protos are different\\.\\s*Both are \\(%s\\) <.*optional_int: 3.*>\\.",
              Pattern.quote(config.nonEmptyMessage().getClass().getName())));
    }
  }

  @Test
  public void testHasAllRequiredFields_success() {
    expectThat(config.nonEmptyMessage()).hasAllRequiredFields();
  }

  @Test
  public void testHasAllRequiredFields_failures() {
    if (!config.messageWithoutRequiredFields().isPresent()) {
      return;
    }

    try {
      assertThat(config.messageWithoutRequiredFields().get()).hasAllRequiredFields();
      fail("Should have failed.");
    } catch (AssertionError e) {
      expectRegex(
          e,
          "Not true that <.*> has all required fields set\\.\\s*"
              + "\\(Lite runtime could not determine which fields were missing\\.\\)");
    }
  }

  @Test
  public void testDefaultInstance_success() {
    expectThat(config.defaultInstance()).isEqualToDefaultInstance();
    expectThat(config.defaultInstanceOfOtherType()).isEqualToDefaultInstance();
    expectThat(config.nonEmptyMessage().getDefaultInstanceForType()).isEqualToDefaultInstance();

    expectThat(null).isNotEqualToDefaultInstance();
    expectThat(config.nonEmptyMessage()).isNotEqualToDefaultInstance();
  }

  @Test
  public void testDefaultInstance_failure() {
    try {
      assertThat(config.nonEmptyMessage()).isEqualToDefaultInstance();
      fail("Should have failed.");
    } catch (AssertionError e) {
      expectRegex(
          e,
          "Not true that <.*optional_int:\\s*3.*> is a default proto instance\\.\\s*"
              + "It has set values\\.");
    }

    try {
      assertThat(config.defaultInstance()).isNotEqualToDefaultInstance();
      fail("Should have failed.");
    } catch (AssertionError e) {
      expectRegex(
          e,
          String.format(
              "Not true that \\(%s\\) <.*\\[empty proto\\].*> is not a default "
                  + "proto instance\\.\\s*It has no set values\\.",
              Pattern.quote(config.defaultInstance().getClass().getName())));
    }
  }

  @Test
  public void testSerializedSize_success() {
    int size = config.nonEmptyMessage().getSerializedSize();
    expectThat(config.nonEmptyMessage()).serializedSize().isEqualTo(size);
    expectThat(config.defaultInstance()).serializedSize().isEqualTo(0);
  }

  @Test
  public void testSerializedSize_failure() {
    int size = config.nonEmptyMessage().getSerializedSize();

    try {
      assertThat(config.nonEmptyMessage()).serializedSize().isGreaterThan(size);
      fail("Should have failed.");
    } catch (AssertionError e) {
      expectRegex(
          e,
          "Not true that sizeOf\\(<.*optional_int:\\s*3.*>\\) \\(<2>\\) " + "is greater than <2>");
    }

    try {
      assertThat(config.defaultInstance()).serializedSize().isGreaterThan(0);
      fail("Should have failed.");
    } catch (AssertionError e) {
      expectRegex(
          e,
          "Not true that sizeOf\\(<.*\\[empty proto\\].*>\\) \\(<0>\\) " + "is greater than <0>");
    }
  }

  // TODO(cgruber): These probably belong in ThrowableSubject.
  private void expectRegex(AssertionError e, String regex) {
    expect
        .withMessage(String.format("Expected <%s> to match '%s'.", regex, e.getMessage()))
        .that(Pattern.compile(regex, Pattern.DOTALL).matcher(e.getMessage()).matches())
        .isTrue();
  }

  private void expectNoRegex(AssertionError e, String regex) {
    expect
        .withMessage(String.format("Expected <%s> to match '%s'.", regex, e.getMessage()))
        .that(Pattern.compile(regex, Pattern.DOTALL).matcher(e.getMessage()).matches())
        .isFalse();
  }
}
