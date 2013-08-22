package org.globsframework.gui.splits.components;

import javax.swing.*;
import java.awt.*;

public class RoundedRectIcon implements Icon {

  private int width;
  private int height;
  private int arcX;
  private int arcY;
  private Color backgroundColor = Color.GRAY;
  private Color borderColor = Color.BLACK;

  public RoundedRectIcon(int width, int height, int arcX, int arcY) {
    this.width = width;
    this.height = height;
    this.arcX = arcX;
    this.arcY = arcY;
  }

  public void paintIcon(Component component, Graphics graphics, int i, int i2) {
    graphics.fillRoundRect(0, 0, width - 1, height - 1, arcX, arcY);
    graphics.setColor(backgroundColor);
    graphics.fillRoundRect(0, 0, width - 1, height - 1, arcX, arcY);
    graphics.setColor(borderColor);
    graphics.drawRoundRect(0, 0, width - 1, height - 1, arcX, arcY);
  }

  public int getIconWidth() {
    return width;
  }

  public int getIconHeight() {
    return height;
  }

  public int getArcX() {
    return arcX;
  }

  public int getArcY() {
    return arcY;
  }

  public Color getBackgroundColor() {
    return backgroundColor;
  }

  public void setBackgroundColor(Color backgroundColor) {
    this.backgroundColor = backgroundColor;
  }

  public Color getBorderColor() {
    return borderColor;
  }

  public void setBorderColor(Color borderColor) {
    this.borderColor = borderColor;
  }
}
