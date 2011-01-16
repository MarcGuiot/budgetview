package org.designup.picsou.gui.components.charts.histo.daily;

import org.designup.picsou.gui.components.charts.histo.utils.HistoDatasetElement;

public class HistoDailyElement extends HistoDatasetElement {

  public Double[] values;

  public HistoDailyElement(int id, Double[] values, String label, String monthLabel, String section, boolean current, boolean future, boolean selected) {
    super(id, label, monthLabel, section, current, future, selected);
    this.values = values;
  }
}
