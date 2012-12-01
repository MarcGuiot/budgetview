package org.designup.picsou.gui.components.charts.stack;

import org.globsframework.model.Key;

public class StackChartBlock {

  public Key key;

  public final int blockY;
  public final int blockHeight;
  public final String label;
  public final String barText;
  public final int labelTextY;
  public final int barTextY;
  public final String tooltipText;
  public final boolean selected;

  public StackChartBlock(Key key,
                         int blockY, int blockHeight,
                         String label, String barText,
                         int labelTextY, int barTextY,
                         boolean selected,
                         String tooltipText) {
    this.key = key;

    this.blockY = blockY;
    this.blockHeight = blockHeight;
    this.label = label;
    this.barText = barText;
    this.labelTextY = labelTextY;
    this.barTextY = barTextY;

    this.selected = selected;
    this.tooltipText = tooltipText;
  }
}
