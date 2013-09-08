package org.designup.picsou.functests.checkers.components;

import junit.framework.Assert;
import org.designup.picsou.functests.checkers.AbstractHistoChecker;
import org.designup.picsou.gui.components.charts.histo.HistoChart;
import com.budgetview.shared.gui.histochart.HistoDataset;
import org.designup.picsou.gui.components.charts.histo.daily.HistoDailyDataset;
import org.designup.picsou.gui.components.charts.histo.diff.HistoDiffDataset;
import org.designup.picsou.gui.components.charts.histo.line.HistoLineDataset;
import org.designup.picsou.gui.description.Formatting;
import org.globsframework.utils.TablePrinter;
import org.uispec4j.Mouse;
import org.uispec4j.Panel;
import org.uispec4j.utils.Utils;

public class HistoChartChecker extends AbstractHistoChecker<HistoChartChecker> {

  public Panel container;
  private String panelName;
  private String chartName;
  private Panel panel;

  public HistoChartChecker(Panel panel) {
    this.panel = panel;
  }

  public HistoChartChecker(Panel container, String panelName, String chartName) {
    this.container = container;
    this.panelName = panelName;
    this.chartName = chartName;
  }

  public HistoChartChecker checkColumnCount(int count) {
    HistoDataset dataset = getDataset(HistoDataset.class);
    if (count != dataset.size()) {
      Assert.fail("Found " + dataset.size() + " columns instead of " + count +
                  ". Actual contents:\n" + dataset);
    }
    return this;
  }

  public HistoChartChecker checkDiffColumn(int index, String label, String section, double reference, double actual) {
    checkDiffColumn(index, label, section, reference, actual, false);
    return this;
  }

  public HistoChartChecker checkDiffColumn(int index, String label, String section, double reference, double actual, boolean selected) {
    HistoDiffDataset dataset = getDataset(HistoDiffDataset.class);
    String errorMessage = getErrorMessage(index, dataset);
    Assert.assertEquals(errorMessage, label, dataset.getLabel(index));
    Assert.assertEquals(errorMessage, section, dataset.getSection(index));
    Assert.assertEquals(errorMessage, reference, dataset.getReferenceValue(index), 0.01);
    Assert.assertEquals(errorMessage, actual, dataset.getActualValue(index), 0.01);
    Assert.assertEquals(errorMessage, selected, dataset.isSelected(index));
    return this;
  }

  public HistoChartChecker checkLineColumn(int index, String label, String section, double value, boolean selected) {
    HistoLineDataset dataset = getDataset(HistoLineDataset.class);
    String errorMessage = getErrorMessage(index, dataset);
    Assert.assertEquals(errorMessage, label.substring(0,1), dataset.getLabel(index).substring(0,1));
    Assert.assertEquals(errorMessage, section, dataset.getSection(index));
    Assert.assertEquals(errorMessage, value, dataset.getValue(index), 0.01);
    Assert.assertEquals(errorMessage, selected, dataset.isSelected(index));
    return this;
  }

  public HistoChartChecker checkLineColumn(int index, String label, String section, double value) {
    checkLineColumn(index, label, section, value, false);
    return this;
  }

  public HistoChartChecker checkDailyColumn(int index, String label, String section, double value) {
    checkDailyColumn(index, label, section, value, false);
    return this;
  }

  public HistoChartChecker checkDailyColumn(int index, String label, String section, double value, boolean selected) {
    HistoDailyDataset dataset = getDataset(HistoDailyDataset.class);
    Assert.assertEquals(getErrorMessage(index, dataset), label, dataset.getLabel(index).substring(0, 1));
    Assert.assertEquals(getErrorMessage(index, dataset), section, dataset.getSection(index));
    Assert.assertEquals(getErrorMessage(index, dataset), value, dataset.getLastValue(index));
    Assert.assertEquals(getErrorMessage(index, dataset), selected, dataset.isSelected(index));
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

  public void dumpCode() {
    StringBuilder builder = new StringBuilder();
    HistoChart chart = getChart();
    HistoDataset dataset = chart.getCurrentDataset();
    builder.append(".checkColumnCount(").append(dataset.size()).append(")\n");

    if (dataset instanceof HistoDiffDataset) {
      HistoDiffDataset diffDataset = (HistoDiffDataset)dataset;
      for (int i = 0; i < diffDataset.size(); i++) {
        builder.append(".checkDiffColumn(").append(i).append(", \"")
          .append(diffDataset.getLabel(i)).append("\", \"")
          .append(diffDataset.getSection(i)).append("\", ")
          .append(Formatting.toString(diffDataset.getReferenceValue(i))).append(", ")
          .append(Formatting.toString(diffDataset.getActualValue(i)))
          .append(diffDataset.isSelected(i) ? ", true" : "")
          .append(")\n");
      }
    }
    else if (dataset instanceof HistoLineDataset) {
      HistoLineDataset lineDataset = (HistoLineDataset)dataset;
      for (int i = 0; i < lineDataset.size(); i++) {
        builder.append(".checkLineColumn(").append(i).append(", \"")
          .append(lineDataset.getLabel(i)).append("\", \"")
          .append(lineDataset.getSection(i)).append("\", ")
          .append(Formatting.toString(lineDataset.getValue(i)))
          .append(lineDataset.isSelected(i) ? ", true" : "")
          .append(")\n");
      }
    }
    else {
      Assert.fail("Unexpected dataset type: " + dataset);
    }
    Assert.fail("Insert:\n" + builder.toString());
  }

  private TablePrinter createPrinter() {
    TablePrinter printer = new TablePrinter(true);
    printer.setHeader("Year", "Month", "Actual", "Planned", "Selected");
    return printer;
  }

  public HistoChartChecker checkTooltip(int index, String expectedText) {
    HistoDataset dataset = getDataset(HistoDataset.class);
    Assert.assertEquals(expectedText, Utils.cleanupHtml(dataset.getTooltip(index, null)));
    return this;
  }

  private String getErrorMessage(int index, HistoDataset dataset) {
    return "Error at index: " + index + " - dataset contents:\n" + dataset;
  }

  public HistoChartChecker scroll(int offset) {
    Mouse.wheel(getPanel(), offset);
    return this;
  }

  protected HistoChart getChart() {
    return (HistoChart)getPanel().getAwtComponent();
  }

  protected Panel getPanel() {
    if (panel == null) {
      panel = container.getPanel(panelName).getPanel(chartName);
    }
    return panel;
  }

  protected String getName() {
    return chartName;
  }
}
