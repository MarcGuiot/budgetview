package org.designup.picsou.functests.checkers.components;

import com.budgetview.shared.utils.Amounts;
import junit.framework.Assert;
import org.designup.picsou.functests.checkers.AbstractHistoChecker;
import org.designup.picsou.gui.components.charts.histo.HistoChart;
import org.designup.picsou.gui.components.charts.histo.HistoSelectionManager;
import org.designup.picsou.gui.components.charts.histo.daily.HistoDailyDataset;
import org.designup.picsou.model.Day;
import org.globsframework.model.Key;
import org.globsframework.utils.Utils;
import org.uispec4j.Mouse;
import org.uispec4j.Panel;

import java.awt.*;
import java.util.Collections;

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

  public HistoDailyChecker checkSelected(int monthId, int day) {
    HistoDailyDataset dataset = getDataset();
    int monthIndex = dataset.getIndex(monthId);
    if (!dataset.isDaySelected(monthIndex, day - 1)) {
      Assert.fail("Day " + day + " of " + monthId + " is not selected. Actual content: " + dataset);
    }
    return this;
  }

  public HistoDailyChecker checkNotSelected(int monthId, int day) {
    HistoDailyDataset dataset = getDataset();
    int monthIndex = dataset.getIndex(monthId);
    if (dataset.isDaySelected(monthIndex, day - 1)) {
      Assert.fail("Day " + day + " of " + monthId + " is selected. Actual content: " + dataset);
    }
    return this;
  }

  public void click(int monthId, int day) {
    HistoDailyDataset dataset = getDataset();
    int monthIndex = dataset.getIndex(monthId);
    HistoChart chart = getChart();
    HistoSelectionManager selectionManager = chart.getSelectionManager();
    selectionManager.updateRollover(monthIndex, Collections.singleton(Key.create(Day.MONTH, monthId, Day.DAY, day - 1)), false, false, new Point(0, 0));
    selectionManager.startClick(false, new Point(0,0));
  }

  public HistoDailyChecker checkValue(int monthId, int dayId, double expectedValue) {
    HistoDailyDataset dataset = getDataset();
    int monthIndex = dataset.getIndex(monthId);
    if (monthIndex < 0) {
      Assert.fail("Month " + monthId + " not found");
    }
    Double actual = dataset.getValue(monthIndex, dayId - 1);
    if (!Amounts.equal(actual, expectedValue)) {
      Assert.fail("Error for " + toString(monthId, dayId) +
                  " - was " + actual + " instead of " + expectedValue +
                  "\nDataset content:\n" + dataset);
    }
    return this;
  }

  public HistoDailyChecker checkIsPastOnly(int monthId) {
    HistoDailyDataset dataset = getDataset();
    int monthIndex = dataset.getIndex(monthId);
    if (dataset.isCurrent(monthIndex)) {
      Assert.fail("Month " + monthId + " is current - dataset:\n" + dataset);
    }
    if (dataset.isFuture(monthIndex)) {
      Assert.fail("Month " + monthId + " is future - dataset:\n" + dataset);
    }
    return this;
  }

  public HistoDailyChecker checkCurrentDay(int monthId, int dayId) {
    HistoDailyDataset dataset = getDataset();
    int dayIndex = dayId - 1;
    int monthIndex = dataset.getIndex(monthId);
    if (monthIndex < 0) {
      Assert.fail("No month " + monthId + " found - dataset:\n" + dataset);
    }
    if (!dataset.isCurrent(monthIndex, dayIndex)) {
      Assert.fail(toString(monthId, dayId) + " not current - dataset:\n" + dataset);
    }
    if (dataset.isFuture(monthIndex, dayIndex)) {
      Assert.fail(toString(monthId, dayId) + " is current, should not be future - dataset:\n" + dataset);
    }
    if (!dataset.isFuture(monthIndex, dayIndex + 1)) {
      Assert.fail(toString(monthId, dayId + 1) + " not future - dataset:\n" + dataset);
    }
    return this;
  }

  public HistoDailyChecker checkCurrentDay(int monthId, int dayId, String label) {
    checkCurrentDay(monthId, dayId);
    Assert.assertEquals(label, getDataset().getCurrentDayLabel());
    return this;
  }

  public void checkTooltip(int monthId, int dayId, String expected) {
    String actual = getTooltip(monthId, dayId);
    Assert.assertEquals(expected, actual);
  }

  public void checkTooltipContains(int monthId, int dayId, String text) {
    String actual = getTooltip(monthId, dayId);
    if (!actual.contains(text)) {
      Assert.fail("'" + text + "' not found in:\n" + actual);
    }
  }

  private String getTooltip(int monthId, int dayId) {
    HistoDailyDataset dataset = getDataset();
    int monthIndex = dataset.getIndex(monthId);
    if (monthIndex < 0) {
      Assert.fail("Month " + monthId + " not found");
    }
    return dataset.getTooltip(monthIndex,
                              Collections.singleton(Key.create(Day.MONTH, monthId, Day.DAY, dayId - 1)));
  }

  public HistoDailyDataset getDataset() {
    return getDataset(HistoDailyDataset.class);
  }

  public void dumpCode() {
    StringBuilder builder = new StringBuilder();
    HistoDailyDataset dataset = getDataset();
    Double value = null;
    for (int monthIndex = 0; monthIndex < dataset.size(); monthIndex++) {
      int monthId = dataset.getId(monthIndex);
      Double[] values = dataset.getValues(monthIndex);
      for (int dayIndex = 0; dayIndex < values.length; dayIndex++) {
        if (value == null || (!value.equals(values[dayIndex]))) {
          value = values[dayIndex];
          builder
            .append("  .checkValue(").append(monthId).append(", ").append(dayIndex + 1).append(", ")
            .append(toString(value))
            .append(")\n");
        }
      }
    }
    Assert.fail("Chart content:\n" + builder.toString());

  }

  public void doubleClick() {
    Mouse.doubleClick(new Panel(getChart()));
  }

  public HistoChart getChart() {
    return (HistoChart)chartPanel.getAwtComponent();
  }

  protected Panel getPanel() {
    return chartPanel;
  }

  protected String getName() {
    return chartName;
  }
}
