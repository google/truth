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

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.common.collect.Iterables.getOnlyElement;
import static com.google.common.truth.Truth.assertThat;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;

import com.google.common.collect.ImmutableSet;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests that {@code Truth.assertThat} methods match {@code StandardSubjectBuilder.that} methods.
 */
@RunWith(JUnit4.class)
public class TruthAssertThatTest {
  // Type.getTypeName() would obsolete this, but isn't available on the Android version we test on.
  private static String typeString(Type type) {
    return type instanceof Class<?> ? ((Class<?>) type).getName() : type.toString();
  }

  private static String methodSignature(Method input) {
    return "("
        + typeString(getOnlyElement(asList(input.getGenericParameterTypes())))
        + ")"
        + typeString(input.getGenericReturnType());
  }

  @Test
  public void staticAssertThatMethodsMatchStandardSubjectBuilderInstanceMethods() {
    ImmutableSet<String> builderSignatures =
        stream(StandardSubjectBuilder.class.getMethods())
            .filter(method -> method.getName().equals("that"))
            .map(TruthAssertThatTest::methodSignature)
            .collect(toImmutableSet());
    ImmutableSet<String> truthSignatures =
        stream(Truth.class.getMethods())
            .filter(
                method -> method.getName().equals("assertThat") && isStatic(method.getModifiers()))
            .map(TruthAssertThatTest::methodSignature)
            .collect(toImmutableSet());

    assertThat(builderSignatures).isNotEmpty();
    assertThat(truthSignatures).isNotEmpty();
    assertThat(truthSignatures).containsExactlyElementsIn(builderSignatures);
  }
}
