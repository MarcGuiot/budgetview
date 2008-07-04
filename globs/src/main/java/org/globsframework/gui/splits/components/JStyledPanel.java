package org.globsframework.gui.splits.components;

import org.globsframework.gui.splits.utils.Java2DUtils;

import javax.swing.*;
import java.awt.*;

public class JStyledPanel extends JPanel {
  private Color topColor = Color.WHITE;
  private Color bottomColor = Color.WHITE;
  private Color borderColor = Color.BLACK;

  private int borderWidth = 0;

  private int cornerRadius = 0;
  private int shadowWidth = 0;
  private int distance = 0;
  private float opacity = 0.5f;

  public JStyledPanel() {
  }

  public Color getBorderColor() {
    return borderColor;
  }

  public void setBorderColor(Color borderColor) {
    this.borderColor = borderColor;
  }

  public Color getTopColor() {
    return topColor;
  }

  public void setTopColor(Color topColor) {
    this.topColor = topColor;
  }

  public Color getBottomColor() {
    return bottomColor;
  }

  public void setBottomColor(Color bottomColor) {
    this.bottomColor = bottomColor;
  }

  public int getBorderWidth() {
    return borderWidth;
  }

  public void setBorderWidth(int borderWidth) {
    this.borderWidth = borderWidth;
  }

  public int getCornerRadius() {
    return cornerRadius;
  }

  public void setCornerRadius(int cornerRadius) {
    this.cornerRadius = cornerRadius;
  }

  public int getShadowWidth() {
    return shadowWidth;
  }

  public void setShadowWidth(int shadowWidth) {
    this.shadowWidth = shadowWidth;
  }

  public int getDistance() {
    return distance;
  }

  public void setDistance(int distance) {
    this.distance = distance;
  }

  public float getOpacity() {
    return opacity;
  }

  public void setOpacity(float opacity) {
    this.opacity = opacity;
  }

  public void update(Graphics graphics) {
    graphics.clearRect(0, 0, getWidth(), getHeight());
    paintComponents(graphics);
  }

  protected void paintComponent(Graphics graphics) {
    setOpaque(false);
    Graphics2D g2d = (Graphics2D)graphics;
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    Insets vInsets = getInsets();

    int width = getWidth() - (vInsets.left + vInsets.right);
    int height = getHeight() - (vInsets.top + vInsets.bottom);

    int x = vInsets.left;
    int y = vInsets.top;

    Java2DUtils.fillShadow(g2d, Color.BLACK, opacity, shadowWidth, cornerRadius,
                           x + distance, y + distance, width - (distance * 2), height - (distance * 2));

    int rectWidth = width - (distance * 2);
    int rectHeight = height - (distance * 2);
    if (borderWidth > 0) {
      g2d.setColor(borderColor);
      g2d.fillRoundRect(x, y, rectWidth, rectHeight, cornerRadius, cornerRadius);
    }


    int innerWidth = rectWidth - 2 * borderWidth;
    int innerHeight = rectHeight - 2 * borderWidth;
    int widthRadius = Math.max(0, cornerRadius  - borderWidth);
    int heightRadius = Math.max(0, cornerRadius  - borderWidth);

    GradientPaint gradient = new GradientPaint(x, y, topColor, x, height, bottomColor);
    g2d.setPaint(gradient);
    g2d.fillRoundRect(x + borderWidth, y + borderWidth,
                      innerWidth, innerHeight,
                      widthRadius, heightRadius);
  }
}
