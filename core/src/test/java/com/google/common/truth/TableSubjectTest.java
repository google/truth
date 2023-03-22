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

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;
import com.google.common.collect.Tables;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for Table Subjects.
 *
 * @author Kurt Alfred Kluever
 */
@RunWith(JUnit4.class)
public class TableSubjectTest extends BaseSubjectTestCase {

  @Test
  public void tableIsEmpty() {
    ImmutableTable<String, String, String> table = ImmutableTable.of();
    assertThat(table).isEmpty();
  }

  @Test
  public void tableIsEmptyWithFailure() {
    ImmutableTable<Integer, Integer, Integer> table = ImmutableTable.of(1, 5, 7);
    expectFailureWhenTestingThat(table).isEmpty();
    assertFailureKeys("expected to be empty", "but was");
  }

  @Test
  public void tableIsNotEmpty() {
    ImmutableTable<Integer, Integer, Integer> table = ImmutableTable.of(1, 5, 7);
    assertThat(table).isNotEmpty();
  }

  @Test
  public void tableIsNotEmptyWithFailure() {
    ImmutableTable<Integer, Integer, Integer> table = ImmutableTable.of();
    expectFailureWhenTestingThat(table).isNotEmpty();
    assertFailureKeys("expected not to be empty");
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
    try {
      assertThat(ImmutableTable.of(1, 2, 3)).hasSize(-1);
      fail();
    } catch (IllegalArgumentException expected) {
    }
  }

  @Test
  public void contains() {
    ImmutableTable<String, String, String> table = ImmutableTable.of("row", "col", "val");
    assertThat(table).contains("row", "col");
  }

  @Test
  public void containsFailure() {
    ImmutableTable<String, String, String> table = ImmutableTable.of("row", "col", "val");
    expectFailureWhenTestingThat(table).contains("row", "row");
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo("Not true that <{row={col=val}}> contains mapping for row/column <row> <row>");
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
    expectFailureWhenTestingThat(table).doesNotContain("row", "col");
    assertThat(expectFailure.getFailure())
        .hasMessageThat()
        .isEqualTo(
            "Not true that <{row={col=val}}> does not contain mapping for "
                + "row/column <row> <col>");
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
    expectFailureWhenTestingThat(table).containsCell("row", "row", "val");
    assertFailureKeys("value of", "expected to contain", "but was");
    assertFailureValue("value of", "table.cellSet()");
    assertFailureValue("expected to contain", "(row,row)=val");
    assertFailureValue("but was", "[(row,col)=val]");
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
    expectFailureWhenTestingThat(table).doesNotContainCell("row", "col", "val");
    assertFailureKeys("value of", "expected not to contain", "but was");
    assertFailureValue("value of", "table.cellSet()");
    assertFailureValue("expected not to contain", "(row,col)=val");
    assertFailureValue("but was", "[(row,col)=val]");
  }

  private static <R, C, V> Cell<R, C, V> cell(R row, C col, V val) {
    return Tables.immutableCell(row, col, val);
  }

  private TableSubject expectFailureWhenTestingThat(Table actual) {
    return expectFailure.whenTesting().that(actual);
  }
}
