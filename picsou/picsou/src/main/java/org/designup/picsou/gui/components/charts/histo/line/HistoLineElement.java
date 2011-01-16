package org.designup.picsou.gui.components.charts.histo.line;

import org.designup.picsou.gui.components.charts.histo.utils.HistoDatasetElement;

public class HistoLineElement extends HistoDatasetElement {
  public final double value;

  HistoLineElement(int id, String label, String tooltip, String section, double value,
                   boolean current, boolean future, boolean selected) {
    super(id, label, tooltip, section, current, future, selected);
    this.value = value;
  }
}
