package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import org.designup.picsou.gui.components.charts.histo.HistoChart;
import org.designup.picsou.gui.components.charts.histo.HistoDataset;
import org.designup.picsou.gui.components.charts.histo.daily.HistoDailyDataset;
import org.globsframework.utils.Utils;
import org.uispec4j.Mouse;
import org.uispec4j.Panel;

public class HistoDailyChecker extends GuiChecker {

  private Panel chartPanel;

  public HistoDailyChecker(Panel rootPanel, String chartName) {
    this.chartPanel = rootPanel.getPanel(chartName);
  }

  public HistoDailyChecker checkEndOfMonthValue(Double value) {
    Double actual = getDataset().getLastValue(0);
    if (!Utils.equal(value, actual)) {
      Assert.fail("expected: " + value + " but was: " + actual + "\nActual content:\n" + getDataset().toString(0));
    }
    return this;
  }

  public HistoDailyChecker checkRange(int firstMonth, int lastMonth) {
    HistoDailyDataset dataset = getDataset();
    int actualFirst = dataset.getId(0);
    int actualLast = dataset.getId(dataset.size() - 1);
    if ((actualFirst != firstMonth) || (actualLast != lastMonth)) {
      Assert.fail("expected: [" + firstMonth + "," + lastMonth + "] but was: [" + actualFirst + "," + actualLast + "]");
    }
    return this;
  }

  public HistoDailyChecker checkSelected(int monthId) {
    HistoDailyDataset dataset = getDataset();
    for (int i = 0; i < dataset.size(); i++) {
      if ((dataset.getId(i) == monthId)) {
        if (!dataset.isSelected(i)) {
          Assert.fail("Month " + monthId + " not selected. Actual content: " + dataset);
        }
        return this;
      }
    }
    Assert.fail("Month " + monthId + " not shown. Actual content: " + dataset);
    return this;
  }

  public void doubleClick() {
    Mouse.doubleClick(new Panel(getChart()));
  }

  public HistoDailyChecker scroll(int offset) {
    Mouse.wheel(chartPanel, offset);
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
    return (HistoChart)chartPanel.getAwtComponent();
  }
}
