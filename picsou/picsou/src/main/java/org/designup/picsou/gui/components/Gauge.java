package org.designup.picsou.gui.components;

import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.utils.Lang;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;

public class Gauge extends JPanel {

  private final boolean overrunIsAnError;
  private final boolean showWarningForErrors;
  private final boolean invertedSignIsAnError;

  private double actualValue;
  private double targetValue;
  private double overrunPart = 0;

  private double fillPercent = 0;
  private double overrunPercent = 0;
  private double emptyPercent = 1.0;
  private boolean overrunError = false;
  private boolean warningShown = false;

  private Color borderColor = Color.DARK_GRAY;
  private Color filledColorTop = Color.BLUE.brighter();
  private Color filledColorBottom = Color.BLUE.darker();
  private Color emptyColorTop = Color.LIGHT_GRAY;
  private Color emptyColorBottom = Color.LIGHT_GRAY.brighter();
  private Color overrunColorTop = Color.CYAN.brighter();
  private Color overrunColorBottom = Color.CYAN.darker();
  private Color overrunErrorColorTop = Color.RED.brighter();
  private Color overrunErrorColorBottom = Color.RED.darker();
  private Color triangleTopColor = Color.YELLOW.brighter();
  private Color triangleBottomColor = Color.YELLOW.darker();
  private Color triangleBorderColor = Color.LIGHT_GRAY;
  private Color triangleShadowColor = Color.DARK_GRAY;
  private Color triangleSignColor = Color.DARK_GRAY;

  private static final int BAR_HEIGHT = 10;
  private static final float TRIANGLE_HEIGHT = 16f;
  private static final float TRIANGLE_WIDTH = 16f;

  public Gauge() {
    this(true, true, true);
  }

  public Gauge(boolean overrunIsAnError, boolean showWarningForErrors, boolean invertedSignIsAnError) {
    this.overrunIsAnError = overrunIsAnError;
    this.showWarningForErrors = showWarningForErrors;
    this.invertedSignIsAnError = invertedSignIsAnError;

    setMinimumSize(new Dimension(60, 28));
    setPreferredSize(new Dimension(200, 28));

    setToolTip("gauge.unset");
  }

  public void setValues(double actualValue, double targetValue) {
    this.actualValue = actualValue;
    this.targetValue = targetValue;
    updateValues();
    repaint();
  }

  public void setValues(double actualValue, double targetValue, double overrunPart) {
    if ((overrunPart < 10E-6) || (actualValue > targetValue)) {
      setValues(actualValue, targetValue);
      return;
    }

    this.actualValue = actualValue;
    this.targetValue = targetValue;
    this.overrunPart = overrunPart;

    double fillValue = actualValue - overrunPart;
    fillPercent = Math.abs(fillValue / targetValue);
    overrunPercent = Math.abs(overrunPart / targetValue);
    emptyPercent = 1 - overrunPercent - fillPercent;
    overrunError = overrunIsAnError;
    warningShown = overrunError && showWarningForErrors;
    double remainingValue = targetValue - actualValue;
    setToolTip("gauge.partial.overrun." + (overrunIsAnError ? "error" : "ok"),
               remainingValue, Math.abs(overrunPart));

    repaint();
  }

