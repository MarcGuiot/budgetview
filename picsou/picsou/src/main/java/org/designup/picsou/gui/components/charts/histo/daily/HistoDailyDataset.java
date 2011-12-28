package org.designup.picsou.gui.components.charts.histo.daily;

import org.designup.picsou.gui.components.charts.histo.utils.AbstractHistoDataset;
import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.model.Day;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.Key;

public class HistoDailyDataset extends AbstractHistoDataset<HistoDailyElement> {

  private Integer currentMonth;
  private Integer currentDay;

  public HistoDailyDataset(String tooltipKey, Integer currentMonth, Integer currentDay) {
    super(tooltipKey);
    this.currentMonth = currentMonth;
    this.currentDay = currentDay;
  }

  public void add(int monthId, Double[] values, String label, String section, boolean current, boolean selected, boolean[] daySelections) {
    add(new HistoDailyElement(monthId, values, label, section, "", findMinDay(values), current, monthId > currentMonth, selected, daySelections));
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

  public Double[] getValues(int index) {
    return getElement(index).values;
  }

  public Double getValue(int index, int dayIndex) {
    return getElement(index).values[dayIndex];
  }

  public int getMinDay(int index) {
    return getElement(index).minDay;
  }

  public Double getLastValue(int index) {
    return getElement(index).getLastValue();
  }

  public boolean isCurrent(int index, int day) {
    int month = getElement(index).id;
    return month == currentMonth && day + 1 == currentDay;
  }

  public boolean isFuture(int index, int day) {
    int month = getElement(index).id;
    if (month > currentMonth) {
      return true;
    }
    else if (month < currentMonth) {
      return false;
    }
    return day + 1 > currentDay;
  }

  public boolean isDaySelected(int index, int day) {
    return getElement(index).daySelections[day];
  }

  public String getTooltip(int index, Key objectKey) {
    if ((index < 0) || (index >= size()) || (objectKey == null)) {
      return "";
    }

    return Lang.get(getTooltipKey(),
                    Day.getFullLabel(objectKey),
                    Formatting.toStandardValueString(getValue(index, objectKey.get(Day.DAY) - 1)));
  }

  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("daily(").append(currentMonth).append(currentDay).append(")\n");
    for (HistoDailyElement element : this.elements) {
      builder.append("- ").append(element.id).append('\n');
      for (int i = 0; i < element.values.length; i++) {
        builder.append("    ").append(Formatting.TWO_DIGIT_INTEGER_FORMAT.format(i))
          .append(':').append(element.values[i]);
        if (element.daySelections[i]) {
          builder.append('*');
        }
        if (i == element.minDay) {
          builder.append(" min");
        }
        builder.append("\n");
      }
    }
    return builder.toString();
  }

  public String toString(int index) {
    return getElement(index).toString();
  }

  public Key getKey(int index, int dayIndex) {
    return Key.create(Day.MONTH, getElement(index).id, Day.DAY, dayIndex + 1);
  }
}
