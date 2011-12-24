package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.designup.picsou.gui.components.charts.histo.HistoChart;
import org.designup.picsou.gui.components.charts.histo.HistoDataset;
import org.designup.picsou.gui.components.charts.histo.daily.HistoDailyDataset;
import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.model.util.Amounts;
import org.globsframework.utils.Utils;
import org.uispec4j.Mouse;
import org.uispec4j.Panel;

public class HistoDailyChecker extends AbstractHistoChecker<HistoDailyChecker> {

  private Panel chartPanel;
  private String chartName;

  public HistoDailyChecker(Panel rootPanel, String chartName) {
    this.chartName = chartName;
    this.chartPanel = rootPanel.getPanel(chartName);
  }

  public HistoDailyChecker checkEndOfMonthValue(Double value) {
    Double actual = getDataset().getLastValue(0);
    if (!Utils.equal(value, actual)) {
      Assert.fail("expected: " + value + " but was: " + actual + "\nActual content:\n" + getDataset().toString(0));
    }
    return this;
  }

  public HistoDailyChecker checkSelected(int monthId) {
    HistoDataset dataset = getDataset();
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

  public HistoDailyChecker checkValue(int monthId, int day, double expectedValue) {
    HistoDailyDataset dataset = getDataset();
    int index = dataset.getIndex(monthId);
    if (index < 0) {
      Assert.fail("Month " + monthId + " not found");
    }
    Double actual = dataset.getValue(index, day - 1);
    if (!Amounts.equal(actual, expectedValue)) {
      Assert.fail("Error for " + monthId + Formatting.TWO_DIGIT_INTEGER_FORMAT.format(day) +
                  " - was " + actual + " instead of " + expectedValue +
                  "\nDataset content:\n" + dataset);
    }
    return this;
  }

  protected HistoDailyDataset getDataset() {
    return getDataset(HistoDailyDataset.class);
  }

  public void dump() {
    Assert.fail("Chart content:\n" + getDataset().toString());
  }

  public void doubleClick() {
    Mouse.doubleClick(new Panel(getChart()));
  }

  protected HistoChart getChart() {
    return (HistoChart)chartPanel.getAwtComponent();
  }

  protected Panel getPanel() {
    return chartPanel;
  }

  protected String getName() {
    return chartName;
  }
}
