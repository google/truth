/*
 * Copyright (c) 2011 Google, Inc.
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

import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.primitives.Ints;
import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;

/** {@link Correspondence} implementations for testing purposes. */
final class TestCorrespondences {
  /**
   * A correspondence between strings and integers which tests whether the string parses as the
   * integer. Parsing is as specified by {@link Integer#decode(String)}. It considers null to
   * correspond to null only.
   */
  static final Correspondence<String, Integer> STRING_PARSES_TO_INTEGER_CORRESPONDENCE =
      Correspondence.from(TestCorrespondences::stringParsesToInteger, "parses to");

  private static boolean stringParsesToInteger(
      @Nullable String actual, @Nullable Integer expected) {
    if (actual == null) {
      return expected == null;
    }
    try {
      // Older versions of Android reject leading plus signs, per the pre-Java-7 contract:
      // https://docs.oracle.com/javase/6/docs/api/java/lang/Integer.html#decode(java.lang.String)
      // https://docs.oracle.com/javase/7/docs/api/java/lang/Integer.html#decode(java.lang.String)
      if (actual.startsWith("+")) {
        actual = actual.substring(1);
      }
      return Integer.decode(actual).equals(expected);
    } catch (NumberFormatException e) {
      return false;
    }
  }

  /** A formatter for the diffs between integers. */
  static final Correspondence.DiffFormatter<Integer, Integer> INT_DIFF_FORMATTER =
      (a, e) -> Integer.toString(a - e);

  /**
   * A correspondence between integers which tests whether they are within 10 of each other. Smart
   * diffing is enabled, with a formatted diff showing the actual value less the expected value.
   * Does not support null values.
   */
  static final Correspondence<Integer, Integer> WITHIN_10_OF =
      Correspondence.from(
              (Integer actual, Integer expected) -> {
                if (actual == null || expected == null) {
                  throw new NullPointerExceptionFromWithin10Of();
                }
                return Math.abs(actual - expected) <= 10;
              },
              "is within 10 of")
          .formattingDiffsUsing(INT_DIFF_FORMATTER);

  private static final class NullPointerExceptionFromWithin10Of extends NullPointerException {}

  /**
   * A correspondence between strings which tests for case-insensitive equality. Supports null
   * expected elements, but throws {@link NullPointerException} on null actual elements.
   */
  static final Correspondence<String, String> CASE_INSENSITIVE_EQUALITY =
      Correspondence.from(String::equalsIgnoreCase, "equals (ignoring case)");

  /**
   * A correspondence between strings which tests for case-insensitive equality, with a broken
   * attempt at null-safety. The {@link Correspondence#compare} implementation returns true for
   * (null, null) and false for (non-null, null), but throws {@link NullPointerException} for (null,
   * non-null).
   */
  static final Correspondence<String, String> CASE_INSENSITIVE_EQUALITY_HALF_NULL_SAFE =
      Correspondence.from(
          TestCorrespondences::equalsIgnoreCaseHalfNullSafe, "equals (ignoring case)");

  /*
   * This is just an example for a test, and it's a convenient way to demonstrate the specific null
   * behavior documented below.
   */
  @SuppressWarnings("Casing_StringEqualsIgnoreCase")
  private static boolean equalsIgnoreCaseHalfNullSafe(String actual, String expected) {
    if (actual == null && expected == null) {
      return true;
    }
    // Oops! We don't handle the case where actual == null but expected != null.
    return actual.equalsIgnoreCase(expected);
  }

  /**
   * An example value object. It has an optional {@code id} field and a required {@code score}
   * field, both positive integers.
   */
  static final class MyRecord {
    private final int id;
    private final int score;

    static MyRecord create(int id, int score) {
      checkState(id >= 0);
      checkState(score > 0);
      return new MyRecord(id, score);
    }

    static MyRecord createWithoutId(int score) {
      checkState(score >= 0);
      return new MyRecord(-1, score);
    }

    MyRecord(int id, int score) {
      this.id = id;
      this.score = score;
    }

    boolean hasId() {
      return id >= 0;
    }

    int getId() {
      checkState(hasId());
      return id;
    }

    int getScore() {
      return score;
    }

    boolean hasSameId(MyRecord that) {
      return this.id == that.id;
    }

    @Override
    public boolean equals(@Nullable Object o) {
      if (o instanceof MyRecord) {
        MyRecord that = (MyRecord) o;
        return this.id == that.id && this.score == that.score;
      }
      return false;
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(id, score);
    }

    /**
     * Returns the string form of the record, which is the {@code id} value or the literal {@code
     * none} if none, the literal {@code /}, and the {@code score} value concatenated.
     */
    @Override
    public String toString() {
      return Joiner.on('/').join(hasId() ? getId() : "none", getScore());
    }

