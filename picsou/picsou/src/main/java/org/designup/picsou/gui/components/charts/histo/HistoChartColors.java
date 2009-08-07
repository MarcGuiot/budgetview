package org.designup.picsou.gui.components.charts.histo;

import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.utils.directory.Directory;

import java.awt.*;

public class HistoChartColors implements ColorChangeListener {
  private Color chartBgColor;
  private Color chartBorderColor;
  private Color scaleLineColor;
  private Color scaleTextColor;
  private Color labelColor;
  private Color selectedLabelColor;

  public HistoChartColors(Directory directory) {
    directory.get(ColorService.class).addListener(this);
  }

  public void colorsChanged(ColorLocator colorLocator) {
    this.chartBgColor = colorLocator.get("histo.chart.bg");
    this.chartBorderColor = colorLocator.get("histo.chart.border");
    this.scaleLineColor = colorLocator.get("histo.scale.line");
    this.scaleTextColor = colorLocator.get("histo.scale.text");
    this.labelColor = colorLocator.get("histo.label");
    this.selectedLabelColor = colorLocator.get("histo.label.selected");
  }

  public Color getChartBgColor() {
    return chartBgColor;
  }

  public Color getChartBorderColor() {
    return chartBorderColor;
  }

  public Color getScaleLineColor() {
    return scaleLineColor;
  }

  public Color getScaleTextColor() {
    return scaleTextColor;
  }

  public Color getLabelColor() {
    return labelColor;
  }

  public Color getSelectedLabelColor() {
    return selectedLabelColor;
  }
}
