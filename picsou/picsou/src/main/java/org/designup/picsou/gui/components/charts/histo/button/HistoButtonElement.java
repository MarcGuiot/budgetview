package org.designup.picsou.gui.components.charts.histo.button;

import org.globsframework.model.Key;

public class HistoButtonElement implements Comparable<HistoButtonElement> {
  public final int minId;
  public final int maxId;
  public final String label;
  public final Key key;

  public HistoButtonElement(int minId, int maxId, String label, Key key) {
    this.minId = minId;
    this.maxId = maxId;
    this.label = label;
    this.key = key;
  }

  public int compareTo(HistoButtonElement other) {
    return Integer.signum(minId - other.minId);
  }

  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    HistoButtonElement that = (HistoButtonElement)o;

    if (key != null ? !key.equals(that.key) : that.key != null) {
      return false;
    }

    return true;
  }

  public int hashCode() {
    return key != null ? key.hashCode() : 0;
  }
}
