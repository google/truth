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
import javax.annotation.Nullable;

/** {@link Correspondence} implementations for testing purposes. */
final class TestCorrespondences {
  /**
   * A correspondence between strings and integers which tests whether the string parses as the
   * integer. Parsing is as specified by {@link Integer#decode(String)}. It considers null to
   * correspond to null only.
   */
  static final Correspondence<String, Integer> STRING_PARSES_TO_INTEGER_CORRESPONDENCE =
      new Correspondence<String, Integer>() {

        @Override
        public boolean compare(@Nullable String actual, @Nullable Integer expected) {
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

        @Override
        public String toString() {
          return "parses to";
        }
      };

  /**
   * A correspondence between integers which tests whether they are within 10 of each other. Smart
   * diffing is enabled, with a formatted diff showing the actual value less the expected value.
   * Does not support null values.
   */
  static final Correspondence<Integer, Integer> WITHIN_10_OF =
      new Correspondence<Integer, Integer>() {

        @Override
        public boolean compare(Integer actual, Integer expected) {
          return Math.abs(actual - expected) <= 10;
        }

        @Override
        public String formatDiff(Integer actual, Integer expected) {
          return Integer.toString(actual - expected);
        }

        @Override
        public String toString() {
          return "is within 10 of";
        }
      };

  /**
   * An example value object. It has an optional {@code id} field and a required {@code score}
   * field, both positive integers.
   */
  static final class Record {
    private final int id;
    private final int score;

    static Record create(int id, int score) {
      checkState(id > 0);
      checkState(score > 0);
      return new Record(id, score);
    }

    static Record createWithoutId(int score) {
      checkState(score > 0);
      return new Record(-1, score);
    }

    Record(int id, int score) {
      this.id = id;
      this.score = score;
    }

    boolean hasId() {
      return id > 0;
    }

    int getId() {
      checkState(hasId());
      return id;
    }

    int getScore() {
      return score;
    }

    boolean hasSameId(Record that) {
      return this.id == that.id;
    }

    @Override
    public boolean equals(@Nullable Object o) {
      if (o instanceof Record) {
        Record that = (Record) o;
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
    @Nullable
    static Record parse(String str) {
      List<String> parts = Splitter.on('/').splitToList(str);
      if (parts.size() != 2) {
        return null;
      }
      @Nullable Integer id = parts.get(0).equals("none") ? -1 : Ints.tryParse(parts.get(0));
      @Nullable Integer score = Ints.tryParse(parts.get(1));
      if (id == null || score == null) {
        return null;
      }
      return new Record(id, score);
    }
  }

  /**
   * A correspondence between {@link Record} instances which tests whether their {@code id} values
   * are equal and their {@code score} values are within 10 of each other. Smart diffing is enabled
   * for records with equal {@code id} values, with a formatted diff showing the actual {@code
   * score} value less the expected {@code score} value preceded by the literal {@code score:}. Does
   * not support null records.
   */
  static final Correspondence<Record, Record> RECORDS_EQUAL_WITH_SCORE_TOLERANCE_10 =
      new Correspondence<Record, Record>() {

        @Override
        public boolean compare(Record actual, Record expected) {
          return actual.hasSameId(expected)
              && Math.abs(actual.getScore() - expected.getScore()) <= 10;
        }

        @Override
        public String formatDiff(Record actual, Record expected) {
          if (actual.hasId() && expected.hasId() && actual.getId() == expected.getId()) {
            return "score:" + Integer.toString(actual.getScore() - expected.getScore());
          } else {
            return null;
          }
        }

        @Override
        public String toString() {
          return "has the same id as and a score is within 10 of";
        }
      };

  /**
   * A correspondence like {@link #RECORDS_EQUAL_WITH_SCORE_TOLERANCE_10} except that the actual
   * values are strings which will be parsed before comparing. If the string does not parse to a
   * record then it does not correspond and is not diffed. Does not support null strings or records.
   */
  static final Correspondence<String, Record> PARSED_RECORDS_EQUAL_WITH_SCORE_TOLERANCE_10 =
      new Correspondence<String, Record>() {

        @Override
        public boolean compare(String actual, Record expected) {
          @Nullable Record actualRecord = Record.parse(actual);
          return actualRecord != null
              && RECORDS_EQUAL_WITH_SCORE_TOLERANCE_10.compare(actualRecord, expected);
        }

        @Override
        public String formatDiff(String actual, Record expected) {
          @Nullable Record actualRecord = Record.parse(actual);
          return actualRecord != null
              ? RECORDS_EQUAL_WITH_SCORE_TOLERANCE_10.formatDiff(actualRecord, expected)
              : null;
        }

        @Override
        public String toString() {
          return "parses to a record that " + RECORDS_EQUAL_WITH_SCORE_TOLERANCE_10.toString();
        }
      };

  /**
   * A key function for {@link Record} instances that keys records by their {@code id} values. The
   * key is null if the record has no {@code id}. Does not support null records.
   */
  static final Function<Record, Integer> RECORD_ID =
      new Function<Record, Integer>() {

        @Override
        @Nullable
        public Integer apply(Record record) {
          return record.hasId() ? record.getId() : null;
        }
      };

  /**
   * A key function for {@link String} instances that attempts to parse them as {@link Record}
   * instances and keys records by their {@code id} values. The key is null if the string does not
   * parse or the record has no {@code id}. Does not support null strings.
   */
  static final Function<String, Integer> PARSED_RECORD_ID =
      new Function<String, Integer>() {

        @Override
        @Nullable
        public Integer apply(String str) {
          @Nullable Record record = Record.parse(str);
          return record != null ? RECORD_ID.apply(record) : null;
        }
      };

  private TestCorrespondences() {}
}
