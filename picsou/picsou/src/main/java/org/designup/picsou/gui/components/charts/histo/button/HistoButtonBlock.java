package org.designup.picsou.gui.components.charts.histo.button;

import org.globsframework.model.Key;

public class HistoButtonBlock {
  public final int minIndex;
  public final int maxIndex;
  public final int row;
  public final boolean truncatedMin;
  public final boolean truncatedMax;
  public final String label;
  public final Key key;

  public HistoButtonBlock(int minIndex, int maxIndex, int row,
                          boolean truncatedMin, boolean truncatedMax,
                          String label, Key key) {
    this.minIndex = minIndex;
    this.maxIndex = maxIndex;
    this.row = row;
    this.truncatedMin = truncatedMin;
    this.truncatedMax = truncatedMax;
    this.label = label;
    this.key = key;
  }
}
