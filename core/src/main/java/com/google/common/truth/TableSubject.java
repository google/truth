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

import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;
import com.google.common.collect.Tables;

import javax.annotation.Nullable;

/**
 * Propositions for {@link Table} subjects.
 *
 * @author Kurt Alfred Kluever
 */
public final class
    TableSubject<S extends TableSubject<S, R, C, V, T>, R, C, V, T extends Table<R, C, V>>
    extends Subject<S, T> {

  private TableSubject(FailureStrategy failureStrategy, @Nullable T table) {
    super(failureStrategy, table);
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  static <R, C, V, T extends Table<R, C, V>>
      TableSubject<? extends TableSubject<?, R, C, V, T>, R, C, V, T> create(
          FailureStrategy failureStrategy, @Nullable Table<R, C, V> table) {
    return new TableSubject(failureStrategy, table);
  }

  /**
   * Fails if the table is not empty.
   */
  public void isEmpty() {
    if (!getSubject().isEmpty()) {
      fail("is empty");
    }
  }

  /**
   * Fails if the table is empty.
   */
  public void isNotEmpty() {
    if (getSubject().isEmpty()) {
      fail("is not empty");
    }
  }

  /**
   * Fails if the table does not have the given size.
   */
  public final void hasSize(int expectedSize) {
    checkArgument(expectedSize >= 0, "expectedSize(%s) must be >= 0", expectedSize);
    int actualSize = getSubject().size();
    if (actualSize != expectedSize) {
      failWithBadResults("has a size of", expectedSize, "is", actualSize);
    }
  }

  /**
   * Fails if the table does not contain a mapping for the given row key and column key.
   */
  public void contains(Object rowKey, Object columnKey) {
    if (!getSubject().contains(rowKey, columnKey)) {
      fail("contains mapping for row/column", rowKey, columnKey);
    }
  }

  /**
   * Fails if the table contains a mapping for the given row key and column key.
   */
  public void doesNotContain(Object rowKey, Object columnKey) {
    if (getSubject().contains(rowKey, columnKey)) {
      fail("does not contain mapping for row/column", rowKey, columnKey);
    }
  }

  /**
   * Fails if the table does not contain the given cell.
   */
  public void containsCell(Object rowKey, Object colKey, Object value) {
    Cell<Object, Object, Object> cell = Tables.immutableCell(rowKey, colKey, value);
    if (!getSubject().cellSet().contains(cell)) {
      fail("contains cell", cell);
    }
  }

  /**
   * Fails if the table contains the given cell.
   */
  public void doesNotContainCell(Object rowKey, Object colKey, Object value) {
    Cell<Object, Object, Object> cell = Tables.immutableCell(rowKey, colKey, value);
    if (getSubject().cellSet().contains(cell)) {
      fail("does not contain cell", cell);
    }
  }

  /**
   * Fails if the table does not contain the given row key.
   */
  public void containsRow(Object rowKey) {
    if (!getSubject().containsRow(rowKey)) {
      fail("contains row", rowKey);
    }
  }

  /**
   * Fails if the table does not contain the given column key.
   */
  public void containsColumn(Object columnKey) {
    if (!getSubject().containsColumn(columnKey)) {
      fail("contains column", columnKey);
    }
  }

  /**
   * Fails if the table does not contain the given value.
   */
  public void containsValue(Object value) {
    if (!getSubject().containsValue(value)) {
      fail("contains value", value);
    }
  }
}
