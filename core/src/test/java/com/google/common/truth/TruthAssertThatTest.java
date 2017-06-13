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
import static com.google.common.truth.Truth.assert_;
import static java.util.Arrays.asList;
import static org.junit.Assert.fail;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.google.common.reflect.TypeToken;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for the FEST-alike assertThat() entry point.
 *
 * @author Christian Gruber (cgruber@israfil.net)
 */
@RunWith(JUnit4.class)
public class TruthAssertThatTest {
  private static final Function<Method, TypeToken<?>> METHOD_TO_RETURN_TYPE_TOKEN =
      new Function<Method, TypeToken<?>>() {
        @Override
        public TypeToken<?> apply(Method input) {
          return TypeToken.of(Iterables.getOnlyElement(asList(input.getParameterTypes())));
        }
      };

  @Test
  public void staticAssertThatMethodsMatchTestVerbInstanceMethods() {
    ImmutableSortedSet<TypeToken<?>> verbTypes =
        FluentIterable.from(asList(TestVerb.class.getMethods()))
            .filter(
                new Predicate<Method>() {
                  @Override
                  public boolean apply(Method input) {
                    return input.getName().equals("that");
                  }
                })
            .transform(METHOD_TO_RETURN_TYPE_TOKEN)
            .toSortedSet(Ordering.usingToString());
    ImmutableSortedSet<TypeToken<?>> truthTypes =
        FluentIterable.from(asList(Truth.class.getMethods()))
            .filter(
                new Predicate<Method>() {
                  @Override
                  public boolean apply(Method input) {
                    return input.getName().equals("assertThat")
                        && Modifier.isStatic(input.getModifiers());
                  }
                })
            .transform(METHOD_TO_RETURN_TYPE_TOKEN)
            .toSortedSet(Ordering.usingToString());

    assert_().that(verbTypes).isNotEmpty();
    assert_().that(truthTypes).isNotEmpty();
    assert_().that(truthTypes).containsExactlyElementsIn(verbTypes);
  }

  @Test
  public void festAlike() {
    assertThat("foo").contains("fo");
    assertThat(false).isFalse();
  }

  @Test
  public void testTruthFramesAreStrippedFromStackTrace() throws Exception {
    try {
      Truth.THROW_ASSERTION_ERROR.fail("test");
    } catch (AssertionError expected) {
      for (StackTraceElement stackTraceElement : expected.getStackTrace()) {
        assertThat(stackTraceElement.getClassName()).doesNotContain("com.google.common.truth");
      }
      return;
    }
    fail("Expected THROW_ASSERTION_ERROR.fail() to throw...");
  }
}
