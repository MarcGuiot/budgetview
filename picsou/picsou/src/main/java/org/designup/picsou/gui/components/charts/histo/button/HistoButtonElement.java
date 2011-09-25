package org.designup.picsou.gui.components.charts.histo.button;

import org.globsframework.model.Key;

public class HistoButtonElement implements Comparable<HistoButtonElement> {
  public final int minId;
  public final int maxId;
  public final String label;
  public final Key key;
  public final String tooltip;

  public HistoButtonElement(int minId, int maxId, String label, Key key, String tooltip) {
    this.minId = minId;
    this.maxId = maxId;
    this.label = label;
    this.key = key;
    this.tooltip = tooltip;
  }

  public int compareTo(HistoButtonElement other) {
    int minDiff = Integer.signum(minId - other.minId);
    if (minDiff != 0) {
      return minDiff;
    }

    int maxDiff = Integer.signum(maxId - other.maxId);
    if (maxDiff != 0) {
      return maxDiff;
    }

    int labelDiff = label.compareTo(other.label);
    if (labelDiff != 0) {
      return labelDiff;
    }

    return key.toString().compareTo(other.key.toString());
  }

}
