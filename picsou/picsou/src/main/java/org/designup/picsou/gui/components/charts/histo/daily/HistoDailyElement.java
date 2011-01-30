package org.designup.picsou.gui.components.charts.histo.daily;

import org.designup.picsou.gui.components.charts.histo.utils.HistoDatasetElement;

import java.util.Arrays;

public class HistoDailyElement extends HistoDatasetElement {

  public final Double[] values;
  public final int minDay;

  public HistoDailyElement(int id, Double[] values, String label, String monthLabel, String section, boolean current, boolean future, boolean selected) {
    super(id, label, monthLabel, section, current, future, selected);
    this.values = values;
    this.minDay = findMinDay();
  }

  private int findMinDay() {
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

  public Double getLastValue() {
    return values[values.length - 1];
  }

  public String toString() {
    return id + "==>"  + Arrays.toString(values);
  }
}
