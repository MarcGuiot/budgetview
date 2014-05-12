package org.globsframework.gui.splits.components;

import javax.swing.*;
import java.awt.*;

public class OvalIcon implements Icon {

  private int width;
  private int height;
  private Color backgroundColor = Color.GRAY;
  private Color borderColor = Color.BLACK;

  public OvalIcon(int width, int height) {
    this.width = width;
    this.height = height;
  }

  public void paintIcon(Component component, Graphics graphics, int x, int y) {
    Graphics2D g2d = (Graphics2D)graphics;
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2d.setColor(backgroundColor);
    g2d.fillOval(x, y, width - 1, height - 1);
    g2d.setColor(borderColor);
    g2d.drawOval(x, y, width - 1, height - 1);
  }

  public int getIconWidth() {
    return width;
  }

  public int getIconHeight() {
    return height;
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
