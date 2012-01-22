package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.designup.picsou.gui.printing.PrintableReport;
import org.designup.picsou.gui.printing.report.BudgetReport;
import org.designup.picsou.gui.printing.report.SeriesTable;
import org.designup.picsou.gui.printing.report.SeriesTablePage;
import org.globsframework.utils.TablePrinter;

import java.awt.print.Printable;
import java.util.ArrayList;
import java.util.List;

public class BudgetReportChecker {
  private BudgetReport report;

  public BudgetReportChecker(PrintableReport report) {
    if (!(report instanceof BudgetReport)) {
      Assert.fail("Unexpected report type: " + report.getClass());
    }
    this.report = (BudgetReport)report;
  }

  public BudgetReportChecker checkPageCount(int count) {
    Assert.assertEquals(count, report.getNumberOfPages());
    return this;
  }

  public TablePageChecker initTablePage(int page) {
    Printable printable = report.getPrintable(page);
    if (!(printable instanceof SeriesTablePage)) {
      Assert.fail("Unexpected type for page " + page + ": " + printable.getClass());
    }
    return new TablePageChecker(((SeriesTablePage)printable).getSeriesTable());
  }

  public class TablePageChecker {

    private SeriesTable seriesTable;
    private List<String[]> rows = new ArrayList<String[]>();

    public TablePageChecker(SeriesTable seriesTable) {
      this.seriesTable = seriesTable;
    }

    public TablePageChecker checkTitle(String title) {
      Assert.assertEquals(title, seriesTable.getTitle());
      return this;
    }

    public TablePageChecker add(String... row) {
      rows.add(row);
      return this;
    }

    public void check() {
      Assert.assertEquals(toString(rows), toString(getActualRows()));
    }

    public void checkEmpty() {
      if (rows.size() > 1) {
        Assert.fail("Table expected to be empty but was: " + toString(getActualRows()));
      }
    }

    private String toString(List<String[]> rows) {
      TablePrinter printer = new TablePrinter();
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
      List<String[]> result = new ArrayList<String[]>();

      int columnCount = seriesTable.getColumnCount();
      String[] header = new String[columnCount];
      for (int column = 0; column < columnCount; column++) {
        header[column] = seriesTable.getColumnTitle(column);
      }
      result.add(header);
      for (SeriesTable.SeriesRow seriesRow : seriesTable.rows()) {
        String[] array = new String[columnCount];
        for (int column = 0; column < columnCount; column++) {
          array[column] = seriesRow.getValue(column);
        }
        result.add(array);
      }

      return result;
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
}
