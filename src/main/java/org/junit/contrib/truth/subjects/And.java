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
package org.junit.contrib.truth.subjects;

/**
 * A convenience class to allow for chaining in the fluent API
 * style, such that subjects can make propositions in series.  
 * i.e. ASSERT.that(blah).isNotNull().and().contains(b).and().isNotEmpty();
 * 
 * @author Christian Gruber (cgruber@israfil.net)
 *
 * @param <S>
 */
public class And<S> {
  
  private final S subject;
  
  public And(S subject) {
    this.subject = subject;
  }
  
  public S and() {
    return subject;
  }

}
