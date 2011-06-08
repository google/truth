/*
 * Copyright (c) 2011 David Saff
 * Copyright (c) 2011 Christian Gruber
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
package org.junit.contrib.truth;

import java.util.Arrays;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests (and effectively sample code) for the Expect 
 * verb (implemented as a rule)
 * 
 * @author David Saff
 * @author Christian Gruber (cgruber@israfil.net)
 */
@RunWith(JUnit4.class)
public class ExpectTest {
  @Rule public Expect EXPECT = Expect.create();

  @Test public void expectTrue() {
    EXPECT.that(4).isEqualTo(4);
  }

  @Ignore @Test public void expectFail() {
    EXPECT.that("abc").contains("x")
          .and().contains("y")
          .and().contains("z");
    EXPECT.that(Arrays.asList(new String[]{"a", "b", "c"})).containsAnyOf("a", "c");
  }
  
}
