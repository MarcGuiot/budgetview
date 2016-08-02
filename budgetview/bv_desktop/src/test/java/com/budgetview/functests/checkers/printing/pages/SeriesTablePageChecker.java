package com.budgetview.functests.checkers.printing.pages;

import com.budgetview.desktop.printing.budget.tables.SeriesTable;
import junit.framework.Assert;
import org.globsframework.utils.TablePrinter;
import org.globsframework.utils.TestUtils;

import java.util.ArrayList;
import java.util.List;

public class SeriesTablePageChecker {

  private SeriesTable seriesTable;
  private List<String[]> expectedRows = new ArrayList<String[]>();
  private List<String[]> actualRows;

  public SeriesTablePageChecker(SeriesTable seriesTable) {
    this.seriesTable = seriesTable;
  }

  public SeriesTablePageChecker checkTitle(String title) {
    Assert.assertEquals(title, seriesTable.getTitle());
    return this;
  }

  public SeriesTablePageChecker add(String... row) {
    expectedRows.add(row);
    return this;
  }

  public void check() {
    Assert.assertEquals(toString(expectedRows), toString(getActualRows()));
  }

  public SeriesTablePageChecker checkRow(int rowIndex, String... expectedRow) {
    List<String[]> actual = getActualRows();
    if (actual.size() <= rowIndex) {
      Assert.fail("Unexpected row count: " + actual.size() + " < " + rowIndex + ". " +
                  "Actual content:\n" +
                  toString(actualRows));
    }

    String[] actualRow = actual.get(rowIndex);
    TestUtils.assertEquals(expectedRow, actualRow);
    return this;
  }

  public SeriesTablePageChecker checkRowCount(int count) {
    List<String[]> rows = getActualRows();
    if (rows.size() != count) {
      Assert.fail("Unexpected row count: " + rows.size() + " instead of " + count + ". " +
                  "Actual content:\n" +
                  toString(actualRows));
    }
    return this;
  }

  public void checkEmpty() {
    if (expectedRows.size() > 1) {
      Assert.fail("Table expected to be empty but was: " + toString(getActualRows()));
    }
  }

  private String toString(List<String[]> rows) {
    TablePrinter printer = new TablePrinter(true);
    boolean first = true;
    for (String[] row : rows) {
      if (first) {
        printer.setHeader(row);
        first = false;
      }
      else {
        printer.addRow(row);
      }
    }
    return printer.toString();
  }

  private List<String[]> getActualRows() {
    if (this.actualRows != null) {
      return this.actualRows;
    }

    actualRows = new ArrayList<String[]>();
    int columnCount = seriesTable.getColumnCount();
    String[] header = new String[columnCount];
    for (int column = 0; column < columnCount; column++) {
      header[column] = seriesTable.getColumnTitle(column);
    }
    actualRows.add(header);
    for (SeriesTable.SeriesRow seriesRow : seriesTable.rows()) {
      String[] array = new String[columnCount];
      for (int column = 0; column < columnCount; column++) {
        array[column] = seriesRow.getValue(column);
      }
      actualRows.add(array);
    }

    return actualRows;
  }

  public void dumpCode() {
    StringBuilder builder = new StringBuilder();
    List<String[]> rows = getActualRows();
    for (String[] row : rows) {
      builder.append("    .add(");
      boolean first = true;
      for (String value : row) {
        if (!first) {
          builder.append(',');
        }
        builder.append('"').append(value).append('"');
        first = false;
      }
      builder.append(")\n");
    }
    builder.append("    .check();");
    Assert.fail("Actual code:\n" + builder.toString());
  }
}
