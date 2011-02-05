package org.designup.picsou.gui.components.charts.histo.utils;

public class HistoDatasetElement {
  public final int id;
  public final String label;
  public final String tooltip;
  public final String section;
  public final boolean current;
  public final boolean future;
  public final boolean selected;

  public HistoDatasetElement(int id, String label, String tooltip, String section,
                             boolean current, boolean future, boolean selected) {
    this.id = id;
    this.label = label;
    this.tooltip = tooltip;
    this.section = section;
    this.current = current;
    this.future = future;
    this.selected = selected;
  }

  public String toString() {
    return label + " / " + section +
           (current ? "current" : "") + ":" +
           (future ? "future" : "") + ":" +
           (selected ? "selected" : "");
  }
}

