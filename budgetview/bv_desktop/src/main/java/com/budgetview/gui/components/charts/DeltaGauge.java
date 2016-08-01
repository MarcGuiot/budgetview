package com.budgetview.gui.components.charts;

import com.budgetview.gui.components.ActionablePanel;
import com.budgetview.shared.utils.Amounts;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;

import static java.awt.geom.AffineTransform.getTranslateInstance;

public class DeltaGauge extends ActionablePanel {

  private Double previousValue;
  private Double newValue;

  private double ratio;
  private Evolution evolution;
  private boolean active;
  private boolean noPreviousValue = false;

  private Color positiveColor = Color.GREEN;
  private Color neutralColor = Color.WHITE.darker();
  private Color negativeColor = Color.RED;
  private Color rolloverColor = Color.BLUE;
  private Color noPreviousValueColor = Color.GRAY;

  public enum Evolution {
    BETTER,
    WORSE,
    FLAT
  }

  public DeltaGauge() {
  }

  public void setValues(Double previousValue, Double newValue) {
    this.previousValue = previousValue;
    this.newValue = newValue;
    updateState();
    repaint();
  }

  public Double getPreviousValue() {
    return previousValue;
  }

  public Double getNewValue() {
    return newValue;
  }

  private void updateState() {
    if (newValue == null) {
      active = false;
      ratio = 0;
      return;
    }

    active = true;
    noPreviousValue = (previousValue == null) && Amounts.isNotZero(newValue);
    if (newValue.equals(previousValue)) {
      ratio = 0;
      evolution = Evolution.FLAT;
    }
    else if (Amounts.isNullOrZero(previousValue) && (newValue == 0)) {
      ratio = 0.0;
      evolution = Evolution.FLAT;
    }
    else if (Amounts.isNullOrZero(previousValue) && (newValue > 0)) {
      ratio = +1;
      evolution = Evolution.BETTER;
    }
    else if (Amounts.isNullOrZero(previousValue) && (newValue < 0)) {
      ratio = +1;
      evolution = Evolution.WORSE;
    }
    else if ((previousValue < 0) && Amounts.isNullOrZero(newValue)) {
      ratio = -1;
      evolution = Evolution.BETTER;
    }
    else if ((previousValue > 0) && Amounts.isNullOrZero(newValue)) {
      ratio = -1;
      evolution = Evolution.WORSE;
    }
    else if ((previousValue < 0) && (newValue > 0)) {
      ratio = +1;
      evolution = Evolution.BETTER;
    }
    else if ((previousValue > 0) && (newValue < 0)) {
      ratio = -1;
      evolution = Evolution.WORSE;
    }
    else {
      double delta = newValue - previousValue;
      evolution = delta > 0 ? Evolution.BETTER : Evolution.WORSE;
      if (newValue / previousValue >= 2) {
        ratio = 1;
      }
      else {
        ratio = newValue / previousValue - 1;
      }
    }
  }

  private Color computeColor() {
    if (evolution == Evolution.FLAT) {
      return neutralColor;
    }
    if (noPreviousValue) {
      return noPreviousValueColor;
    }
    Color targetColor = evolution == Evolution.BETTER ? positiveColor : negativeColor;
    float[] source = neutralColor.getRGBColorComponents(null);
    float[] target = targetColor.getRGBColorComponents(null);
    float[] result = new float[source.length];
    for (int i = 0; i < result.length; i++) {
      result[i] = source[i] + (float)((target[i] - source[i]) * Math.abs(ratio));
    }
    return new Color(result[0], result[1], result[2]);
  }

  public double getRatio() {
    return ratio;
  }

  public Evolution getEvolution() {
    return evolution;
  }

  public boolean isActive() {
    return active;
  }

  public void paint(Graphics graphics) {

    Graphics2D g2 = (Graphics2D)graphics;
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    if (isOpaque()) {
      g2.setColor(getBackground());
      g2.fillRect(0, 0, getWidth(), getHeight());
    }

    if (!active) {
      return;
    }

    paintBorder(graphics);

    int width = getWidth() - 1;
    int height = getHeight() - 1;

    GeneralPath shape = createArrowShape();
    Rectangle initialRectangle = shape.getBounds();

    shape.transform(AffineTransform.getRotateInstance(-Math.PI * ratio / 2,
                                                      initialRectangle.width / 2,
                                                      initialRectangle.height / 2));

    AffineTransform scaling =
      AffineTransform.getScaleInstance(width / (float)initialRectangle.width,
                                       height / (float)initialRectangle.height);
    shape.transform(scaling);

    Rectangle newRectangle = shape.getBounds();
    shape.transform(getTranslateInstance(-newRectangle.x, -newRectangle.y));

    setForeground(computeColor());
    g2.setColor(getForeground());
    g2.fill(shape);

    if (isRolloverInProgress()) {
      g2.setColor(rolloverColor);
      g2.draw(shape);
    }
  }

  private GeneralPath createArrowShape() {
    GeneralPath shape = new GeneralPath();
    shape.moveTo(0, 0);
    shape.lineTo(10, 5);
    shape.lineTo(0, 10);
    shape.lineTo(3, 5);
    shape.closePath();
    return shape;
  }

  public void setPositiveColor(Color positiveColor) {
    this.positiveColor = positiveColor;
  }

  public void setNeutralColor(Color neutralColor) {
    this.neutralColor = neutralColor;
  }

  public void setNegativeColor(Color negativeColor) {
    this.negativeColor = negativeColor;
  }

  public void setNoPreviousValueColor(Color noPreviousValueColor) {
    this.noPreviousValueColor = noPreviousValueColor;
  }
}
