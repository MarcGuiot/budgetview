package org.globsframework.gui.splits.components;

import org.globsframework.gui.splits.utils.GuiUtils;

import javax.swing.*;
import javax.swing.plaf.basic.BasicToggleButtonUI;
import java.awt.*;

public class StyledToggleButtonUI extends BasicToggleButtonUI {
  private Color topColor = Color.WHITE;
  private Color bottomColor = Color.WHITE;
  private Color borderColor = Color.BLACK;
  private Color rolloverColor = Color.BLUE;

  private int borderWidth = 0;

  private int cornerRadius = 0;
  private int padding = 0;

  public StyledToggleButtonUI() {
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

  public Color getRolloverColor() {
    return rolloverColor;
  }

  public void setRolloverColor(Color rolloverColor) {
    this.rolloverColor = rolloverColor;
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

  protected void paintText(Graphics g, AbstractButton button, Rectangle textRect, String text) {
    ButtonModel model = button.getModel();
    FontMetrics fm = button.getFontMetrics(g.getFont());
    int mnemonicIndex = button.getDisplayedMnemonicIndex();

    if (model.isEnabled()) {
      if (model.isRollover()) {
        g.setColor(rolloverColor);
      }
      else {
        g.setColor(button.getForeground());
      }
      g.drawString(text, textRect.x + getTextShiftOffset(),
                   textRect.y + fm.getAscent() + getTextShiftOffset());
      GuiUtils.drawUnderlineCharAt(g, text, mnemonicIndex, textRect.x + getTextShiftOffset(),
                                   textRect.y + fm.getAscent() + getTextShiftOffset());

    }
    else {
      g.setColor(button.getBackground().brighter());
      g.drawString(text, textRect.x, textRect.y + fm.getAscent());
      GuiUtils.drawUnderlineCharAt(g, text, mnemonicIndex, textRect.x, textRect.y + fm.getAscent());

      g.setColor(button.getBackground().darker());
      g.drawString(text, textRect.x - 1, textRect.y + fm.getAscent() - 1);
      GuiUtils.drawUnderlineCharAt(g, text, mnemonicIndex, textRect.x - 1, textRect.y + fm.getAscent() - 1);
    }
  }

  protected void paintButtonPressed(Graphics g, AbstractButton button) {
    button.setOpaque(false);
    Graphics2D g2d = (Graphics2D)g;
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    int width = button.getWidth();
    int height = button.getHeight();

    int x = 0;
    int y = 0;

    int rectWidth = width - (padding * 2);
    int rectHeight = height - (padding * 2);
    if (borderWidth > 0) {
      g2d.setColor(borderColor);
      g2d.fillRoundRect(x, y, rectWidth, rectHeight, cornerRadius, cornerRadius);
    }

    int innerWidth = rectWidth - 2 * borderWidth;
    int innerHeight = rectHeight - 2 * borderWidth;
    int widthRadius = Math.max(0, cornerRadius - borderWidth);
    int heightRadius = Math.max(0, cornerRadius - borderWidth);

    GradientPaint gradient = new GradientPaint(x, y, topColor, x, height, bottomColor);
    g2d.setPaint(gradient);
    g2d.fillRoundRect(x + borderWidth, y + borderWidth, innerWidth, innerHeight, widthRadius, heightRadius);
  }
}