package org.designup.picsou.gui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;

public class Gauge extends JPanel {
  private double actualValue;
  private double targetValue;

  private Color borderColor = Color.DARK_GRAY;
  private Color filledColorTop = Color.BLUE.brighter();
  private Color filledColorBottom = Color.BLUE.darker();
  private Color emptyColorTop = Color.LIGHT_GRAY;
  private Color emptyColorBottom = Color.LIGHT_GRAY.brighter();
  private Color warningColorTop = Color.RED.brighter();
  private Color warningColorBottom = Color.RED.darker();
  private Color triangleTopColor = Color.YELLOW.brighter();
  private Color triangleBottomColor = Color.YELLOW.darker();
  private Color triangleBorderColor = Color.LIGHT_GRAY;
  private Color triangleShadowColor = Color.DARK_GRAY;
  private Color triangleSignColor = Color.DARK_GRAY;

  private static final int BAR_HEIGHT = 10;
  private static final float TRIANGLE_HEIGHT = 16f;
  private static final float TRIANGLE_WIDTH = 16f;

  public Gauge() {
    setMinimumSize(new Dimension(60, 28));
    setPreferredSize(new Dimension(200, 28));
  }

  public void paint(Graphics g) {
    Graphics2D g2 = (Graphics2D)g;
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    int width = getWidth() - 1;
    int height = getHeight() - 1;

    int barVerticalMargin = (height - BAR_HEIGHT) / 2;
    int barTop = barVerticalMargin;
    int barBottom = height - barVerticalMargin;
    
    float ratio = targetValue != 0 ? (float)actualValue / (float)targetValue : -1;

    if (ratio > 1) {
      fill(g2, warningColorTop, warningColorBottom, width, BAR_HEIGHT, barTop, barBottom);
      drawBorder(g2, width, barTop, BAR_HEIGHT);
      drawWarning(g2, width, height);
    }
    else if (ratio > 0) {
      fill(g2, emptyColorTop, emptyColorBottom, width, BAR_HEIGHT, barTop, barBottom);
      fill(g2, filledColorTop, filledColorBottom, (int)(width * ratio), BAR_HEIGHT, barTop, barBottom);
      drawBorder(g2, width, barTop, BAR_HEIGHT);
    }
    else if (ratio <= 0) {
      fill(g2, emptyColorTop, emptyColorBottom, width, BAR_HEIGHT, barTop, barBottom);
      drawBorder(g2, width, barTop, BAR_HEIGHT);
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

  private void fill(Graphics2D g2, Color topColor, Color bottomColor, int barWidth, int barHeight, int barTop, int barBottom) {
    g2.setPaint(new GradientPaint(0, barTop, topColor, 0, barBottom, bottomColor));
    g2.fillRect(0, barTop, barWidth, barHeight);
  }

  public void setActualValue(double actualValue) {
    this.actualValue = actualValue;
// TODO: REMOVE THIS - FOR GRAPHICAL TESTS ONLY
//    this.actualValue = Math.random();
// TODO: REMOVE THIS - FOR GRAPHICAL TESTS ONLY
  }

  public double getActualValue() {
    return actualValue;
  }

  public void setTargetValue(double targetValue) {
    this.targetValue = targetValue;
// TODO: REMOVE THIS - FOR GRAPHICAL TESTS ONLY
//    this.targetValue = Math.random() * 1.3;
// TODO: REMOVE THIS - FOR GRAPHICAL TESTS ONLY
  }

  public double getTargetValue() {
    return targetValue;
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

  public void setWarningColorTop(Color warningColorTop) {
    this.warningColorTop = warningColorTop;
  }

  public void setWarningColorBottom(Color warningColorBottom) {
    this.warningColorBottom = warningColorBottom;
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
