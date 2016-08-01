package com.budgetview.shared.gui.dailychart;

import com.budgetview.shared.gui.histochart.utils.AbstractHistoDataset;

public abstract class AbstractHistoDailyDataset extends AbstractHistoDataset<HistoDailyElement> {

  protected Integer currentMonthId;
  protected Integer currentDayId;
  protected String currentDayLabel;

  public AbstractHistoDailyDataset(String tooltipKey, Integer currentMonthId, Integer currentDayId, String currentDayLabel) {
    super(tooltipKey);
    this.currentMonthId = currentMonthId;
    this.currentDayId = currentDayId;
    this.currentDayLabel = currentDayLabel;
  }

  public void add(int monthId, Double[] values, String label, String section, boolean current, boolean selected, boolean[] daySelections) {
    add(new HistoDailyElement(monthId, values, label, section, "", findMinDay(values), current, monthId > currentMonthId, selected, daySelections));
    updateMax(values);
  }

  private int findMinDay(Double[] values) {
    if (values.length == 0) {
      return -1;
    }
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

  public Double[] getValues(int monthIndex) {
    return getElement(monthIndex).values;
  }

  public Double getValue(int monthIndex, int dayIndex) {
    return getElement(monthIndex).values[dayIndex];
  }

  public int getMinDay(int monthIndex) {
    return getElement(monthIndex).minDay;
  }

  public Double getLastValue(int monthIndex) {
    return getElement(monthIndex).getLastValue();
  }

  public boolean isCurrent(int monthIndex, int dayIndex) {
    int monthId = getElement(monthIndex).id;
    int dayId = dayIndex + 1;
    return monthId == currentMonthId && dayId == currentDayId;
  }

  public String getCurrentDayLabel() {
    return currentDayLabel;
  }

  public boolean isFuture(int monthIndex, int dayIndex) {
    int month = getElement(monthIndex).id;
    if (month > currentMonthId) {
      return true;
    }
    else if (month < currentMonthId) {
      return false;
    }
    int dayId = dayIndex + 1;
    return dayId > currentDayId;
  }

  public boolean isDaySelected(int monthIndex, int dayIndex) {
    return getElement(monthIndex).daySelections[dayIndex];
  }

}
