package org.globsframework.gui.splits.icons;

import javax.swing.*;
import java.awt.*;

public class RoundedRectIcon extends BorderColorIcon {

  private int width;
  private int height;
  private int arcX;
  private int arcY;

  public RoundedRectIcon(int width, int height, int arcX, int arcY) {
    this.width = width;
    this.height = height;
    this.arcX = arcX;
    this.arcY = arcY;
  }

  public void paintIcon(Component component, Graphics graphics, int i, int i2) {
    Graphics2D g2d = (Graphics2D)graphics;
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2d.setColor(getBackgroundColor());
    g2d.fillRoundRect(0, 0, width - 1, height - 1, arcX, arcY);
    g2d.setColor(getBorderColor());
    g2d.drawRoundRect(0, 0, width - 1, height - 1, arcX, arcY);
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
}
