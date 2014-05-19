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
package org.truth0.util;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Extracted routines that need to be swapped in for GWT, to allow for
 * minimal deltas between the GWT and non-GWT version.
 *
 * @author Christian Gruber (cgruber@google.com)
 */
public final class Platform {
  private Platform() {}

  /**
   * Returns true if the instance is assignable to the type Clazz.
   */
  public static boolean isInstanceOfType(Object instance, Class<?> clazz) {
    return isInstanceOfTypeGWT(instance, clazz);
  }

  /**
   * Returns true if the instance is assignable to the type Clazz.
   */
  static boolean isInstanceOfTypeGWT(Object instance, Class<?> clazz) {
    String className = clazz.getName();
    Set<String> types = new LinkedHashSet<String>();
    types.add(instance.getClass().getCanonicalName());
    addTypeNames(instance.getClass(), types);
    for (String type : types) {
      if (type.equals(className)) {
        return true;
      }
    }
    return false;
  }

  private static void addInterfaceNames(Class<?>[] interfaces, Set<String> types) {
    for (Class<?> interfaze : interfaces) {
      types.add(interfaze.getName());
      addInterfaceNames(interfaze.getInterfaces(), types);
    }
  }

  private static void addTypeNames(Class<?> clazz, Set<String> types) {
    for (Class<?> current = clazz ; current != null ; current = current.getSuperclass()) {
      types.add(current.getName());
      addInterfaceNames(current.getInterfaces(), types);
    }
  }

}
