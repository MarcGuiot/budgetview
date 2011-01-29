package org.designup.picsou.gui.components.charts.histo.daily;

import org.designup.picsou.gui.components.charts.histo.utils.HistoDatasetElement;

import java.util.Arrays;

public class HistoDailyElement extends HistoDatasetElement {

  public Double[] values;

  public HistoDailyElement(int id, Double[] values, String label, String monthLabel, String section, boolean current, boolean future, boolean selected) {
    super(id, label, monthLabel, section, current, future, selected);
    this.values = values;
  }

  public Double getLastValue() {
    return values[values.length - 1];
  }

  public String toString() {
    return id + "==>"  + Arrays.toString(values);
  }
}
