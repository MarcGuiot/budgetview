package org.designup.picsou.functests.checkers.printing;

import junit.framework.Assert;
import org.designup.picsou.functests.checkers.components.HistoChartChecker;
import org.designup.picsou.functests.checkers.components.StackChecker;
import org.designup.picsou.gui.printing.PrintableReport;
import org.designup.picsou.gui.printing.report.BudgetOverviewPage;
import org.designup.picsou.gui.printing.report.BudgetReport;
import org.designup.picsou.gui.printing.report.SeriesTable;
import org.designup.picsou.gui.printing.report.SeriesTablePage;
import org.globsframework.utils.TablePrinter;
import org.globsframework.utils.TestUtils;
import org.uispec4j.Panel;

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

  public BudgetOverviewPageChecker getChartPage() {
    Printable printable = report.getPrintable(0);
    if (!(printable instanceof BudgetOverviewPage)) {
      Assert.fail("Unexpected type for page 0: " + printable.getClass());
    }
    return new BudgetOverviewPageChecker((BudgetOverviewPage)printable);
  }

  public class TablePageChecker {

    private SeriesTable seriesTable;
    private List<String[]> expectedRows = new ArrayList<String[]>();
    private List<String[]> actualRows;

    public TablePageChecker(SeriesTable seriesTable) {
      this.seriesTable = seriesTable;
    }

    public TablePageChecker checkTitle(String title) {
      Assert.assertEquals(title, seriesTable.getTitle());
      return this;
    }

    public TablePageChecker add(String... row) {
      expectedRows.add(row);
      return this;
    }

    public void check() {
      Assert.assertEquals(toString(expectedRows), toString(getActualRows()));
    }

    public TablePageChecker checkRow(int rowIndex, String... expectedRow) {
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

    public TablePageChecker checkRowCount(int count) {
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

  public class BudgetOverviewPageChecker {
    private Panel panel;

    public BudgetOverviewPageChecker(BudgetOverviewPage page) {
      this.panel = new Panel(page.getPanel());
    }

    public StackChecker getOverviewStack() {
      return new StackChecker(panel.getPanel("balanceChart"));
    }

    public StackChecker getExpensesStack() {
      return new StackChecker(panel.getPanel("seriesChart"));
    }

    public HistoChartChecker getHistoChart() {
      return new HistoChartChecker(panel.getPanel("histoChart"));
    }
  }
}
