package org.designup.picsou.gui.components.charts.histo;

public class HistoChartConfig {
  public final boolean drawLabels;
  public final boolean drawSections;
  public final boolean drawInnerLabels;
  public final boolean clickable;

  public HistoChartConfig(boolean drawLabels, boolean drawSections, boolean drawInnerLabels, boolean clickable) {
    this.drawLabels = drawLabels;
    this.drawSections = drawSections;
    this.drawInnerLabels = drawInnerLabels;
    this.clickable = clickable;
  }
}
