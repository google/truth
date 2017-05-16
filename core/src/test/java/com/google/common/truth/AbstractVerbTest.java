/*
 * Copyright (c) 2011 Google, Inc.
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

import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.CheckReturnValue;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

/**
 * Tests for AbstractVerbs.
 *
 * @author Christian Gruber (cgruber@israfil.net)
 */
@RunWith(Theories.class)
public class AbstractVerbTest {
  private static final AtomicReference<String> failureMessage = new AtomicReference<String>();

  private static final FailureStrategy FAILURE_STRATEGY =
      new AbstractFailureStrategy() {
        @Override
        public void fail(String message, Throwable ignoreInThisTest) {
          failureMessage.set(message);
        }
      };

  @SuppressWarnings("rawtypes")
  private static final AbstractVerb<?> CAPTURE_FAILURE =
      new AbstractVerb(FAILURE_STRATEGY) {
        @Override
        @CheckReturnValue
        public AbstractVerb<?> withFailureMessage(String failureMessage) {
          throw new UnsupportedOperationException();
        }

        @Override
        @CheckReturnValue
        public AbstractVerb<?> withFailureMessage(String failureMessage, Object... args) {
          throw new UnsupportedOperationException();
        }

        @Override
        protected String getFailureMessage() {
          return null;
        }

        @Override
        protected boolean hasFailureMessage() {
          return false;
        }
      };
  @DataPoints public static String[] strings = new String[] {"a", "b"};

  @Test
  public void noArgFail() {
    CAPTURE_FAILURE.fail();
    assertThat(failureMessage.get()).isEqualTo("");
  }

  @Theory
  public void argFail(String message) {
    CAPTURE_FAILURE.fail(message);
    assertThat(failureMessage.get()).isEqualTo(message);
  }
}
