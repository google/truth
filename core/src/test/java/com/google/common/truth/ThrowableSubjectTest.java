/*
 * Copyright (c) 2014 Google, Inc.
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

import static com.google.common.truth.ExpectFailure.expectFailure;
import static com.google.common.truth.FailureAssertions.assertFailureKeys;
import static com.google.common.truth.FailureAssertions.assertFailureValue;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

import java.io.IOException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link ThrowableSubject}. */
// We don't want to use ThrowableSubject when testing ThrowableSubject.
@SuppressWarnings({"GetMessageTruth", "AssertThatThrowableGetMessage"})
@RunWith(JUnit4.class)
public class ThrowableSubjectTest {

  @Test
  public void hasMessageThat() {
    NullPointerException npe = new NullPointerException("message");
    assertThat(npe).hasMessageThat().isEqualTo("message");
  }

  @Test
  public void hasMessageThat_null() {
    assertThat(new NullPointerException()).hasMessageThat().isNull();
    assertThat(new NullPointerException(null)).hasMessageThat().isNull();
  }

  @Test
  public void hasMessageThat_failure() {
    NullPointerException actual = new NullPointerException("message");
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(actual).hasMessageThat().isEqualTo("foobar"));
    assertFailureValue(e, "value of", "throwable.getMessage()");
    assertErrorHasActualAsCause(actual, e);
  }

  @Test
  public void hasMessageThat_messageHasNullMessage_failure() {
    expectFailure(
        whenTesting ->
            whenTesting.that(new NullPointerException("message")).hasMessageThat().isNull());
  }

  @Test
  public void hasMessageThat_nullMessageHasMessage_failure() {
    NullPointerException npe = new NullPointerException(null);
    expectFailure(whenTesting -> whenTesting.that(npe).hasMessageThat().isEqualTo("message"));
  }

  @Test
  public void hasMessageThat_tooDeep_failure() {
    Exception actual = new Exception("foobar");
    AssertionError e =
        expectFailure(
            whenTesting -> whenTesting.that(actual).hasCauseThat().hasMessageThat().isNull());
    assertFailureKeys(
        e, "Attempt to assert about the message of a null Throwable", "null Throwable was");
    assertFailureValue(e, "null Throwable was", "throwable.getCause()");
    assertErrorHasActualAsCause(actual, e);
  }

  @Test
  public void hasMessageThat_onNull() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that((Throwable) null).hasMessageThat());
    assertFailureKeys(e, "Attempt to assert about the message of a null Throwable");
  }

  @Test
  public void hasCauseThat_message() {
    assertThat(new Exception("foobar", new IOException("barfoo")))
        .hasCauseThat()
        .hasMessageThat()
        .isEqualTo("barfoo");
  }

  @Test
  public void hasCauseThat_instanceOf() {
    assertThat(new Exception("foobar", new IOException("barfoo")))
        .hasCauseThat()
        .isInstanceOf(IOException.class);
  }

  @Test
  public void hasCauseThat_null() {
    assertThat(new Exception("foobar")).hasCauseThat().isNull();
  }

  @Test
  public void hasCauseThat_message_failure() {
    Exception actual = new Exception("foobar", new IOException("barfoo"));
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting.that(actual).hasCauseThat().hasMessageThat().isEqualTo("message"));
    assertFailureValue(e, "value of", "throwable.getCause().getMessage()");
    assertErrorHasActualAsCause(actual, e);
  }

  @Test
  public void hasCauseThat_instanceOf_failure() {
    Exception actual = new Exception("foobar", new IOException("barfoo"));
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting.that(actual).hasCauseThat().isInstanceOf(RuntimeException.class));
    assertFailureValue(e, "value of", "throwable.getCause()");
    assertErrorHasActualAsCause(actual, e);
  }

  @Test
  public void hasCauseThat_tooDeep_failure() {
    Exception actual = new Exception("foobar");
    AssertionError e =
        expectFailure(
            whenTesting -> whenTesting.that(actual).hasCauseThat().hasCauseThat().isNull());
    assertFailureKeys(
        e, "Attempt to assert about the cause of a null Throwable", "null Throwable was");
    assertFailureValue(e, "null Throwable was", "throwable.getCause()");
    assertErrorHasActualAsCause(actual, e);
  }

  @Test
  public void hasCauseThat_onNull() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that((Throwable) null).hasCauseThat());
    assertFailureKeys(e, "Attempt to assert about the cause of a null Throwable");
  }

  @Test
  public void hasCauseThat_deepNull_failure() {
    Exception actual =
        new Exception("foobar", new RuntimeException("barfoo", new IOException("buzz")));
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting
                    .that(actual)
                    .hasCauseThat()
                    .hasCauseThat()
                    .hasMessageThat()
                    .isEqualTo("message"));
    assertFailureValue(e, "value of", "throwable.getCause().getCause().getMessage()");
    assertErrorHasActualAsCause(actual, e);
  }

  @Test
  public void inheritedMethodChainsSubject() {
    NullPointerException expected = new NullPointerException("expected");
    NullPointerException actual = new NullPointerException("actual");
    AssertionError e = expectFailure(whenTesting -> whenTesting.that(actual).isEqualTo(expected));
    assertErrorHasActualAsCause(actual, e);
  }

  private static void assertErrorHasActualAsCause(Throwable actual, AssertionError failure) {
    assertWithMessage("AssertionError's cause").that(failure.getCause()).isEqualTo(actual);
  }
}
