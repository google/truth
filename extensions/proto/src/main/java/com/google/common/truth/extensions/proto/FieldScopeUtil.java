/*
 * Copyright (c) 2016 Google, Inc.
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
package com.google.common.truth.extensions.proto;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;
import java.util.List;

/** Utility methods for {@link FieldScope}s and {@link FluentEqualityConfig}. */
final class FieldScopeUtil {
  /**
   * Returns a function which translates integer field numbers into field names using the Descriptor
   * if available.
   *
   * @param fmt Format string that must contain exactly one '%s' and no other format parameters.
   */
  static Function<Optional<Descriptor>, String> fieldNumbersFunction(
      final String fmt, final Iterable<Integer> fieldNumbers) {
    return new Function<Optional<Descriptor>, String>() {
      @Override
      public String apply(Optional<Descriptor> optDescriptor) {
        return resolveFieldNumbers(optDescriptor, fmt, fieldNumbers);
      }
    };
  }

  /**
   * Returns a function which formats the given string by getting the usingCorrespondenceString from
   * the given FieldScope with the argument descriptor.
   *
   * @param fmt Format string that must contain exactly one '%s' and no other format parameters.
   */
  static Function<Optional<Descriptor>, String> fieldScopeFunction(
      final String fmt, final FieldScope fieldScope) {
    return new Function<Optional<Descriptor>, String>() {
      @Override
      public String apply(Optional<Descriptor> optDescriptor) {
        return String.format(fmt, fieldScope.usingCorrespondenceString(optDescriptor));
      }
    };
  }

  /** Returns a function which concatenates the outputs of the two input functions. */
  static Function<Optional<Descriptor>, String> concat(
      final Function<? super Optional<Descriptor>, String> function1,
      final Function<? super Optional<Descriptor>, String> function2) {
    return new Function<Optional<Descriptor>, String>() {
      @Override
      public String apply(Optional<Descriptor> optDescriptor) {
        return function1.apply(optDescriptor) + function2.apply(optDescriptor);
      }
    };
  }

  /**
   * Returns the singular descriptor used by all non-null messages in the list.
   *
   * <p>If there is no descriptor, or more than one, returns {@code Optional.absent()}.
   */
  static Optional<Descriptor> getSingleDescriptor(Iterable<? extends Message> messages) {
    Optional<Descriptor> optDescriptor = Optional.absent();
    for (Message message : messages) {
      if (message != null) {
        Descriptor descriptor = message.getDescriptorForType();
        if (!optDescriptor.isPresent()) {
          optDescriptor = Optional.of(descriptor);
        } else if (descriptor != optDescriptor.get()) {
          // Two different descriptors - abandon ship.
          return Optional.absent();
        }
      }
    }
    return optDescriptor;
  }

  /** Joins the arguments into a {@link List} for convenience. */
  static List<Integer> asList(int first, int... rest) {
    List<Integer> list = Lists.newArrayList();
    list.add(first);
    list.addAll(Ints.asList(rest));
    return list;
  }

  private static final Joiner JOINER = Joiner.on(", ");

  static String join(Iterable<?> objects) {
    return JOINER.join(objects);
  }

  /**
   * Formats {@code fmt} with the field numbers, concatenated, if a descriptor is available to
   * resolve them to field names. Otherwise it uses the raw integers.
   *
   * @param fmt Format string that must contain exactly one '%s' and no other format parameters.
   */
  private static String resolveFieldNumbers(
      Optional<Descriptor> optDescriptor, String fmt, Iterable<Integer> fieldNumbers) {
    if (optDescriptor.isPresent()) {
      Descriptor descriptor = optDescriptor.get();
      List<String> strings = Lists.newArrayList();
      for (int fieldNumber : fieldNumbers) {
        FieldDescriptor field = descriptor.findFieldByNumber(fieldNumber);
        strings.add(field != null ? field.toString() : String.format("%d (?)", fieldNumber));
      }
      return String.format(fmt, join(strings));
    } else {
      return String.format(fmt, join(fieldNumbers));
    }
  }

  private FieldScopeUtil() {}
}