  private void updateValues() {
    boolean sameSign = Math.signum(actualValue) * Math.signum(targetValue) >= 0;
    double absActual = Math.abs(actualValue);
    double absTarget = Math.abs(targetValue);

    if ((absTarget == 0) && (absActual == 0)) {
      fillPercent = 0;
      overrunPercent = 0;
      emptyPercent = 1;
      overrunError = false;
      warningShown = false;
      setToolTip("gauge.unset");
    }
    else if (absTarget == 0) {
      fillPercent = 0;
      overrunPercent = 1;
      emptyPercent = 0;
      overrunError = overrunIsAnError;
      warningShown = overrunError && showWarningForErrors;
      setToolTip("gauge.overrun." + (overrunIsAnError ? "error" : "ok"), absActual);
    }
    else if (!sameSign) {
      fillPercent = 0;
      overrunPercent = absActual / (absActual + absTarget);
      emptyPercent = 1 - overrunPercent;
      overrunError = invertedSignIsAnError;
      warningShown = overrunError && showWarningForErrors;
      setToolTip("gauge.inverted." + (invertedSignIsAnError ? "error" : "ok"), absActual);
    }
    else if (absActual - absTarget >= 0.01) {
      fillPercent = absTarget / absActual;
      overrunPercent = 1 - fillPercent;
      emptyPercent = 0;
      overrunError = overrunIsAnError;
      warningShown = overrunError && showWarningForErrors;
      setToolTip("gauge.overrun." + (overrunIsAnError ? "error" : "ok"), absActual - absTarget);
    }
    else if (Math.abs(absTarget - absActual) <= 0.01) {
      fillPercent = 1;
      overrunPercent = 0;
      emptyPercent = 0;
      overrunError = false;
      warningShown = false;
      setToolTip("gauge.complete");
    }
    else {
      fillPercent = absActual / absTarget;
      if (fillPercent > 1) {
        fillPercent = 1;
      }
      overrunPercent = 0;
      emptyPercent = 1 - fillPercent;
      overrunError = false;
      warningShown = false;
      setToolTip("gauge.partial", absTarget - absActual);
    }
  }

  private void setToolTip(String key, Double... values) {
    String[] formattedValues = new String[values.length];
    for (int i = 0; i < values.length; i++) {
      formattedValues[i] = Formatting.DECIMAL_FORMAT.format(values[i]);

    }
    this.setToolTipText(Lang.get(key, formattedValues));
  }

  public void paint(Graphics g) {
    Graphics2D g2 = (Graphics2D)g;
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    int width = getWidth() - 1;
    int height = getHeight() - 1;

    if (isOpaque()) {
      g2.setColor(getBackground());
      g2.fillRect(0, 0, width, height);
    }

    int barTop = (height - BAR_HEIGHT) / 2;
    int barBottom = height - barTop;

    int fillWidth = (int)(width * fillPercent);
    int emptyWidth = (int)(width * emptyPercent);

    int overrunWidth = width - fillWidth - emptyWidth;
    int overrunEnd = fillWidth + overrunWidth;

    if (fillPercent > 0) {
      fillBar(g2, filledColorTop, filledColorBottom, 0, fillWidth, barTop, barBottom);
    }

    if (overrunPercent > 0) {
      if (overrunError) {
        fillBar(g2, overrunErrorColorTop, overrunErrorColorBottom, fillWidth, overrunWidth, barTop, barBottom);
      }
      else {
        fillBar(g2, overrunColorTop, overrunColorBottom, fillWidth, overrunWidth, barTop, barBottom);
      }
    }

    if (emptyPercent > 0) {
      fillBar(g2, emptyColorTop, emptyColorBottom, overrunEnd, emptyWidth, barTop, barBottom);
    }

    drawBorder(g2, width, barTop, BAR_HEIGHT);
    if (warningShown) {
      drawWarning(g2, width, height);
    }
  }

  private void drawBorder(Graphics2D g2, int width, int barTop, int barHeight) {
    g2.setColor(borderColor);
    g2.drawRect(0, barTop, width, barHeight);
  }

  private void drawWarning(Graphics2D g2, int width, int height) {
    GeneralPath shape = createWarningShape();

    Rectangle rectangle = shape.getBounds();
    AffineTransform scaling =
      AffineTransform.getScaleInstance(TRIANGLE_WIDTH / (float)rectangle.width,
                                       TRIANGLE_HEIGHT / (float)rectangle.height);
    shape.transform(scaling);

    rectangle = shape.getBounds();
    float middleX = (float)width / 2.0f;
    float middleY = (float)height / 2;
    AffineTransform translation =
      AffineTransform.getTranslateInstance(middleX - TRIANGLE_WIDTH / 2.0f - rectangle.x,
                                           middleY - TRIANGLE_HEIGHT / 2 - rectangle.y);
    shape.transform(translation);

    shape.transform(AffineTransform.getTranslateInstance(2, 2));
    g2.setColor(triangleShadowColor);
    g2.fill(shape);

    shape.transform(AffineTransform.getTranslateInstance(-2, -2));
    g2.setPaint(new GradientPaint(0, 0, triangleTopColor, 0, height, triangleBottomColor));
    g2.fill(shape);

    g2.setColor(triangleBorderColor);
    g2.draw(shape);

    g2.setColor(triangleSignColor);
    GeneralPath sign = createWarningSign();
    sign.transform(scaling);
    sign.transform(translation);
    g2.fill(sign);
  }

