package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import org.designup.picsou.gui.components.charts.histo.HistoChart;
import org.designup.picsou.gui.components.charts.histo.HistoDataset;
import org.designup.picsou.gui.components.charts.histo.diff.HistoDiffDataset;
import org.designup.picsou.gui.components.charts.histo.line.HistoLineDataset;
import org.designup.picsou.gui.description.Formatting;
import org.globsframework.utils.TablePrinter;
import org.uispec4j.Mouse;
import org.uispec4j.Panel;
import org.uispec4j.Window;
import org.uispec4j.interception.toolkit.Empty;
import org.uispec4j.utils.Utils;

import java.awt.*;
import java.util.Set;
import java.util.TreeSet;

public class HistoChecker extends GuiChecker {

  public Window window;
  private String panelName;
  private String chartName;

  public HistoChecker(Window window, String panelName, String chartName) {
    this.window = window;
    this.panelName = panelName;
    this.chartName = chartName;
  }

  public HistoChecker checkColumnCount(int count) {
    HistoDataset dataset = getDataset(HistoDataset.class);
    if (count != dataset.size()) {
      Assert.fail("Found " + dataset.size() + " columns instead of " + count +
                  ". Actual contents:\n" + dataset);
    }
    return this;
  }

  public HistoChecker checkDiffColumn(int index, String label, String section, double reference, double actual) {
    checkDiffColumn(index, label, section, reference, actual, false);
    return this;
  }

  public HistoChecker checkDiffColumn(int index, String label, String section, double reference, double actual, boolean selected) {
    HistoDiffDataset dataset = getDataset(HistoDiffDataset.class);
    Assert.assertEquals(getErrorMessage(index, dataset), label, dataset.getLabel(index));
    Assert.assertEquals(getErrorMessage(index, dataset), section, dataset.getSection(index));
    Assert.assertEquals(getErrorMessage(index, dataset), reference, dataset.getReferenceValue(index), 0.01);
    Assert.assertEquals(getErrorMessage(index, dataset), actual, dataset.getActualValue(index), 0.01);
    Assert.assertEquals(getErrorMessage(index, dataset), selected, dataset.isSelected(index));
    return this;
  }

  public HistoChecker checkLineColumn(int index, String label, String section, double value, boolean selected) {
    HistoLineDataset dataset = getDataset(HistoLineDataset.class);
    Assert.assertEquals(getErrorMessage(index, dataset), label, dataset.getLabel(index));
    Assert.assertEquals(getErrorMessage(index, dataset), section, dataset.getSection(index));
    Assert.assertEquals(getErrorMessage(index, dataset), value, dataset.getValue(index));
    Assert.assertEquals(getErrorMessage(index, dataset), selected, dataset.isSelected(index));
    return this;
  }

  public HistoChecker checkLineColumn(int index, String label, String section, double value) {
    checkLineColumn(index, label, section, value, false);
    return this;
  }

  public void checkContents(Object[][] content) {
    checkColumnCount(content.length);

    TablePrinter expected = createPrinter();
    for (Object[] row : content) {
      expected.addRow(row[0],
                      row[1].toString().substring(0, 1),
                      Formatting.toString((Double)row[2]),
                      Formatting.toString((Double)row[3]),
                      row.length > 4 ? (Boolean)row[4] : "");
    }

    TablePrinter actual = createPrinter();
    HistoDiffDataset dataset = getDataset(HistoDiffDataset.class);
    for (int i = 0; i < dataset.size(); i++) {
      actual.addRow(dataset.getSection(i),
                    dataset.getLabel(i),
                    Formatting.toString(dataset.getActualValue(i)),
                    Formatting.toString(dataset.getReferenceValue(i)),
                    dataset.isSelected(i) ? "true" : "");
    }

    Assert.assertEquals("Invalid chart content", expected.toString(), actual.toString());
  }

