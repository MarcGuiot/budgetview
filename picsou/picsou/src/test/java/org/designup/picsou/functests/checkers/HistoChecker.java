package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
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

public class HistoChecker extends AbstractHistoChecker<HistoChecker> {

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
    Assert.assertEquals(getErrorMessage(index, dataset), label, dataset.getLabel(index).substring(0, 1));
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
                      row[1].toString().substring(0, 3),
                      Formatting.toString((Double)row[2]),
                      Formatting.toString((Double)row[3]),
                      row.length > 4 ? (Boolean)row[4] : "");
    }

    TablePrinter actual = createPrinter();
    HistoDiffDataset dataset = getDataset(HistoDiffDataset.class);
    for (int i = 0; i < dataset.size(); i++) {
      actual.addRow(dataset.getSection(i),
                    dataset.getLabel(i).substring(0, 3),
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


  private String getErrorMessage(int index, HistoDataset dataset) {
    return "Error at index: " + index + " - dataset contents:\n" + dataset;
  }

  public HistoChecker checkRange(int firstMonth, int lastMonth) {
    HistoDataset dataset = getDataset(HistoDataset.class);
    int actualFirst = dataset.getId(0);
    int actualLast = dataset.getId(dataset.size() - 1);
    if ((actualFirst != firstMonth) || (actualLast != lastMonth)) {
      Assert.fail("expected: [" + firstMonth + "," + lastMonth + "] but was: [" + actualFirst + "," + actualLast + "]");
    }
    return this;
  }

  public HistoChecker scroll(int offset) {
    Mouse.wheel(getPanel(), offset);
    return this;
  }

  protected HistoChart getChart() {
    Panel panel = getPanel();
    return (HistoChart)panel.getAwtComponent();
  }

  protected Panel getPanel() {
    return window.getPanel(panelName).getPanel(chartName);
  }
}
