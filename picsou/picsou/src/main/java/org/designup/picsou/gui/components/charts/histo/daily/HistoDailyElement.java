package org.designup.picsou.gui.components.charts.histo.daily;

import org.designup.picsou.gui.components.charts.histo.utils.HistoDatasetElement;

import java.util.Arrays;

public class HistoDailyElement extends HistoDatasetElement {

  public final Double[] values;
  public final int minDay;

  public HistoDailyElement(int id, Double[] values, String label, String section, String tooltip, int minDay, boolean current, boolean future, boolean selected) {
    super(id, label, tooltip, section, current, future, selected);
    this.values = values;
    this.minDay = minDay;
  }

  public Double getLastValue() {
    return values[values.length - 1];
  }

  public String toString() {
    return id + "==>"  + Arrays.toString(values);
  }
}
