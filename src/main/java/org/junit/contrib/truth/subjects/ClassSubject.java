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


import org.junit.contrib.truth.FailureStrategy;
import org.junit.contrib.truth.util.GwtCompatible;
import org.junit.contrib.truth.util.ReflectionUtil;

@GwtCompatible
public class ClassSubject extends Subject<ClassSubject, Class<?>> {
  public ClassSubject(FailureStrategy failureStrategy, Class<?> o) {
    super(failureStrategy, o);
  }

  public void declaresField(String fieldName) {
    if (getSubject() == null) {
      failureStrategy.fail("Cannot determine a field name from a null class.");
      return; // not all failures throw exceptions.
    }
    try {
      ReflectionUtil.getField(getSubject(), fieldName);
    } catch (NoSuchFieldException e) {
      StringBuilder message = new StringBuilder("Not true that ");
      message.append("<").append(getSubject().getSimpleName()).append(">");
      message.append(" has a field named <").append(fieldName).append(">");
      failureStrategy.fail(message.toString());
    }
  }

}
