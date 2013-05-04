package org.designup.picsou.gui.components.charts.histo;

import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.utils.directory.Directory;

import java.awt.*;

public class HistoChartColors implements ColorChangeListener, Disposable {
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

  private String prefix;
  private Directory directory;

  public HistoChartColors(Directory directory) {
    this("histo", directory);
  }

  public HistoChartColors(String prefix, Directory directory) {
    this.prefix = prefix;
    this.directory = directory;
    directory.get(ColorService.class).addListener(this);
  }

  public void dispose() {
    directory.get(ColorService.class).removeListener(this);
    directory = null;
  }

  public void colorsChanged(ColorLocator colorLocator) {
    this.chartBgColor = colorLocator.get(prefix + ".chart.bg");
    this.chartBorderColor = colorLocator.get(prefix + ".chart.border");
    this.scaleLineColor = colorLocator.get(prefix + ".scale.line");
    this.scaleOriginLineColor = colorLocator.get(prefix + ".scale.line.origin");
    this.scaleTextColor = colorLocator.get(prefix + ".scale.text");
    this.sectionLineColor = colorLocator.get(prefix + ".section.line");
    this.labelColor = colorLocator.get(prefix + ".label");
    this.selectedLabelColor = colorLocator.get(prefix + ".label.selected");
    this.rolloverLabelColor = colorLocator.get(prefix + ".label.rollover");
    this.selectedColumnColor = colorLocator.get(prefix + ".selection.bg");
    this.selectedLabelBackgroundColor = colorLocator.get(prefix + ".selection.label.bg");
    this.selectedColumnBorder = colorLocator.get(prefix + ".selection.border");
    this.columnDividerColor = colorLocator.get(prefix + ".column.divider");
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
