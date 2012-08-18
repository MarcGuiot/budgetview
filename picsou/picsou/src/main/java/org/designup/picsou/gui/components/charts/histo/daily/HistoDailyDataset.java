package org.designup.picsou.gui.components.charts.histo.daily;

import org.designup.picsou.gui.components.charts.histo.utils.AbstractHistoDataset;
import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.model.Day;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.Key;

import java.util.Set;

public class HistoDailyDataset extends AbstractHistoDataset<HistoDailyElement> {

  private Integer currentMonthId;
  private Integer currentDayId;
  private String currentDayLabel;

  public HistoDailyDataset(String tooltipKey, Integer currentMonthId, Integer currentDayId, String currentDayLabel) {
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

  public String getTooltip(int index, Set<Key> objectKeys) {
    if ((index < 0) || (index >= size()) || (objectKeys.isEmpty())) {
      return "";
    }

    Key objectKey = objectKeys.iterator().next();
    return Lang.get(getTooltipKey(),
                    Day.getFullLabel(objectKey),
                    Formatting.toStandardValueString(getValue(index, objectKey.get(Day.DAY) - 1)));
  }

  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("daily(").append(currentMonthId).append(currentDayId).append(")\n");
    for (HistoDailyElement element : this.elements) {
      builder.append("- ").append(element.id).append('\n');
      for (int dayIndex = 0; dayIndex < element.values.length; dayIndex++) {
        builder.append("    [").append(Formatting.TWO_DIGIT_INTEGER_FORMAT.format(dayIndex))
          .append("] ").append(element.values[dayIndex]);
        if (element.daySelections[dayIndex]) {
          builder.append('*');
        }
        if (dayIndex == element.minDay) {
          builder.append(" min");
        }
        builder.append("\n");
      }
    }
    return builder.toString();
  }

  public String toString(int monthIndex) {
    return getElement(monthIndex).toString();
  }

  public Key getKey(int monthIndex, int dayIndex) {
    return Key.create(Day.MONTH, getElement(monthIndex).id, Day.DAY, dayIndex + 1);
  }
}
