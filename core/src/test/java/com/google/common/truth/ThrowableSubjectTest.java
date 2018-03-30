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

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link Throwable} subjects.
 *
 * @author Kurt Alfred Kluever
 */
@RunWith(JUnit4.class)
public class ThrowableSubjectTest extends BaseSubjectTestCase {

  @SuppressWarnings("deprecation")
  @Test
  public void hasMessage() {
    NullPointerException npe = new NullPointerException("message");
    assertThat(npe).hasMessage("message");
  }

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
    expectFailureWhenTestingThat(actual).hasMessageThat().isEqualTo("foobar");
    assertFailureValue("value of", "throwable.getMessage()");
    assertErrorHasActualAsCause(actual, expectFailure.getFailure());
  }

  @Test
  public void hasMessageThat_MessageHasNullMessage_failure() {
    expectFailureWhenTestingThat(new NullPointerException("message")).hasMessageThat().isNull();
  }

  @Test
  public void hasMessageThat_Named_failure() {
    NullPointerException npe = new NullPointerException("message");
    expectFailureWhenTestingThat(npe).named("NPE").hasMessageThat().isEqualTo("foobar");
    assertFailureValue("value of", "NPE.getMessage()");
  }

  @Test
  public void hasMessageThat_NullMessageHasMessage_failure() {
    NullPointerException npe = new NullPointerException(null);
    expectFailureWhenTestingThat(npe).hasMessageThat().isEqualTo("message");
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
    expectFailureWhenTestingThat(actual).hasCauseThat().hasMessageThat().isEqualTo("message");
    assertFailureValue("value of", "throwable.getCause().getMessage()");
    assertErrorHasActualAsCause(actual, expectFailure.getFailure());
  }

  @Test
  public void hasCauseThat_instanceOf_failure() {
    Exception actual = new Exception("foobar", new IOException("barfoo"));
    expectFailureWhenTestingThat(actual).hasCauseThat().isInstanceOf(RuntimeException.class);
    assertFailureValue("value of", "throwable.getCause()");
    assertErrorHasActualAsCause(actual, expectFailure.getFailure());
  }

  @Test
  public void hasCauseThat_tooDeep_failure() {
    Exception actual = new Exception("foobar");
    expectFailureWhenTestingThat(actual).hasCauseThat().hasCauseThat().isNull();
    assertThat(expectFailure.getFailure().getMessage())
        .isEqualTo(
            "value of: throwable.getCause().getCause()\n"
                + "Causal chain is not deep enough - add a .isNotNull() check?");
    assertErrorHasActualAsCause(actual, expectFailure.getFailure());
  }

  @Test
  public void hasCauseThat_deepNull_failure() {
    Exception actual =
        new Exception("foobar", new RuntimeException("barfoo", new IOException("buzz")));
    expectFailureWhenTestingThat(actual)
        .hasCauseThat()
        .hasCauseThat()
        .hasMessageThat()
        .isEqualTo("message");
    assertFailureValue("value of", "throwable.getCause().getCause().getMessage()");
    assertErrorHasActualAsCause(actual, expectFailure.getFailure());
  }

  @Test
  public void inheritedMethodChainsSubject() {
    NullPointerException expected = new NullPointerException("expected");
    NullPointerException actual = new NullPointerException("actual");
    expectFailureWhenTestingThat(actual).isEqualTo(expected);
    assertErrorHasActualAsCause(actual, expectFailure.getFailure());
  }

  private static void assertErrorHasActualAsCause(Throwable actual, AssertionError failure) {
    assertThat(failure.getCause()).named("AssertionError's cause").isEqualTo(actual);
  }

  private ThrowableSubject expectFailureWhenTestingThat(Throwable actual) {
    return expectFailure.whenTesting().that(actual);
  }
}
