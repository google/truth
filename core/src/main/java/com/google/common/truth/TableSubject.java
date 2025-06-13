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
package com.google.common.truth;

import static com.google.common.truth.Fact.fact;
import static com.google.common.truth.Fact.simpleFact;

import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;
import com.google.common.collect.Tables;
import org.jspecify.annotations.Nullable;

/**
 * A subject for {@link Table} values.
 *
 * @author Kurt Alfred Kluever
 */
public final class TableSubject extends Subject {
  private final @Nullable Table<?, ?, ?> actual;

  private TableSubject(FailureMetadata metadata, @Nullable Table<?, ?, ?> actual) {
    super(metadata, actual);
    this.actual = actual;
  }

  /** Checks that the actual table is empty. */
  public void isEmpty() {
    if (actual == null) {
      failWithActual(simpleFact("expected an empty table"));
    } else if (!actual.isEmpty()) {
      failWithActual(simpleFact("expected to be empty"));
    }
  }

  /** Checks that the actual table is not empty. */
  public void isNotEmpty() {
    if (actual == null) {
      failWithActual(simpleFact("expected a nonempty table"));
    } else if (actual.isEmpty()) {
      failWithoutActual(simpleFact("expected not to be empty"));
    }
  }

  /** Checks that the actual table has the given size. */
  public void hasSize(int expectedSize) {
    if (actual == null) {
      failWithActual(fact("expected a table with size", expectedSize));
    } else if (expectedSize < 0) {
      failWithoutActual(
          simpleFact("expected a table with a negative size, but that is impossible"),
          fact("expected size", expectedSize),
          fact("actual size", actual.size()),
          fact("table was", actualCustomStringRepresentationForPackageMembersToCall()));
    } else {
      check("size()").that(actual.size()).isEqualTo(expectedSize);
    }
  }

  /** Checks that the actual table contains a mapping for the given row key and column key. */
  public void contains(@Nullable Object rowKey, @Nullable Object columnKey) {
    if (actual == null || !actual.contains(rowKey, columnKey)) {
      /*
       * TODO(cpovirk): Consider including information about whether any cell with the given row
       * *or* column was present.
       */
      failWithActual(
          simpleFact(
              actual == null
                  ? "expected a table that contains a mapping for row-column key pair"
                  : "expected to contain mapping for row-column key pair"),
          fact("row key", rowKey),
          fact("column key", columnKey));
    }
  }

  /**
   * Checks that the actual table does not contain a mapping for the given row key and column key.
   */
  public void doesNotContain(@Nullable Object rowKey, @Nullable Object columnKey) {
    if (actual == null) {
      failWithActual(
          simpleFact("expected a table that does not contain a mapping for row-column key pair"),
          fact("row key", rowKey),
          fact("column key", columnKey));
    } else if (actual.contains(rowKey, columnKey)) {
      failWithoutActual(
          simpleFact("expected not to contain mapping for row-column key pair"),
          fact("row key", rowKey),
          fact("column key", columnKey),
          fact("but contained value", actual.get(rowKey, columnKey)),
          fact("full contents", actual));
    }
  }

  /** Checks that the actual table contains the given cell. */
  public void containsCell(
      @Nullable Object rowKey, @Nullable Object colKey, @Nullable Object value) {
    containsCell(
        Tables.<@Nullable Object, @Nullable Object, @Nullable Object>immutableCell(
            rowKey, colKey, value));
  }

  /** Checks that the actual table contains the given cell. */
  public void containsCell(@Nullable Cell<?, ?, ?> cell) {
    if (cell == null) {
      failWithoutActual(
          simpleFact("expected to contain a null cell, but that is impossible"),
          fact("table was", actualCustomStringRepresentationForPackageMembersToCall()));
    } else if (actual == null) {
      failWithActual(
          simpleFact("expected a table that contains the given cell"),
          fact("row key", cell.getRowKey()),
          fact("column key", cell.getColumnKey()),
          fact("value", cell.getValue()));
    } else if (actual.contains(cell.getRowKey(), cell.getColumnKey())) {
      // TODO(cpovirk): For a cell that exists but has a null value, consider a clarification like:
      // "key is present but with a different value" (from MapSubject.containsEntry)
      check("get(%s, %s)", cell.getRowKey(), cell.getColumnKey())
          .that(actual.get(cell.getRowKey(), cell.getColumnKey()))
          .isEqualTo(cell.getValue());
    } else {
      checkNoNeedToDisplayBothValues("cellSet()").that(actual.cellSet()).contains(cell);
    }
  }

  /** Checks that the actual table does not contain the given cell. */
  public void doesNotContainCell(
      @Nullable Object rowKey, @Nullable Object colKey, @Nullable Object value) {
    doesNotContainCell(
        Tables.<@Nullable Object, @Nullable Object, @Nullable Object>immutableCell(
            rowKey, colKey, value));
  }

  /** Checks that the actual table does not contain the given cell. */
  public void doesNotContainCell(@Nullable Cell<?, ?, ?> cell) {
    if (cell == null) {
      failWithoutActual(
          simpleFact("refusing to check for a null cell because tables never contain null cells"),
          fact("table was", actualCustomStringRepresentationForPackageMembersToCall()));
    } else if (actual == null) {
      failWithActual(
          simpleFact("expected a table that does not contain the given cell"),
          fact("row key", cell.getRowKey()),
          fact("column key", cell.getColumnKey()),
          fact("value", cell.getValue()));
    } else {
      checkNoNeedToDisplayBothValues("cellSet()").that(actual.cellSet()).doesNotContain(cell);
    }
  }

  /** Checks that the actual table contains the given row key. */
  public void containsRow(@Nullable Object rowKey) {
    if (actual == null) {
      failWithActual(fact("expected a table with row", rowKey));
    } else {
      check("rowKeySet()").that(actual.rowKeySet()).contains(rowKey);
    }
  }

  /** Checks that the actual table contains the given column key. */
  public void containsColumn(@Nullable Object columnKey) {
    if (actual == null) {
      failWithActual(fact("expected a table with column", columnKey));
    } else {
      check("columnKeySet()").that(actual.columnKeySet()).contains(columnKey);
    }
  }

  /** Checks that the actual table contains the given value. */
  public void containsValue(@Nullable Object value) {
    if (actual == null) {
      failWithActual(fact("expected a table with value", value));
    } else {
      check("values()").that(actual.values()).contains(value);
    }
  }

  static Factory<TableSubject, Table<?, ?, ?>> tables() {
    return TableSubject::new;
  }
}
