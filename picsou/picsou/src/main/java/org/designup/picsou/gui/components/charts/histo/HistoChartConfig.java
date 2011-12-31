package org.designup.picsou.gui.components.charts.histo;

public class HistoChartConfig {
  public final boolean drawLabels;
  public final boolean drawSections;
  public final boolean drawInnerLabels;
  public final boolean drawScale;
  public final boolean keepScaleZone;
  public final boolean drawColumnDividers;
  public final boolean columnClickEnabled;
  public final boolean objectClickEnabled;
  public final boolean drawInnerAnnotations;

  public HistoChartConfig(boolean drawLabels,
                          boolean drawSections,
                          boolean drawInnerLabels,
                          boolean drawScale,
                          boolean keepScaleZone,
                          boolean drawColumnDividers,
                          boolean columnClickEnabled,
                          boolean objectClickEnabled,
                          boolean drawInnerAnnotations) {
    this.drawLabels = drawLabels;
    this.drawSections = drawSections;
    this.drawInnerLabels = drawInnerLabels;
    this.drawScale = drawScale;
    this.keepScaleZone = keepScaleZone;
    this.drawColumnDividers = drawColumnDividers;
    this.columnClickEnabled = columnClickEnabled;
    this.objectClickEnabled = objectClickEnabled;
    this.drawInnerAnnotations = drawInnerAnnotations;
  }
}
