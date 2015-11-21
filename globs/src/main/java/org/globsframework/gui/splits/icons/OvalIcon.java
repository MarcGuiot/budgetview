package org.globsframework.gui.splits.icons;

import java.awt.*;

public class OvalIcon extends BorderColorIcon {

  private int width;
  private int height;

  public OvalIcon(int width, int height) {
    this.width = width;
    this.height = height;
  }

  public void paintIcon(Component component, Graphics graphics, int x, int y) {
    Graphics2D g2d = (Graphics2D) graphics;
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2d.setColor(getBackgroundColor());
    g2d.fillOval(x, y, width - 1, height - 1);
    g2d.setColor(getBorderColor());
    g2d.drawOval(x, y, width - 1, height - 1);
  }

  public int getIconWidth() {
    return width;
  }

  public int getIconHeight() {
    return height;
  }
}
