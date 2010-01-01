package org.globsframework.gui.splits.color.utils;

import javax.swing.*;
import java.awt.*;

public class ColorRectIcon implements Icon {

  private Color color = Color.WHITE;
  private int height = 12;
  private int width = 15;

  public ColorRectIcon() {
  }

  public ColorRectIcon(int width, int height) {
    this(width, height, Color.WHITE);
  }

  public ColorRectIcon(int width, int height, Color color) {
    this.width = width;
    this.height = height;
    this.color = color;
  }

  public void setColor(Color color) {
    this.color = color;
  }

  public int getIconHeight() {
    return height;
  }

  public int getIconWidth() {
    return width;
  }

  public void paintIcon(Component c, Graphics g, int x, int y) {
    int w = getIconWidth();
    int h = getIconHeight();

    Graphics2D g2 = (Graphics2D)g;
    g2.setColor(color);
    g.fillRect(x, y + 1, x + w, y + h - 2);
    g2.setColor(Color.DARK_GRAY);
    g.drawRect(x, y + 1, x + w, y + h - 2);
  }
}
