package org.globsframework.gui.splits.components;

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
    drawStringUnderlineCharAt(g, s, mnemIndex, textX + direction.x, textY + direction.y);
    g.setColor(l.getForeground());
    drawStringUnderlineCharAt(g, s, mnemIndex, textX, textY);
  }

  public static void drawStringUnderlineCharAt(Graphics g, String text, int underlinedIndex, int x, int y) {
    g.drawString(text, x, y);
    if (underlinedIndex >= 0 && underlinedIndex < text.length()) {
      // PENDING: this needs to change.
      FontMetrics fm = g.getFontMetrics();
      int underlineRectX = x + fm.stringWidth(text.substring(0, underlinedIndex));
      int underlineRectY = y;
      int underlineRectWidth = fm.charWidth(text.charAt(underlinedIndex));
      int underlineRectHeight = 1;
      g.fillRect(underlineRectX, underlineRectY + 1, underlineRectWidth, underlineRectHeight);
    }
  }

}