package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import org.designup.picsou.gui.components.charts.histo.HistoChart;
import org.designup.picsou.gui.components.charts.histo.HistoDataset;
import org.designup.picsou.gui.components.charts.histo.painters.HistoDiffDataset;
import org.designup.picsou.gui.components.charts.histo.painters.HistoLineDataset;
import org.uispec4j.Mouse;
import org.uispec4j.Panel;
import org.uispec4j.Window;
import org.uispec4j.interception.toolkit.Empty;
import org.uispec4j.utils.Utils;

import java.awt.*;
import java.util.HashSet;
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
    Assert.assertEquals(count, dataset.size());
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

  public void clickColumnId(int month) {
    HistoChart chart = getChart();
    HistoDataset dataset = chart.getCurrentDataset();
    int columnIndex = dataset.getIndex(month);
    if (columnIndex < 0) {
      Assert.fail("Month " + month + " not found - dataset contents:\n" + dataset);
    }

    clickColumn(columnIndex);
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
    Panel panel = window.getPanel(panelName).getPanel(chartName);
    return (HistoChart)panel.getAwtComponent();
  }
}
