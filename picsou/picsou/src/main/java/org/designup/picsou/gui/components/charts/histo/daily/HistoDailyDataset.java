package org.designup.picsou.gui.components.charts.histo.daily;

import org.designup.picsou.gui.components.charts.histo.utils.AbstractHistoDataset;
import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.gui.utils.AmountColors;
import org.designup.picsou.model.Month;
import org.designup.picsou.utils.Lang;
import org.globsframework.utils.Utils;

public class HistoDailyDataset extends AbstractHistoDataset<HistoDailyElement> {

  private Integer currentMonth;
  private Integer currentDay;

  public HistoDailyDataset(String tooltipKey, Integer currentMonth, Integer currentDay) {
    super(tooltipKey);
    this.currentMonth = currentMonth;
    this.currentDay = currentDay;
  }

  public void add(int monthId, Double[] values, String label, String section, boolean current, boolean selected) {
    add(new HistoDailyElement(monthId, values, label, section, createTooltip(monthId, values), findMinDay(values), current, monthId > currentMonth, selected));
    updateMax(values);
  }

  private String createTooltip(int monthId, Double[] values) {
    Double minValue = Utils.min(values);
    return Lang.get(getTooltipKey(),
                    Month.getFullLabel(monthId),
                    Formatting.toMinimumValueString(minValue));
  }

  private int findMinDay(Double[] values) {
    int minDay = values.length / 2;
    Double minValue = values[minDay];
    for (int i = 0; i < values.length; i++) {
      Double value = values[i];
      if (value != null) {
        if ((minValue == null) || (value < minValue)) {
          minValue = value;
          minDay = i;
        }
      }
    }
    return minDay;
  }

  private void updateMax(Double[] values) {
    for (Double value : values) {
      if (value != null) {
        updateMax(value);
      }
    }
  }

  public Double[] getValues(int index) {
    return getElement(index).values;
  }

  public Double getValue(int index, int day) {
    return getElement(index).values[day];
  }

  public int getMinDay(int index) {
    return getElement(index).minDay;
  }

  public Double getLastValue(int index) {
    return getElement(index).getLastValue();
  }

  public boolean isCurrent(int index, int day) {
    int month = getElement(index).id;
    return month == currentMonth && day - 1 == currentDay;
  }

  public boolean isFuture(int index, int day) {
    int month = getElement(index).id;
    if (month > currentMonth) {
      return true;
    }
    else if (month < currentMonth) {
      return false;
    }
    return day - 1 > currentDay;
  }

  public boolean minInFuture(int index) {
    return (isFuture(index, getMinDay(index)));
  }

  public String getTooltip(int index) {
    if ((index < 0) || (index >= size())) {
      return "";
    }
    return Lang.get(getTooltipKey(), getElement(index).tooltip);
  }

  public String toString() {
    return "daily(" + currentMonth + " / " + currentDay + ")\n" + super.toString();
  }

  public String toString(int index) {
    return getElement(index).toString();
  }
}
