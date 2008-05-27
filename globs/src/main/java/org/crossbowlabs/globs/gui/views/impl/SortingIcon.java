package org.crossbowlabs.globs.gui.views.impl;

import javax.swing.*;
import java.awt.*;

public class SortingIcon implements Icon {
  public static final int NONE = 0;
  public static final int DOWN = 1;
  public static final int UP = 2;

  private int direction;
  private int width = 6;
  private int height = 6;

  private static Color ARROW_BG_COLOR;
  private static Color ARROW_LIGHT_COLOR;
  private static Color ARROW_SHADE_COLOR;

  static {
    ARROW_BG_COLOR = Color.LIGHT_GRAY;
    ARROW_LIGHT_COLOR = ARROW_BG_COLOR.brighter();
    ARROW_SHADE_COLOR = ARROW_BG_COLOR.darker();
  }

  public SortingIcon(int direction) {
    this.direction = direction;
  }

  public int getDirection() {
    return direction;
  }

  public int getIconWidth() {
    return width;
  }

  public int getIconHeight() {
    return height;
  }

  public void paintIcon(Component c, Graphics g, int x, int y) {
    int w = width;
    int h = height;
    int m = w / 2;
    if (direction == DOWN) {
      g.setColor(ARROW_BG_COLOR);
      g.fillPolygon(new Polygon(new int[]{x, x + w, x + m}, new int[]{y, y, y + h}, 3));
      g.setColor(ARROW_SHADE_COLOR);
      g.drawLine(x, y, x + w, y);
      g.drawLine(x, y, x + m, y + h);
      g.setColor(ARROW_LIGHT_COLOR);
      g.drawLine(x + w, y, x + m, y + h);
    }
    else if (direction == UP) {
      g.setColor(ARROW_BG_COLOR);
      g.fillPolygon(new Polygon(new int[]{x + m, x + w, x}, new int[]{y, y + h, y + h}, 3));
      g.setColor(ARROW_SHADE_COLOR);
      g.drawLine(x + m, y, x, y + h);
      g.setColor(ARROW_LIGHT_COLOR);
      g.drawLine(x, y + h, x + w, y + h);
      g.drawLine(x + m, y, x + w, y + h);
    }
  }
}
