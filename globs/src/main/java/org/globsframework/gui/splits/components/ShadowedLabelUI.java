package org.globsframework.gui.splits.components;

import org.globsframework.gui.splits.utils.GuiUtils;

import javax.swing.*;
import javax.swing.plaf.basic.BasicLabelUI;
import java.awt.*;

public class ShadowedLabelUI extends BasicLabelUI {

  public enum Direction {
    NORTH(0, -1),
    NORTHEAST(1, -1),
    EAST(0, 1),
    SOUTHEAST(1, 1),
    SOUTH(0, 1),
    SOUTHWEST(-1, 1),
    WEST(-1, 0),
    NORTHWEST(-1, -1);

    private int x;
    private int y;

    private Direction(int x, int y) {
      this.x = x;
      this.y = y;
    }

    public static Direction parse(String directionName) {
      for (Direction direction : values()) {
        if (direction.name().equalsIgnoreCase(directionName)) {
          return direction;
        }
      }
      return null;
    }
  }

  private Direction direction = Direction.SOUTHEAST;
  private Color shadowColor = Color.BLACK;

  public void setDirection(Direction direction) {
    this.direction = direction;
  }

  public Direction getDirection() {
    return direction;
  }

  public Color getShadowColor() {
    return shadowColor;
  }

  public void setShadowColor(Color shadowColor) {
    this.shadowColor = shadowColor;
  }

  protected void paintEnabledText(JLabel l, Graphics g, String s, int textX, int textY) {
    if (g instanceof Graphics2D) {
      ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }
    int mnemIndex = l.getDisplayedMnemonicIndex();
    g.setColor(shadowColor);
    g.drawString(s, textX + direction.x, textY + direction.y);
    GuiUtils.drawUnderlineCharAt(g, s, mnemIndex, textX + direction.x, textY + direction.y);
    g.setColor(l.getForeground());
    g.drawString(s, textX, textY);
    GuiUtils.drawUnderlineCharAt(g, s, mnemIndex, textX, textY);
  }

}