  public void dump() {
    StringBuilder builder = new StringBuilder();
    HistoDiffDataset dataset = getDataset(HistoDiffDataset.class);
    builder.append(".checkColumnCount(").append(dataset.size()).append(")\n");
    for (int i = 0; i < dataset.size(); i++) {
        builder.append(".checkDiffColumn(").append(i).append(", \"")
          .append(dataset.getLabel(i)).append("\", \"")
          .append(dataset.getSection(i)).append("\", ")
          .append(Formatting.toString(dataset.getReferenceValue(i))).append(", ")
          .append(Formatting.toString(dataset.getActualValue(i)))
          .append(dataset.isSelected(i) ? ", true" : "")
          .append(")\n");
    }
    System.out.println(builder.toString());
  }

  private TablePrinter createPrinter() {
    TablePrinter printer = new TablePrinter();
    printer.setHeader("Year", "Month", "Actual", "Planned", "Selected");
    return printer;
  }

  public HistoChecker checkTooltip(int index, String expectedText) {
    HistoDataset dataset = getDataset(HistoDataset.class);
    Assert.assertEquals(expectedText, Utils.cleanupHtml(dataset.getTooltip(index)));
    return this;
  }

  public void click(double xPercent) {
    HistoChart chart = getChart();
    chart.paint(Empty.NULL_GRAPHICS_2D);
    Dimension size = chart.getSize();
    int x = (int)((size.width - 50) * xPercent);
    doClick(chart, x);
  }

  public void clickColumn(int columnIndex) {
    HistoChart chart = getChart();
    chart.paint(Empty.NULL_GRAPHICS_2D);
    int x = chart.getX(columnIndex);
    doClick(chart, x);
  }

  public void clickColumnId(int month) {
    HistoChart chart = getChart();
    HistoDataset dataset = chart.getCurrentDataset();
    int columnIndex = dataset.getIndex(month);
    if (columnIndex < 0) {
      Assert.fail("Month " + month + " not found - dataset contents:\n" + dataset);
    }

    clickColumn(columnIndex);
  }

  public void clickAllColumns() {
    HistoChart chart = getChart();
    chart.paint(Empty.NULL_GRAPHICS_2D);
    int y = chart.getSize().height / 2;

    int x0 = chart.getX(0);
    Mouse.enter(chart, x0, y);
    Mouse.move(chart, x0, y);
    Mouse.pressed(chart, x0, y);

    chart.paint(Empty.NULL_GRAPHICS_2D);
    int x1 = chart.getX(chart.getCurrentDataset().size() - 1);
    Mouse.drag(chart, x1, y);
    Mouse.released(chart, x1, y);
    Mouse.exit(chart, x1, y);
  }

  private void doClick(HistoChart chart, int x) {
    int y = chart.getSize().height / 2;

    Mouse.enter(chart, x, y);
    Mouse.move(chart, x, y);
    Mouse.pressed(chart, x, y);
    Mouse.released(chart, x, y);
    Mouse.exit(chart, x, y);
  }

  public void checkSelectedIds(Integer... ids) {
    HistoChart chart = getChart();
    HistoDataset dataset = chart.getCurrentDataset();

    Set<Integer> selection = new TreeSet<Integer>();
    for (int index = 0; index < dataset.size(); index++) {
      if (dataset.isSelected(index)) {
        selection.add(dataset.getId(index));
      }
    }

    org.globsframework.utils.TestUtils.assertSetEquals(selection, ids);
  }

  private String getErrorMessage(int index, HistoDataset dataset) {
    return "Error at index: " + index + " - dataset contents:\n" + dataset;
  }

  private <T extends HistoDataset> T getDataset(Class<T> datasetClass) {
    HistoChart chart = getChart();
    HistoDataset dataset = chart.getCurrentDataset();
    if (!datasetClass.isAssignableFrom(dataset.getClass())) {
      throw new AssertionFailedError("Unexpected dataset type: " + datasetClass.getSimpleName());
    }
    return (T)dataset;
  }

  private HistoChart getChart() {
    Panel panel = getPanel();
    return (HistoChart)panel.getAwtComponent();
  }

  private Panel getPanel() {
    return window.getPanel(panelName).getPanel(chartName);
  }

  public HistoChecker scroll(int i) {
    Mouse.wheel(getPanel(), i);
    return this;
  }
}
