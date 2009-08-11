package org.designup.picsou.gui.components.charts.stack;

public class StackChartBlock {
  public final int blockY;
  public final int blockHeight;
  public final String label;
  public final String barText;
  public final int labelTextY;
  public final int barTextY;
  public final boolean selected;

  public StackChartBlock(int blockY, int blockHeight,
                         String label, String barText,
                         int labelTextY, int barTextY,
                         boolean selected) {
    this.blockY = blockY;
    this.blockHeight = blockHeight;
    this.label = label;
    this.barText = barText;
    this.labelTextY = labelTextY;
    this.barTextY = barTextY;
    this.selected = selected;
  }
}
