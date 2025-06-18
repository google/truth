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

import static com.google.common.collect.Tables.immutableCell;
import static com.google.common.truth.ExpectFailure.assertThat;
import static com.google.common.truth.ExpectFailure.expectFailure;
import static com.google.common.truth.FailureAssertions.assertFailureKeys;
import static com.google.common.truth.FailureAssertions.assertFailureValue;
import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;
import org.jspecify.annotations.Nullable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for Table Subjects.
 *
 * @author Kurt Alfred Kluever
 */
@RunWith(JUnit4.class)
public class TableSubjectTest {

  @Test
  public void isEmpty() {
    ImmutableTable<String, String, String> table = ImmutableTable.of();
    assertThat(table).isEmpty();
  }

  @Test
  public void isEmptyWithFailure() {
    ImmutableTable<Integer, Integer, Integer> table = ImmutableTable.of(1, 5, 7);
    AssertionError e = expectFailure(whenTesting -> whenTesting.that(table).isEmpty());
    assertFailureKeys(e, "expected to be empty", "but was");
  }

  @Test
  public void isEmptyOnNullTable() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that((Table<?, ?, ?>) null).isEmpty());
    assertFailureKeys(e, "expected an empty table", "but was");
  }

  @Test
  public void isNotEmpty() {
    ImmutableTable<Integer, Integer, Integer> table = ImmutableTable.of(1, 5, 7);
    assertThat(table).isNotEmpty();
  }

  @Test
  public void isNotEmptyWithFailure() {
    ImmutableTable<Integer, Integer, Integer> table = ImmutableTable.of();
    AssertionError e = expectFailure(whenTesting -> whenTesting.that(table).isNotEmpty());
    assertFailureKeys(e, "expected not to be empty");
  }

  @Test
  public void isNotEmptyOnNullTable() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that((Table<?, ?, ?>) null).isNotEmpty());
    assertFailureKeys(e, "expected a nonempty table", "but was");
  }

  @Test
  public void hasSize() {
    assertThat(ImmutableTable.of(1, 2, 3)).hasSize(1);
  }

  @Test
  public void hasSizeZero() {
    assertThat(ImmutableTable.of()).hasSize(0);
  }

  @Test
  public void hasSizeNegative() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(ImmutableTable.of(1, 2, 3)).hasSize(-1));
    assertFailureKeys(
        e,
        "expected a table with a negative size, but that is impossible",
        "expected size",
        "actual size",
        "table was");
    assertFailureValue(e, "expected size", "-1");
    assertFailureValue(e, "actual size", "1");
    assertFailureValue(e, "table was", "{1={2=3}}");
  }

  @Test
  public void hasSizeOnNullTable() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that((Table<?, ?, ?>) null).hasSize(1));
    assertFailureKeys(e, "expected a table with size", "but was");
    assertFailureValue(e, "expected a table with size", "1");
  }

  @Test
  public void contains() {
    ImmutableTable<String, String, String> table = ImmutableTable.of("row", "col", "val");
    assertThat(table).contains("row", "col");
  }

  @Test
  public void containsFailure() {
    ImmutableTable<String, String, String> table = ImmutableTable.of("row", "col", "val");
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(table).contains("row", "otherCol"));
    assertThat(e)
        .factKeys()
        .containsExactly(
            "expected to contain mapping for row-column key pair",
            "row key",
            "column key",
            "but was");
    assertThat(e).factValue("row key").isEqualTo("row");
    assertThat(e).factValue("column key").isEqualTo("otherCol");
  }

  @Test
  public void containsOnNullTable() {
    AssertionError e =
        expectFailure(
            whenTesting -> whenTesting.that((Table<?, ?, ?>) null).contains("row", "otherCol"));
    assertThat(e)
        .factKeys()
        .containsExactly(
            "expected a table that contains a mapping for row-column key pair",
            "row key",
            "column key",
            "but was");
  }

  @Test
  public void doesNotContain() {
    ImmutableTable<String, String, String> table = ImmutableTable.of("row", "col", "val");
    assertThat(table).doesNotContain("row", "row");
    assertThat(table).doesNotContain("col", "row");
    assertThat(table).doesNotContain("col", "col");
    assertThat(table).doesNotContain(null, null);
  }

  @Test
  public void doesNotContainFailure() {
    ImmutableTable<String, String, String> table = ImmutableTable.of("row", "col", "val");
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(table).doesNotContain("row", "col"));
    assertThat(e)
        .factKeys()
        .containsExactly(
            "expected not to contain mapping for row-column key pair",
            "row key",
            "column key",
            "but contained value",
            "full contents");
    assertThat(e).factValue("row key").isEqualTo("row");
    assertThat(e).factValue("column key").isEqualTo("col");
    assertThat(e).factValue("but contained value").isEqualTo("val");
  }

  @Test
  public void doesNotContainOnNullTable() {
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting.that((Table<?, ?, ?>) null).doesNotContain("row", "otherCol"));
    assertThat(e)
        .factKeys()
        .containsExactly(
            "expected a table that does not contain a mapping for row-column key pair",
            "row key",
            "column key",
            "but was");
  }

  @Test
  public void containsCell() {
    ImmutableTable<String, String, String> table = ImmutableTable.of("row", "col", "val");
    assertThat(table).containsCell("row", "col", "val");
    assertThat(table).containsCell(cell("row", "col", "val"));
  }

  @Test
  public void containsCellFailure() {
    ImmutableTable<String, String, String> table = ImmutableTable.of("row", "col", "val");
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(table).containsCell("row", "row", "val"));
    assertFailureKeys(e, "value of", "expected to contain", "but was");
    assertFailureValue(e, "value of", "table.cellSet()");
    assertFailureValue(e, "expected to contain", "(row,row)=val");
    assertFailureValue(e, "but was", "[(row,col)=val]");
  }

  @Test
  public void containsCellFailureWithKeyPairPresent() {
    ImmutableTable<String, String, String> table = ImmutableTable.of("row", "col", "val");
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(table).containsCell("row", "col", "var"));
    assertFailureKeys(e, "value of", "expected", "but was", "table was");
    assertFailureValue(e, "value of", "table.get(row, col)");
    assertFailureValue(e, "expected", "var");
    assertFailureValue(e, "but was", "val");
  }

  @Test
  public void containsCellOnNullTable() {
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting.that((Table<?, ?, ?>) null).containsCell("row", "col", "val"));
    assertFailureKeys(
        e,
        "expected a table that contains the given cell",
        "row key",
        "column key",
        "value",
        "but was");
    assertFailureValue(e, "row key", "row");
    assertFailureValue(e, "column key", "col");
    assertFailureValue(e, "value", "val");
    assertFailureValue(e, "but was", "null");
  }

  @Test
  public void containsNullCell() {
    ImmutableTable<String, String, String> table = ImmutableTable.of("row", "col", "val");
    AssertionError e = expectFailure(whenTesting -> whenTesting.that(table).containsCell(null));
    assertFailureKeys(e, "expected to contain a null cell, but that is impossible", "table was");
    assertFailureValue(e, "table was", "{row={col=val}}");
  }

  @Test
  public void doesNotContainCell() {
    ImmutableTable<String, String, String> table = ImmutableTable.of("row", "col", "val");
    assertThat(table).doesNotContainCell("row", "row", "val");
    assertThat(table).doesNotContainCell("col", "row", "val");
    assertThat(table).doesNotContainCell("col", "col", "val");
    assertThat(table).doesNotContainCell(null, null, null);
    assertThat(table).doesNotContainCell(cell("row", "row", "val"));
    assertThat(table).doesNotContainCell(cell("col", "row", "val"));
    assertThat(table).doesNotContainCell(cell("col", "col", "val"));
    assertThat(table).doesNotContainCell(cell(null, null, null));
  }

  @Test
  public void doesNotContainCellFailure() {
    ImmutableTable<String, String, String> table = ImmutableTable.of("row", "col", "val");
    AssertionError e =
        expectFailure(
            whenTesting -> whenTesting.that(table).doesNotContainCell("row", "col", "val"));
    assertFailureKeys(e, "value of", "expected not to contain", "but was");
    assertFailureValue(e, "value of", "table.cellSet()");
    assertFailureValue(e, "expected not to contain", "(row,col)=val");
    assertFailureValue(e, "but was", "[(row,col)=val]");
  }

  @Test
  public void doesNotContainCellOnNullTable() {
    AssertionError e =
        expectFailure(
            whenTesting ->
                whenTesting.that((Table<?, ?, ?>) null).doesNotContainCell("row", "col", "val"));
    assertFailureKeys(
        e,
        "expected a table that does not contain the given cell",
        "row key",
        "column key",
        "value",
        "but was");
    assertFailureValue(e, "row key", "row");
    assertFailureValue(e, "column key", "col");
    assertFailureValue(e, "value", "val");
    assertFailureValue(e, "but was", "null");
  }

  @Test
  public void doesNotContainNullCell() {
    ImmutableTable<String, String, String> table = ImmutableTable.of("row", "col", "val");
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(table).doesNotContainCell(null));
    assertFailureKeys(
        e,
        "refusing to check for a null cell because tables never contain null cells",
        "table was");
    assertFailureValue(e, "table was", "{row={col=val}}");
  }

  @Test
  public void containsRow() {
    ImmutableTable<String, String, String> table = ImmutableTable.of("row", "col", "val");
    assertThat(table).containsRow("row");
  }

  @Test
  public void containsRowFailure() {
    ImmutableTable<String, String, String> table = ImmutableTable.of("row", "col", "val");
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(table).containsRow("your boat"));
    assertFailureKeys(e, "value of", "expected to contain", "but was", "table was");
    assertFailureValue(e, "value of", "table.rowKeySet()");
    assertFailureValue(e, "expected to contain", "your boat");
    assertFailureValue(e, "but was", "[row]");
  }

  @Test
  public void containsRowOnNullTable() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that((Table<?, ?, ?>) null).containsRow("row"));
    assertFailureKeys(e, "expected a table with row", "but was");
    assertFailureValue(e, "expected a table with row", "row");
    assertFailureValue(e, "but was", "null");
  }

  @Test
  public void containsColumn() {
    ImmutableTable<String, String, String> table = ImmutableTable.of("row", "col", "val");
    assertThat(table).containsColumn("col");
  }

  @Test
  public void containsColumnFailure() {
    ImmutableTable<String, String, String> table = ImmutableTable.of("row", "col", "val");
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that(table).containsColumn("response"));
    assertFailureKeys(e, "value of", "expected to contain", "but was", "table was");
    assertFailureValue(e, "value of", "table.columnKeySet()");
    assertFailureValue(e, "expected to contain", "response");
    assertFailureValue(e, "but was", "[col]");
  }

  @Test
  public void containsColumnOnNullTable() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that((Table<?, ?, ?>) null).containsColumn("col"));
    assertFailureKeys(e, "expected a table with column", "but was");
    assertFailureValue(e, "expected a table with column", "col");
    assertFailureValue(e, "but was", "null");
  }

  @Test
  public void containsValue() {
    ImmutableTable<String, String, String> table = ImmutableTable.of("row", "col", "val");
    assertThat(table).containsValue("val");
  }

  @Test
  public void containsValueFailure() {
    ImmutableTable<String, String, String> table = ImmutableTable.of("row", "col", "val");
    AssertionError e = expectFailure(whenTesting -> whenTesting.that(table).containsValue("var"));
    assertFailureKeys(e, "value of", "expected to contain", "but was", "table was");
    assertFailureValue(e, "value of", "table.values()");
    assertFailureValue(e, "expected to contain", "var");
    assertFailureValue(e, "but was", "[val]");
  }

  @Test
  public void containsValueOnNullTable() {
    AssertionError e =
        expectFailure(whenTesting -> whenTesting.that((Table<?, ?, ?>) null).containsValue("val"));
    assertFailureKeys(e, "expected a table with value", "but was");
    assertFailureValue(e, "expected a table with value", "val");
    assertFailureValue(e, "but was", "null");
  }

  private static <
          R extends @Nullable Object, C extends @Nullable Object, V extends @Nullable Object>
      Cell<R, C, V> cell(R row, C col, V val) {
    return immutableCell(row, col, val);
  }
}
