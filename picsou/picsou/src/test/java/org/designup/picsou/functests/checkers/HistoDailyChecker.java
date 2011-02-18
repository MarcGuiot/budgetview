package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.designup.picsou.gui.components.charts.histo.HistoChart;
import org.designup.picsou.gui.components.charts.histo.HistoDataset;
import org.designup.picsou.gui.components.charts.histo.daily.HistoDailyDataset;
import org.globsframework.utils.Utils;
import org.uispec4j.Mouse;
import org.uispec4j.Panel;

public class HistoDailyChecker extends AbstractHistoChecker<HistoDailyChecker> {

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

  protected HistoDailyDataset getDataset() {
    return getDataset(HistoDailyDataset.class);
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
}
