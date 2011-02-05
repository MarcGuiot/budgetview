package org.designup.picsou.gui.series.evolution.histobuilders;

import org.designup.picsou.gui.components.charts.histo.HistoChartConfig;

public class HistoChartBuilderConfig extends HistoChartConfig {

  public final int monthsBack;
  public final int monthsLater;
  public final boolean centerOnSelection;

  public HistoChartBuilderConfig(boolean drawLabels, boolean drawSections, boolean drawInnerLabels, boolean clickable,
                                 int monthsBack, int monthsLater, boolean centerOnSelection) {
    super(drawLabels, drawSections, drawInnerLabels, clickable);
    this.monthsBack = monthsBack;
    this.monthsLater = monthsLater;
    this.centerOnSelection = centerOnSelection;
  }
}
