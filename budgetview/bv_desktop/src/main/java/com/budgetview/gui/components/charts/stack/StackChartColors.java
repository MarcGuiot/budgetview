package com.budgetview.gui.components.charts.stack;

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
  private String floorKey;
  private String selectionBorderKey;
  private String rolloverTextKey;

  private Color leftBarColor;
  private Color rightBarColor;
  private Color barTextColor;
  private Color labelColor;
  private Color borderColor;
  private Color floorColor;
  private Color selectionBorderColor;
  private Color rolloverLabelColor;

  public StackChartColors(String leftBarKey,
                          String rightBarKey,
                          String barTextKey,
                          String labelKey,
                          String borderKey,
                          String floorKey,
                          String selectionBorderKey,
                          String rolloverTextKey,
                          Directory directory) {
    this.leftBarKey = leftBarKey;
    this.rightBarKey = rightBarKey;
    this.barTextKey = barTextKey;
    this.labelKey = labelKey;
    this.borderKey = borderKey;
    this.floorKey = floorKey;
    this.selectionBorderKey = selectionBorderKey;
    this.rolloverTextKey = rolloverTextKey;

    directory.get(ColorService.class).addListener(this);
  }

  public void colorsChanged(ColorLocator colorLocator) {
    leftBarColor = colorLocator.get(leftBarKey);
    rightBarColor = colorLocator.get(rightBarKey);
    barTextColor = colorLocator.get(barTextKey);
    labelColor = colorLocator.get(labelKey);
    floorColor = colorLocator.get(floorKey);
    borderColor = colorLocator.get(borderKey);
    selectionBorderColor = colorLocator.get(selectionBorderKey);
    rolloverLabelColor = colorLocator.get(rolloverTextKey);
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

  public Color getLabelColor(boolean rollover) {
    return rollover ? rolloverLabelColor : labelColor;
  }

  public Color getBorderColor() {
    return borderColor;
  }

  public Color getFloorColor() {
    return floorColor;
  }

  public Color getSelectionBorderColor() {
    return selectionBorderColor;
  }
}
