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
package org.truth0.subjects;

import static org.truth0.Truth.ASSERT;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.net.CookieStore;
import java.util.Random;

/**
 * Tests for {@code AbstractArraySubject}.
 *
 * @author Christian Gruber (cgruber@israfil.net)
 */
@RunWith(JUnit4.class)
public class AbstractArraySubjectTest {
  @Test public void compressType_JavaLang() {
    ASSERT.that(ObjectArraySubject.compressType(String.class.toString())).isEqualTo("String");
  }

  @Test public void compressType_JavaUtil() {
    ASSERT.that(ObjectArraySubject.compressType(Random.class.toString())).isEqualTo("Random");
  }

  @Test public void compressType_Generic() {
    ASSERT.that(ObjectArraySubject.compressType("java.util.Set<java.lang.Integer>"))
        .isEqualTo("Set<Integer>");
  }

  @Test public void compressType_Uncompressed() {
    ASSERT.that(ObjectArraySubject.compressType(CookieStore.class.toString()))
        .isEqualTo("java.net.CookieStore");
  }

  @Test public void compressType_GenericWithPartialUncompress() {
    ASSERT.that(ObjectArraySubject.compressType("java.util.Set<java.net.CookieStore>"))
        .isEqualTo("Set<java.net.CookieStore>");
  }

  @Test public void compressType_Primitive() {
    ASSERT.that(ObjectArraySubject.compressType(int.class.toString())).isEqualTo("int");
  }
}
