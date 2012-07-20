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
package org.junit.contrib.truth.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.junit.contrib.truth.subjects.Subject;

/**
 * Reflection utility methods.
 *
 * @author Christian Gruber (cgruber@israfil.net)
 */
public class ReflectionUtil {

  /** Returns the captured type. */
  @SuppressWarnings("unchecked")
  public static <S extends Subject<S,T>, T> Class<S> capture(Class<?> clazz, int paramIndex) {
    Type superclass = clazz.getGenericSuperclass();
    if (!(superclass instanceof ParameterizedType)) {
      throw new IllegalArgumentException ("" + superclass + " isn't parameterized");
    }
    // we want the type of the Subject, so the 0th element of the type arguments.
    Type[] typeParams = ((ParameterizedType) superclass).getActualTypeArguments();
    return (Class<S>)typeParams[paramIndex];
  }



}

