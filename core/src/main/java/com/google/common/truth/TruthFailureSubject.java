/*
 * Copyright (c) 2018 Google, Inc.
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

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.truth.Field.field;

import com.google.common.collect.ImmutableList;
import javax.annotation.Nullable;

// TODO(cpovirk): Expose this publicly once we finalize names.

/**
 * Subject for {@link AssertionError} objects thrown by Truth. {@code TruthFailureSubject} contains
 * methods for asserting about the individual "fields" of those failures. This allows tests to avoid
 * asserting about the same field more often than necessary, including avoiding asserting about
 * fields that are set by other subjects that the main subject delegates to. This keeps tests
 * shorter and less fragile.
 *
 * <p>To create an instance, call {@link ExpectFailure#assertThat}.
 *
 * <p>This class accepts any {@code AssertionError} value, but it will throw an exception if a
 * caller tries to access the fields of an error that wasn't produced by Truth.
 */
final class TruthFailureSubject extends ThrowableSubject {
  /*
   * TODO(cpovirk): Expose this publicly once it can have the right type. That can't happen until we
   * add type parameters to ThrowableSubject, make TruthFailureSubject not extend ThrowableSubject,
   * remove the type parameters from Subject altogether, or *maybe* just loosen the type parameters
   * on Factory. At that point, also mention it in the class-level docs.
   */

  /**
   * Package-private factory for creating {@link TruthFailureSubject} instances. At the moment, the
   * only public way to create an instance is {@link ExpectFailure#assertThat}.
   */
  static Factory<ThrowableSubject, Throwable> truthFailures() {
    return FACTORY;
  }

  private static final Factory<ThrowableSubject, Throwable> FACTORY =
      new Factory<ThrowableSubject, Throwable>() {
        @Override
        public ThrowableSubject createSubject(FailureMetadata metadata, Throwable actual) {
          return new TruthFailureSubject(metadata, actual, "failure");
        }
      };

  TruthFailureSubject(
      FailureMetadata metadata, @Nullable Throwable throwable, @Nullable String typeDescription) {
    super(metadata, throwable, typeDescription);
  }

  /** Returns a subject for the list of field keys. */
  public IterableSubject fieldKeys() {
    if (!(actual() instanceof ErrorWithFields)) {
      failWithRawMessage("expected a failure thrown by Truth's new failure API");
      return ignoreCheck().that(ImmutableList.of());
    }
    ErrorWithFields error = (ErrorWithFields) actual();
    return check("fieldKeys()").that(getFieldKeys(error));
  }

  private static ImmutableList<String> getFieldKeys(ErrorWithFields error) {
    ImmutableList.Builder<String> fields = ImmutableList.builder();
    for (Field field : error.fields()) {
      fields.add(field.key);
    }
    return fields.build();
  }

  /**
   * Returns a subject for the value with the given name.
   *
   * <p>The value, if present, is always a string, the {@code String.valueOf} representation of the
   * value passed to {@link Field#field}.
   *
   * <p>The value is null in the case of {@linkplain Field#fieldWithoutValue fields that have no
   * value}. By contrast, fields that have a value that is rendered as "null" (such as those created
   * with {@code field("key", null)}) are considered to have a value, the string "null."
   *
   * <p>If the failure under test contains more than one field with the given key, this method will
   * fail the test. To assert about such a failure, use {@linkplain #fieldValue(String, int) the
   * other overload} of {@code fieldValue}.
   */
  public StringSubject fieldValue(String key) {
    return doFieldValue(key, null);
  }

  /**
   * Returns a subject for the value of the {@code index}-th instance of the field with the given
   * name. Most Truth failures do not contain multiple fields with the same key, so most tests
   * should use {@linkplain #fieldValue(String) the other overload} of {@code fieldValue}.
   */
  public StringSubject fieldValue(String key, int index) {
    checkArgument(index >= 0, "index must be nonnegative: %s", index);
    return doFieldValue(key, index);
  }

  private StringSubject doFieldValue(String key, @Nullable Integer index) {
    checkNotNull(key);
    if (!(actual() instanceof ErrorWithFields)) {
      failWithRawMessage("expected a failure thrown by Truth's new failure API");
      return ignoreCheck().that("");
    }
    ErrorWithFields error = (ErrorWithFields) actual();

    /*
     * We don't care as much about including the actual Throwable and its fields in these because
     * the Throwable will be attached as a cause in nearly all cases.
     */
    ImmutableList<Field> fieldsWithName = fieldsWithName(error, key);
    if (fieldsWithName.isEmpty()) {
      failWithRawMessage(
          field("expected to contain field", key)
              + "\n"
              + field("but contained only", getFieldKeys(error)));
      return ignoreCheck().that("");
    }
    if (index == null && fieldsWithName.size() > 1) {
      failWithRawMessage(
          field("expected to contain a single field with key", key)
              + "\n"
              + field("but contained multiple", fieldsWithName));
      return ignoreCheck().that("");
    }
    if (index != null && index > fieldsWithName.size()) {
      failWithRawMessage(
          field("for key", key)
              + "\n"
              + field("index too high", index)
              + "\n"
              + field("field count was", fieldsWithName.size()));
      return ignoreCheck().that("");
    }
    StandardSubjectBuilder check =
        index == null ? check("fieldValue(%s)", key) : check("fieldValue(%s, %s)", key, index);
    return check.that(fieldsWithName.get(firstNonNull(index, 0)).value);
  }

  private static ImmutableList<Field> fieldsWithName(ErrorWithFields error, String key) {
    ImmutableList.Builder<Field> fields = ImmutableList.builder();
    for (Field field : error.fields()) {
      if (field.key.equals(key)) {
        fields.add(field);
      }
    }
    return fields.build();
  }
}