  private GeneralPath createWarningShape() {
    GeneralPath shape = new GeneralPath();
    shape.moveTo(1, 9);
    shape.lineTo(5.5f, 1.0f);
    shape.curveTo(5.75f, 0.4f, 6.25f, 0.4f, 6.5f, 1.0f);
    shape.lineTo(11, 9);
    shape.curveTo(11.3f, 9.5f, 11.5f, 10.0f, 11f, 10f);
    shape.lineTo(1, 10);
    shape.moveTo(1, 9);
    shape.curveTo(0.7f, 9.5f, 0.5f, 10.0f, 1.0f, 10.0f);
    shape.closePath();
    return shape;
  }

  private GeneralPath createWarningSign() {
    GeneralPath shape = new GeneralPath();
    shape.moveTo(5, 4);
    shape.lineTo(6, 3);
    shape.lineTo(7, 4);
    shape.lineTo(6, 8);
    shape.closePath();

    shape.moveTo(5, 8.5f);
    shape.lineTo(6, 8.0f);
    shape.lineTo(7, 8.5f);
    shape.lineTo(6, 9.5f);
    shape.closePath();

    return shape;
  }

  private void fillBar(Graphics2D g2, Color topColor, Color bottomColor, int barX, int barWidth, int barTop, int barBottom) {
    g2.setPaint(new GradientPaint(0, barTop, topColor, 0, barBottom, bottomColor));
    g2.fillRect(barX, barTop, barWidth, BAR_HEIGHT);
  }

  public double getActualValue() {
    return actualValue;
  }

  public double getTargetValue() {
    return targetValue;
  }

  public double getOverrunPart() {
    return overrunPart;
  }

  public double getFillPercent() {
    return fillPercent;
  }

  public double getOverrunPercent() {
    return overrunPercent;
  }

  public double getEmptyPercent() {
    return emptyPercent;
  }

  public boolean isWarningShown() {
    return warningShown;
  }

  public boolean isOverrunErrorShown() {
    return overrunIsAnError && (overrunPercent > 0);
  }

  public void setBorderColor(Color borderColor) {
    this.borderColor = borderColor;
  }

  public void setFilledColorTop(Color filledColorTop) {
    this.filledColorTop = filledColorTop;
  }

  public void setFilledColorBottom(Color filledColorBottom) {
    this.filledColorBottom = filledColorBottom;
  }

  public void setEmptyColorTop(Color emptyColorTop) {
    this.emptyColorTop = emptyColorTop;
  }

  public void setEmptyColorBottom(Color emptyColorBottom) {
    this.emptyColorBottom = emptyColorBottom;
  }

  public void setOverrunColorTop(Color overrunColorTop) {
    this.overrunColorTop = overrunColorTop;
  }

  public void setOverrunColorBottom(Color overrunColorBottom) {
    this.overrunColorBottom = overrunColorBottom;
  }

  public void setOverrunErrorColorTop(Color overrunErrorColorTop) {
    this.overrunErrorColorTop = overrunErrorColorTop;
  }

  public void setOverrunErrorColorBottom(Color overrunErrorColorBottom) {
    this.overrunErrorColorBottom = overrunErrorColorBottom;
  }

  public void setTriangleTopColor(Color triangleTopColor) {
    this.triangleTopColor = triangleTopColor;
  }

  public void setTriangleBottomColor(Color triangleBottomColor) {
    this.triangleBottomColor = triangleBottomColor;
  }

  public void setTriangleBorderColor(Color triangleBorderColor) {
    this.triangleBorderColor = triangleBorderColor;
  }

  public void setTriangleShadowColor(Color triangleShadowColor) {
    this.triangleShadowColor = triangleShadowColor;
  }

  public void setTriangleSignColor(Color triangleSignColor) {
    this.triangleSignColor = triangleSignColor;
  }
}
