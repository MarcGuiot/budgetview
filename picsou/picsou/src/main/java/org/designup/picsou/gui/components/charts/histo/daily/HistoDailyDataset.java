package org.designup.picsou.gui.components.charts.histo.daily;

import org.designup.picsou.gui.components.charts.histo.utils.AbstractHistoDataset;
import org.designup.picsou.utils.Lang;

public class HistoDailyDataset extends AbstractHistoDataset<HistoDailyElement> {

  private Integer currentMonth;
  private Integer currentDay;

  public HistoDailyDataset(String tooltipKey, Integer currentMonth, Integer currentDay) {
    super(tooltipKey);
    this.currentMonth = currentMonth;
    this.currentDay = currentDay;
  }

  public void add(int monthId, Double[] values, String label, String tooltip, String section, boolean current, boolean selected) {
    add(new HistoDailyElement(monthId, values, label, tooltip, section, current, monthId > currentMonth, selected));
    updateMax(values);
  }

  private void updateMax(Double[] values) {
    for (Double value : values) {
      if (value != null) {
        updateMax(value);
      }
    }
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

  public Double[] getValues(int index) {
    return getElement(index).values;
  }

  public String getTooltip(int index) {
    if ((index < 0) || (index >= size())) {
      return "";
    }
    return Lang.get(getTooltipKey(), getElement(index).tooltip);
  }

  public Double getLastValue(int index) {
    return getElement(index).getLastValue();
  }

  public String toString() {
    return "daily(" + currentMonth + " / " + currentDay + ")\n" + super.toString();
  }

  public String toString(int index) {
    return getElement(index).toString();
  }
}
