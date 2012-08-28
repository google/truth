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
package org.truth0.util;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Reflection utility methods.
 *
 * @author Christian Gruber (cgruber@israfil.net)
 */
public class ReflectionUtil {

  /** Returns the captured type. */
  public static Class<?> typeParameter(Class<?> clazz, int paramIndex) {
    Type superclass = clazz.getGenericSuperclass();
    if (!(superclass instanceof ParameterizedType)) {
      throw new IllegalArgumentException ("" + superclass + " isn't parameterized");
    }
    Type[] typeParams = ((ParameterizedType) superclass).getActualTypeArguments();
    return (Class<?>)typeParams[paramIndex];
  }

  public static Field getField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
    Class<?> currentClass = clazz;
    while (currentClass != null) {
      try {
        return clazz.getDeclaredField(fieldName);
      } catch (NoSuchFieldException e) {
        currentClass = currentClass.getSuperclass();
      }
    }
    throw new NoSuchFieldException("No such field " + fieldName + " declared on " +
        clazz.getSimpleName() + " or its parent classes.");
  }
}
