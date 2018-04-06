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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.truth.Fact.factWithoutValue;

import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;
import com.google.common.collect.Tables;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

/**
 * Propositions for {@link Table} subjects.
 *
 * @author Kurt Alfred Kluever
 */
public final class TableSubject extends Subject<TableSubject, Table<?, ?, ?>> {
  TableSubject(FailureMetadata metadata, @NullableDecl Table<?, ?, ?> table) {
    super(metadata, table);
  }

  /** Fails if the table is not empty. */
  public void isEmpty() {
    if (!actual().isEmpty()) {
      fail(factWithoutValue("expected to be empty"));
    }
  }

  /** Fails if the table is empty. */
  public void isNotEmpty() {
    if (actual().isEmpty()) {
      failWithoutActual(factWithoutValue("expected not to be empty"));
    }
  }

  /** Fails if the table does not have the given size. */
  public final void hasSize(int expectedSize) {
    checkArgument(expectedSize >= 0, "expectedSize(%s) must be >= 0", expectedSize);
    check("size()").that(actual().size()).isEqualTo(expectedSize);
  }

  /** Fails if the table does not contain a mapping for the given row key and column key. */
  public void contains(@NullableDecl Object rowKey, @NullableDecl Object columnKey) {
    if (!actual().contains(rowKey, columnKey)) {
      fail("contains mapping for row/column", rowKey, columnKey);
    }
  }

  /** Fails if the table contains a mapping for the given row key and column key. */
  public void doesNotContain(@NullableDecl Object rowKey, @NullableDecl Object columnKey) {
    if (actual().contains(rowKey, columnKey)) {
      fail("does not contain mapping for row/column", rowKey, columnKey);
    }
  }

  /** Fails if the table does not contain the given cell. */
  public void containsCell(
      @NullableDecl Object rowKey, @NullableDecl Object colKey, @NullableDecl Object value) {
    containsCell(Tables.<Object, Object, Object>immutableCell(rowKey, colKey, value));
  }

  /** Fails if the table does not contain the given cell. */
  public void containsCell(Cell<?, ?, ?> cell) {
    checkNotNull(cell);
    if (!actual().cellSet().contains(cell)) {
      fail("contains cell", cell);
    }
  }

  /** Fails if the table contains the given cell. */
  public void doesNotContainCell(
      @NullableDecl Object rowKey, @NullableDecl Object colKey, @NullableDecl Object value) {
    doesNotContainCell(Tables.<Object, Object, Object>immutableCell(rowKey, colKey, value));
  }

  /** Fails if the table contains the given cell. */
  public void doesNotContainCell(Cell<?, ?, ?> cell) {
    checkNotNull(cell);
    if (actual().cellSet().contains(cell)) {
      fail("does not contain cell", cell);
    }
  }

  /** Fails if the table does not contain the given row key. */
  public void containsRow(@NullableDecl Object rowKey) {
    if (!actual().containsRow(rowKey)) {
      fail("contains row", rowKey);
    }
  }

  /** Fails if the table does not contain the given column key. */
  public void containsColumn(@NullableDecl Object columnKey) {
    if (!actual().containsColumn(columnKey)) {
      fail("contains column", columnKey);
    }
  }

  /** Fails if the table does not contain the given value. */
  public void containsValue(@NullableDecl Object value) {
    if (!actual().containsValue(value)) {
      fail("contains value", value);
    }
  }
}
