package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import org.designup.picsou.gui.components.charts.histo.HistoChart;
import org.designup.picsou.gui.components.charts.histo.HistoDataset;
import org.designup.picsou.gui.components.charts.histo.daily.HistoDailyDataset;
import org.designup.picsou.model.util.Amounts;
import org.globsframework.utils.Utils;
import org.uispec4j.Panel;
import org.uispec4j.Window;

public class HistoDailyChecker extends GuiChecker {

  public Window window;
  private String panelName;
  private String chartName;

  public HistoDailyChecker(Window window, String panelName, String chartName) {
    this.window = window;
    this.panelName = panelName;
    this.chartName = chartName;
  }

  public HistoDailyChecker checkEndOfMonthValue(Double value) {
    Double actual = getDataset().getLastValue(0);
    if (!Utils.equal(value, actual)) {
      Assert.fail("expected: " + value + " but was: " + actual + "\nActual content:\n" + getDataset().toString(0));
    }
    return this;
  }

  private HistoDailyDataset getDataset() {
    HistoChart chart = getChart();
    HistoDataset dataset = chart.getCurrentDataset();
    if (!HistoDailyDataset.class.isAssignableFrom(dataset.getClass())) {
      throw new AssertionFailedError("Unexpected dataset type: " + dataset.getClass().getSimpleName());
    }
    return (HistoDailyDataset)dataset;
  }

  private HistoChart getChart() {
    Panel panel = getPanel();
    return (HistoChart)panel.getAwtComponent();
  }

  private Panel getPanel() {
    return window.getPanel(panelName).getPanel(chartName);
  }
}