    /**
     * If the argument is the string form of a record, returns that record; otherwise returns {@code
     * null}.
     */
    static @Nullable MyRecord parse(String str) {
      List<String> parts = Splitter.on('/').splitToList(str);
      if (parts.size() != 2) {
        return null;
      }
      Integer id = parts.get(0).equals("none") ? -1 : Ints.tryParse(parts.get(0));
      Integer score = Ints.tryParse(parts.get(1));
      if (id == null || score == null) {
        return null;
      }
      return new MyRecord(id, score);
    }
  }

  /**
   * A correspondence between {@link MyRecord} instances which tests whether their {@code id} values
   * are equal and their {@code score} values are within 10 of each other. Smart diffing is not
   * supported.
   *
   * <p>The {@link Correspondence#compare} implementation support nulls, such that null corresponds
   * to null only. The {@link Correspondence#formatDiff} implementation does not support nulls.
   */
  static final Correspondence<MyRecord, MyRecord> RECORDS_EQUAL_WITH_SCORE_TOLERANCE_10_NO_DIFF =
      Correspondence.from(
          TestCorrespondences::recordsAreCloseEnough,
          "has the same id as and a score within 10 of");

  /**
   * A formatter for diffs between records. If the records have the same key, it gives a string of
   * the form {@code "score:<score_diff>"}. If they have different keys, it gives null.
   */
  static final Correspondence.DiffFormatter<MyRecord, MyRecord> RECORD_DIFF_FORMATTER =
      TestCorrespondences::formatRecordDiff;

  /**
   * A correspondence between {@link MyRecord} instances which tests whether their {@code id} values
   * are equal and their {@code score} values are within 10 of each other. Smart diffing is enabled
   * for records with equal {@code id} values, with a formatted diff showing the actual {@code
   * score} value less the expected {@code score} value preceded by the literal {@code score:}.
   *
   * <p>The {@link Correspondence#compare} implementation support nulls, such that null corresponds
   * to null only. The {@link Correspondence#formatDiff} implementation does not support nulls.
   */
  static final Correspondence<MyRecord, MyRecord> RECORDS_EQUAL_WITH_SCORE_TOLERANCE_10 =
      RECORDS_EQUAL_WITH_SCORE_TOLERANCE_10_NO_DIFF.formattingDiffsUsing(RECORD_DIFF_FORMATTER);

  /**
   * A correspondence like {@link #RECORDS_EQUAL_WITH_SCORE_TOLERANCE_10} except that the actual
   * values are strings which will be parsed before comparing. If the string does not parse to a
   * record then it does not correspond and is not diffed. Does not support null strings or records.
   */
  static final Correspondence<String, MyRecord> PARSED_RECORDS_EQUAL_WITH_SCORE_TOLERANCE_10 =
      Correspondence.from(
              (String a, MyRecord e) -> {
                MyRecord actualRecord = MyRecord.parse(a);
                return actualRecord != null && recordsAreCloseEnough(actualRecord, e);
              },
              "parses to a record that " + RECORDS_EQUAL_WITH_SCORE_TOLERANCE_10)
          .formattingDiffsUsing(
              (a, e) -> {
                MyRecord actualRecord = MyRecord.parse(a);
                return actualRecord != null ? formatRecordDiff(actualRecord, e) : null;
              });

  private static boolean recordsAreCloseEnough(
      @Nullable MyRecord actual, @Nullable MyRecord expected) {
    if (actual == null) {
      return expected == null;
    }
    if (expected == null) {
      return false;
    }
    return actual.hasSameId(expected) && Math.abs(actual.getScore() - expected.getScore()) <= 10;
  }

  private static @Nullable String formatRecordDiff(MyRecord actual, MyRecord expected) {
    if (actual.hasId() && expected.hasId() && actual.getId() == expected.getId()) {
      return "score:" + (actual.getScore() - expected.getScore());
    } else {
      return null;
    }
  }

  /**
   * A key function for {@link MyRecord} instances that keys records by their {@code id} values. The
   * key is null if the record has no {@code id}. Does not support null records.
   */
  static final Function<MyRecord, Integer> RECORD_ID =
      record -> record.hasId() ? record.getId() : null;

  /**
   * A key function for {@link MyRecord} instances that keys records by their {@code id} values. The
   * key is null if the record has no {@code id}. Does not support null records.
   */
  static final Function<MyRecord, Integer> NULL_SAFE_RECORD_ID =
      record -> {
        if (record == null) {
          return 0;
        }
        return record.hasId() ? record.getId() : null;
      };

  /**
   * A key function for {@link String} instances that attempts to parse them as {@link MyRecord}
   * instances and keys records by their {@code id} values. The key is null if the string does not
   * parse or the record has no {@code id}. Does not support null strings.
   */
  static final Function<String, Integer> PARSED_RECORD_ID =
      str -> {
        MyRecord record = MyRecord.parse(str);
        return record != null ? RECORD_ID.apply(record) : null;
      };

  private TestCorrespondences() {}
}
