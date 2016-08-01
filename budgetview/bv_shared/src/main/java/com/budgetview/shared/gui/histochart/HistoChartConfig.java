package com.budgetview.shared.gui.histochart;

public class HistoChartConfig {
  public final boolean drawLabels;
  public final boolean drawSections;
  public final boolean drawInnerLabels;
  public final boolean drawScale;
  public final boolean keepScaleZone;
  public final boolean drawColumnDividers;
  public final boolean drawFuture;
  public final boolean columnClickEnabled;
  public final boolean objectClickEnabled;
  public final boolean drawInnerAnnotations;
  public boolean useWheelScroll;

  public HistoChartConfig(boolean drawLabels,
                          boolean drawSections,
                          boolean drawInnerLabels,
                          boolean drawScale,
                          boolean keepScaleZone,
                          boolean drawColumnDividers,
                          boolean drawInnerAnnotations,
                          boolean drawFuture,
                          boolean columnClickEnabled,
                          boolean objectClickEnabled) {
    this.drawLabels = drawLabels;
    this.drawSections = drawSections;
    this.drawInnerLabels = drawInnerLabels;
    this.drawScale = drawScale;
    this.keepScaleZone = keepScaleZone;
    this.drawColumnDividers = drawColumnDividers;
    this.drawFuture = drawFuture;
    this.columnClickEnabled = columnClickEnabled;
    this.objectClickEnabled = objectClickEnabled;
    this.drawInnerAnnotations = drawInnerAnnotations;
  }

  public void setUseWheelScroll(boolean useWheelScroll) {
    this.useWheelScroll = useWheelScroll;
  }
}
