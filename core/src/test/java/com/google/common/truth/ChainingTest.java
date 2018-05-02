/*
 * Copyright (c) 2018 Google, Inc.
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

package com.google.common.truth;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assert_;

import com.google.common.base.Objects;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for chained subjects (produced with {@link Subject#check()}, etc.). */
@RunWith(JUnit4.class)
public final class ChainingTest extends BaseSubjectTestCase {
  private static final Throwable throwable = new Throwable("root");

  @Test
  public void noChaining() {
    expectFailureWhenTestingThat("root").isThePresentKingOfFrance();
    assertNoCause("message");
  }

  @Test
  public void oneLevel() {
    expectFailureWhenTestingThat("root").delegatingTo("child").isThePresentKingOfFrance();
    assertNoCause("message");
  }

  @Test
  public void twoLevels() {
    expectFailureWhenTestingThat("root")
        .delegatingTo("child")
        .delegatingTo("grandchild")
        .isThePresentKingOfFrance();
    assertNoCause("message");
  }

  @Test
  public void noChainingRootThrowable() {
    expectFailureWhenTestingThat(throwable).isThePresentKingOfFrance();
    assertHasCause("message");
  }

  @Test
  public void oneLevelRootThrowable() {
    expectFailureWhenTestingThat(throwable).delegatingTo("child").isThePresentKingOfFrance();
    assertHasCause("message");
  }

  @Test
  public void twoLevelsRootThrowable() {
    expectFailureWhenTestingThat(throwable)
        .delegatingTo("child")
        .delegatingTo("grandchild")
        .isThePresentKingOfFrance();
    assertHasCause("message");
  }

  // e.g., future.failureCause()
  @Test
  public void oneLevelDerivedThrowable() {
    expectFailureWhenTestingThat("root").delegatingTo(throwable).isThePresentKingOfFrance();
    assertHasCause("message");
  }

  @Test
  public void twoLevelsDerivedThrowableMiddle() {
    expectFailureWhenTestingThat("root")
        .delegatingTo(throwable)
        .delegatingTo("grandchild")
        .isThePresentKingOfFrance();
    assertHasCause("message");
  }

  @Test
  public void twoLevelsDerivedThrowableLast() {
    expectFailureWhenTestingThat("root")
        .delegatingTo("child")
        .delegatingTo(throwable)
        .isThePresentKingOfFrance();
    assertHasCause("message");
  }

  @Test
  public void oneLevelNamed() {
    expectFailureWhenTestingThat("root")
        .delegatingToNamed("child", "child")
        .isThePresentKingOfFrance();
    assertNoCause("value of    : myObject.child\nmessage\nmyObject was: root");
  }

  @Test
  public void twoLevelsNamed() {
    expectFailureWhenTestingThat("root")
        .delegatingToNamed("child", "child")
        .delegatingToNamed("grandchild", "grandchild")
        .isThePresentKingOfFrance();
    assertNoCause("value of    : myObject.child.grandchild\nmessage\nmyObject was: root");
  }

  @Test
  public void twoLevelsOnlyFirstNamed() {
    expectFailureWhenTestingThat("root")
        .delegatingToNamed("child", "child")
        .delegatingTo("grandchild")
        .isThePresentKingOfFrance();
    assertNoCause("message\nmyObject was: root");
  }

  @Test
  public void twoLevelsOnlySecondNamed() {
    expectFailureWhenTestingThat("root")
        .delegatingTo("child")
        .delegatingToNamed("grandchild", "grandchild")
        .isThePresentKingOfFrance();
    assertNoCause("value of    : myObject.grandchild\nmessage\nmyObject was: root");
  }

  @Test
  public void oneLevelNamedNoNeedToDisplayBoth() {
    expectFailureWhenTestingThat("root")
        .delegatingToNamedNoNeedToDisplayBoth("child", "child")
        .isThePresentKingOfFrance();
    assertNoCause("value of: myObject.child\nmessage");
  }

  @Test
  public void twoLevelsNamedNoNeedToDisplayBoth() {
    expectFailureWhenTestingThat("root")
        .delegatingToNamedNoNeedToDisplayBoth("child", "child")
        .delegatingToNamedNoNeedToDisplayBoth("grandchild", "grandchild")
        .isThePresentKingOfFrance();
    assertNoCause("value of: myObject.child.grandchild\nmessage");
  }

  @Test
  public void twoLevelsOnlyFirstNamedNoNeedToDisplayBoth() {
    expectFailureWhenTestingThat("root")
        .delegatingToNamedNoNeedToDisplayBoth("child", "child")
        .delegatingTo("grandchild")
        .isThePresentKingOfFrance();
    assertNoCause("message");
  }

  @Test
  public void twoLevelsOnlySecondNamedNoNeedToDisplayBoth() {
    expectFailureWhenTestingThat("root")
        .delegatingTo("child")
        .delegatingToNamedNoNeedToDisplayBoth("grandchild", "grandchild")
        .isThePresentKingOfFrance();
    assertNoCause("value of: myObject.grandchild\nmessage");
  }

