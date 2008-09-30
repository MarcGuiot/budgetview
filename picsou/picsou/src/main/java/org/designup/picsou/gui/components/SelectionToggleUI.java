package org.designup.picsou.gui.components;

import org.designup.picsou.gui.utils.Gui;
import org.globsframework.gui.splits.utils.GuiUtils;

import javax.swing.*;
import javax.swing.plaf.basic.BasicToggleButtonUI;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class SelectionToggleUI extends BasicToggleButtonUI {
  private boolean useBold = false;
  private int padding = 0;
  private int borderWidth = 1;
  private Color borderColor = Color.GRAY.brighter();
  private int cornerRadius = 10;
  private Color topColor = Color.WHITE;
  private Color bottomColor = Color.WHITE;
  private Color rolloverTextColor = Color.RED;
  private Color pressedTextColor = Color.BLUE;
  private Color disabledTextColor = Color.GRAY;

  protected void installDefaults(final AbstractButton button) {
    super.installDefaults(button);
    button.setRolloverEnabled(true);
    button.setOpaque(false);
    button.setBorderPainted(false);
    button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

    if (useBold) {
      final Font defaultFont = button.getFont();
      final Font boldFont = defaultFont.deriveFont(defaultFont.getStyle() ^ Font.BOLD);

      button.getModel().addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent e) {
          button.setFont(button.isSelected() ? boldFont : defaultFont);
        }
      });
    }
  }

  protected void paintButtonPressed(Graphics g, AbstractButton button) {
    button.setOpaque(false);
    Graphics2D g2d = (Graphics2D)g;
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    int width = button.getWidth();
    int height = button.getHeight();
    if (Gui.isMacOSX()) {
      height -= 2;
    }

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
    g2d.fillRoundRect(x + borderWidth, y + borderWidth,
                      innerWidth, innerHeight,
                      widthRadius, heightRadius);
  }

  protected void paintText(Graphics g, JComponent component, Rectangle textRect, String text) {
    AbstractButton b = (AbstractButton)component;
    ButtonModel model = b.getModel();
    FontMetrics fm = g.getFontMetrics(g.getFont());
    int mnemonicIndex = b.getDisplayedMnemonicIndex();
    if (model.isRollover()) {
      g.setColor(rolloverTextColor);
    }
    else if (model.isPressed() || model.isSelected()) {
      g.setColor(pressedTextColor);
    }
    else {
      g.setColor(component.getForeground());
    }
    if (!b.isEnabled()) {
      g.setColor(disabledTextColor);
    }

    if (mnemonicIndex > 0) {
      GuiUtils.drawStringUnderlineCharAt(g, text, mnemonicIndex,
                                         textRect.x + getTextShiftOffset(),
                                         textRect.y + fm.getAscent() + getTextShiftOffset());
    }
    else {
      g.drawString(text, textRect.x + getTextShiftOffset(),
                   textRect.y + fm.getAscent() + getTextShiftOffset());
    }
  }

  public void setUseBold(boolean useBold) {
    this.useBold = useBold;
  }

  public void setBorderColor(Color borderColor) {
    this.borderColor = borderColor;
  }

  public void setBottomColor(Color bottomColor) {
    this.bottomColor = bottomColor;
  }

  public void setTopColor(Color topColor) {
    this.topColor = topColor;
  }

  public void setPressedTextColor(Color pressedTextColor) {
    this.pressedTextColor = pressedTextColor;
  }

  public void setRolloverTextColor(Color rolloverTextColor) {
    this.rolloverTextColor = rolloverTextColor;
  }

  public void setDisabledTextColor(Color disabledTextColor) {
    this.disabledTextColor = disabledTextColor;
  }

  public void setBorderWidth(int borderWidth) {
    this.borderWidth = borderWidth;
  }

  public void setCornerRadius(int cornerRadius) {
    this.cornerRadius = cornerRadius;
  }

  public void setPadding(int padding) {
    this.padding = padding;
  }
}
