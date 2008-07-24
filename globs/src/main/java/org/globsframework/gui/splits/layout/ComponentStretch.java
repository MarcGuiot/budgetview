package org.globsframework.gui.splits.layout;

import java.awt.*;

public class ComponentStretch {
  private Component component;
  private GridPos gridPos;
  private double weightX;
  private double weightY;
  private Fill fill;
  private Anchor anchor;
  private Insets insets;

  public ComponentStretch(Component component, Fill fill, Anchor anchor, double weightX, double weightY) {
    this(component, fill, anchor, weightX, weightY, null);
  }

  public ComponentStretch(Component component, Fill fill, Anchor anchor, double weightX, double weightY, Insets insets) {
    this.component = component;
    this.fill = fill;
    this.anchor = anchor;
    this.weightX = weightX;
    this.weightY = weightY;
    this.insets = insets;
  }

  public Component getComponent() {
    return component;
  }

  public void setComponent(Component component) {
    this.component = component;
  }

  public Fill getFill() {
    return fill;
  }

  public void setFill(Fill fill) {
    this.fill = fill;
  }

  public Anchor getAnchor() {
    return anchor;
  }

  public void setAnchor(Anchor anchor) {
    this.anchor = anchor;
  }

  public double getWeightX() {
    return weightX;
  }

  public void setWeightX(double weightX) {
    this.weightX = weightX;
  }

  public double getWeightY() {
    return weightY;
  }

  public void setWeightY(double weightY) {
    this.weightY = weightY;
  }

  public GridPos getGridPos() {
    return gridPos;
  }

  public void setGridPos(GridPos gridPos) {
    this.gridPos = gridPos;
  }

  public Insets getInsets() {
    return insets;
  }

  public void setInsets(Insets insets) {
    this.insets = insets;
  }

  public String toString() {
    return component.getClass().getSimpleName() + "[" + component.getName() + "] " +
           (gridPos != null ? gridPos + " " : "") +
           weightX + " " + weightY + " " + fill.name() + "/" + anchor.name();
  }
}