  @Test
  public void twoLevelsNamedOnlyFirstNoNeedToDisplayBoth() {
    expectFailureWhenTestingThat("root")
        .delegatingToNamedNoNeedToDisplayBoth("child", "child")
        .delegatingToNamed("grandchild", "grandchild")
        .isThePresentKingOfFrance();
    assertNoCause("value of    : myObject.child.grandchild\nmessage\nmyObject was: root");
  }

  @Test
  public void twoLevelsNamedOnlySecondNoNeedToDisplayBoth() {
    expectFailureWhenTestingThat("root")
        .delegatingToNamed("child", "child")
        .delegatingToNamedNoNeedToDisplayBoth("grandchild", "grandchild")
        .isThePresentKingOfFrance();
    assertNoCause("value of    : myObject.child.grandchild\nmessage\nmyObject was: root");
  }

  @Test
  public void namedAndComparisonFailure() {
    expectFailureWhenTestingThat("root").delegatingToNamed("child", "child").isEqualToString("z");
    assertNoCause(
        "value of: myObject.child\nmessage expected:<[child]> but was:<[z]>\nmyObject was: root");
  }

  @Test
  public void namedAndMessage() {
    expectFailure
        .whenTesting()
        .withMessage("prefix")
        .about(myObjects())
        .that("root")
        .delegatingToNamed("child", "child")
        .isThePresentKingOfFrance();
    assertNoCause("prefix\nvalue of    : myObject.child\nmessage\nmyObject was: root");
  }

  @Test
  public void checkFail() {
    expectFailureWhenTestingThat("root").doCheckFail();
    assertNoCause("message");
  }

  @Test
  public void checkFailWithName() {
    expectFailureWhenTestingThat("root").doCheckFail("child");
    assertNoCause("value of: myObject.child\nmessage\nmyObject was: root");
  }

  @Test
  public void badFormat() {
    try {
      Object unused = assertThat("root").check("%s %s", 1, 2, 3);
      assert_().fail();
    } catch (IllegalArgumentException expected) {
    }
  }

  /*
   * TODO(cpovirk): It would be nice to have multiple Subject subclasses so that we know we're
   * pulling the type from the right link in the chain. But we get some coverage of that from other
   * tests like MultimapSubjectTest.
   */

  private static final class MyObjectSubject extends Subject<MyObjectSubject, Object> {
    static final Factory<MyObjectSubject, Object> FACTORY =
        new Factory<MyObjectSubject, Object>() {
          @Override
          public MyObjectSubject createSubject(FailureMetadata metadata, Object actual) {
            return new MyObjectSubject(metadata, actual);
          }
        };

    private MyObjectSubject(FailureMetadata metadata, Object actual) {
      super(metadata, actual);
    }

    /** Runs a check that always fails with the generic message "message." */
    void isThePresentKingOfFrance() {
      failWithRawMessage("message");
    }

    /**
     * Checks that the value is equal to the given string, failing with the generic message
     * "message" and a {@link org.junit.ComparisonFailure} if not.
     */
    void isEqualToString(String expected) {
      if (!Objects.equal(actual(), expected)) {
        failComparing("message", String.valueOf(actual()), expected);
      }
    }

    void doCheckFail() {
      check().fail("message");
    }

    void doCheckFail(String name) {
      check(name).fail("message");
    }

    /**
     * Returns a new {@code MyObjectSubject} for the given actual value, chaining it to the current
     * subject with {@link Subject#check}.
     */
    MyObjectSubject delegatingTo(Object actual) {
      return check().about(myObjects()).that(actual);
    }

    /**
     * Returns a new {@code MyObjectSubject} for the given actual value, chaining it to the current
     * subject with {@link Subject#check}.
     */
    MyObjectSubject delegatingToNamed(Object actual, String name) {
      return check(name).about(myObjects()).that(actual);
    }

    MyObjectSubject delegatingToNamedNoNeedToDisplayBoth(Object actual, String name) {
      return checkNoNeedToDisplayBothValues(name).about(myObjects()).that(actual);
    }
  }

  private static Subject.Factory<MyObjectSubject, Object> myObjects() {
    return MyObjectSubject.FACTORY;
  }

  private MyObjectSubject expectFailureWhenTestingThat(Object actual) {
    return expectFailure.whenTesting().about(myObjects()).that(actual);
  }

  private void assertNoCause(String message) {
    assertThatFailure().hasMessageThat().isEqualTo(message);
    assertThatFailure().hasCauseThat().isNull();
  }

  private void assertHasCause(String message) {
    assertThatFailure().hasMessageThat().isEqualTo(message);
    assertThatFailure().hasCauseThat().isEqualTo(throwable);
  }

  private ThrowableSubject assertThatFailure() {
    return assertThat(expectFailure.getFailure());
  }
}
