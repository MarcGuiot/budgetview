package org.designup.picsou.gui.components.charts.histo;

import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.utils.directory.Directory;

import java.awt.*;

public class HistoChartColors implements ColorChangeListener {
  private static final BasicStroke VERTICAL_DIVIDER_STROKE = new BasicStroke(2.0f);
  private Color chartBgColor;
  private Color chartBorderColor;
  private Color scaleLineColor;
  private Color scaleOriginLineColor;
  private Color scaleTextColor;
  private Color labelColor;
  private Color selectedLabelColor;
  private Color rolloverLabelColor;
  private Color sectionLineColor;
  private Color selectedColumnColor;
  private Color selectedLabelBackgroundColor;
  private Color selectedColumnBorder;
  private Color columnDividerColor;

  public HistoChartColors(Directory directory) {
    directory.get(ColorService.class).addListener(this);
  }

  public void colorsChanged(ColorLocator colorLocator) {
    this.chartBgColor = colorLocator.get("histo.chart.bg");
    this.chartBorderColor = colorLocator.get("histo.chart.border");
    this.scaleLineColor = colorLocator.get("histo.scale.line");
    this.scaleOriginLineColor = colorLocator.get("histo.scale.line.origin");
    this.scaleTextColor = colorLocator.get("histo.scale.text");
    this.sectionLineColor = colorLocator.get("histo.section.line");
    this.labelColor = colorLocator.get("histo.label");
    this.selectedLabelColor = colorLocator.get("histo.label.selected");
    this.rolloverLabelColor = colorLocator.get("histo.label.rollover");
    this.selectedColumnColor = colorLocator.get("histo.selection.bg");
    this.selectedLabelBackgroundColor = colorLocator.get("histo.selection.label.bg");
    this.selectedColumnBorder = colorLocator.get("histo.selection.border");
    this.columnDividerColor = colorLocator.get("histo.column.divider");
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

  public Color getScaleOriginLineColor() {
    return scaleOriginLineColor;
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

  public Color getRolloverLabelColor() {
    return rolloverLabelColor;
  }

  public Color getSectionLineColor() {
    return sectionLineColor;
  }

  public Color getSelectedColumnColor() {
    return selectedColumnColor;
  }

  public Color getSelectedLabelBackgroundColor() {
    return selectedLabelBackgroundColor;
  }

  public Color getSelectedColumnBorder() {
    return selectedColumnBorder;
  }

  public void setColumnDividerStyle(Graphics2D g2) {
    g2.setStroke(VERTICAL_DIVIDER_STROKE);
    g2.setComposite(AlphaComposite.Src);
    g2.setColor(columnDividerColor);
  }
}
