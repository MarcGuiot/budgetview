package org.globsframework.gui.splits.icons;

import java.awt.*;

public class PlusIcon extends SingleColorIcon {

  private int iconWidth;
  private int iconHeight;
  private int horizontalWidth;
  private int verticalWidth;

  public PlusIcon(int iconWidth, int iconHeight, int horizontalWidth, int verticalWidth) {
    this.iconWidth = iconWidth;
    this.iconHeight = iconHeight;
    this.horizontalWidth = horizontalWidth;
    this.verticalWidth = verticalWidth;
  }

  public void paintIcon(Component component, Graphics graphics, int x, int y) {
    Graphics2D g2d = (Graphics2D) graphics;
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2d.setColor(getColor());
    g2d.fillRect(x, y + iconHeight / 2 - horizontalWidth / 2, iconWidth - 1, horizontalWidth);
    g2d.fillRect(x + iconWidth / 2 - verticalWidth / 2, y, verticalWidth, iconHeight - 1);
  }

  public int getIconWidth() {
    return iconWidth;
  }

  public int getIconHeight() {
    return iconHeight;
  }

  public int getHorizontalWidth() {
    return horizontalWidth;
  }

  public int getVerticalWidth() {
    return verticalWidth;
  }
}
