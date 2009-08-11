package org.designup.picsou.gui.components.charts.stack;

import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.utils.directory.Directory;

import java.awt.*;

public class StackChartColors implements ColorChangeListener {

  private String leftBarKey;
  private String rightBarKey;
  private String barTextKey;
  private String labelKey;
  private String borderKey;
  private String selectionBorderKey;
  private String selectionTextKey;

  private Color leftBarColor;
  private Color rightBarColor;
  private Color barTextColor;
  private Color labelColor;
  private Color borderColor;
  private Color selectionBorderColor;
  private Color selectionTextColor;

  public StackChartColors(String leftBarKey,
                          String rightBarKey,
                          String barTextKey,
                          String labelKey,
                          String borderKey,
                          String selectionBorderKey,
                          String selectionTextKey,
                          Directory directory) {
    this.leftBarKey = leftBarKey;
    this.rightBarKey = rightBarKey;
    this.barTextKey = barTextKey;
    this.labelKey = labelKey;
    this.borderKey = borderKey;
    this.selectionBorderKey = selectionBorderKey;
    this.selectionTextKey = selectionTextKey;

    directory.get(ColorService.class).addListener(this);
  }

  public void colorsChanged(ColorLocator colorLocator) {
    leftBarColor = colorLocator.get(leftBarKey);
    rightBarColor = colorLocator.get(rightBarKey);
    barTextColor = colorLocator.get(barTextKey);
    labelColor = colorLocator.get(labelKey);
    borderColor = colorLocator.get(borderKey);
    selectionBorderColor = colorLocator.get(selectionBorderKey);
    selectionTextColor = colorLocator.get(selectionTextKey);
  }

  public Color getLeftBarColor() {
    return leftBarColor;
  }

  public Color getRightBarColor() {
    return rightBarColor;
  }

  public Color getBarTextColor() {
    return barTextColor;
  }

  public Color getLabelColor() {
    return labelColor;
  }

  public Color getBorderColor() {
    return borderColor;
  }

  public Color getSelectionBorderColor() {
    return selectionBorderColor;
  }

  public Color getSelectionTextColor() {
    return selectionTextColor;
  }
}
