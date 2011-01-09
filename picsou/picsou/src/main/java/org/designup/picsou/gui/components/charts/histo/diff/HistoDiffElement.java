package org.designup.picsou.gui.components.charts.histo.diff;

import org.designup.picsou.gui.components.charts.histo.utils.HistoDatasetElement;

public class HistoDiffElement extends HistoDatasetElement {
  public final double referenceValue;
  public final double actualValue;
  public final boolean future;

  HistoDiffElement(int id,
                   String label, String tooltip, String section,
                   double referenceValue, double actualValue,
                   boolean current, boolean selected, boolean future) {
    super(id, label, tooltip, section, current, selected);
    this.referenceValue = referenceValue;
    this.actualValue = actualValue;
    this.future = future;
  }

  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder
      .append(label)
      .append(" - ")
      .append(referenceValue)
      .append(" / ")
      .append(actualValue);
    if (selected) {
      builder.append(" - selected");
    }
    if (future) {
      builder.append(" - future");
    }
    return builder.toString();
  }
}
