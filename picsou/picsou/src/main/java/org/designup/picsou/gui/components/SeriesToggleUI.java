package org.designup.picsou.gui.components;

import com.sun.java.swing.SwingUtilities2;

import javax.swing.plaf.basic.BasicToggleButtonUI;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.*;
import javax.swing.text.View;
import java.awt.*;

public class SeriesToggleUI extends BasicToggleButtonUI {
  private int padding = 0;
  private int borderWidth = 1;
  private Color borderColor = Color.GRAY.brighter();
  private int cornerRadius = 10;
  private Color topColor = Color.WHITE;
  private Color bottomColor = Color.WHITE;
  private Color rolloverTextColor = Color.RED;
  private Color pressedTextColor = Color.BLUE;

  protected void installDefaults(AbstractButton b) {
    super.installDefaults(b);
    b.setRolloverEnabled(true);
    b.setOpaque(false);
  }

  public void paint(Graphics g, JComponent component) {
    AbstractButton button = (AbstractButton)component;
    ButtonModel model = button.getModel();

    Dimension size = button.getSize();
    Insets insets = button.getInsets();
    Rectangle viewRect = new Rectangle(size);
    viewRect.x += insets.left;
    viewRect.y += insets.top;
    viewRect.width -= (insets.right + viewRect.x);
    viewRect.height -= (insets.bottom + viewRect.y);

    Rectangle iconRect = new Rectangle();
    Rectangle textRect = new Rectangle();

    g.setColor(button.getBackground());

    if (model.isArmed() && model.isPressed() || model.isSelected()) {
      paintButtonPressed(g, button);
    }

    // Paint the Icon
    if (button.getIcon() != null) {
      paintIcon(g, button, iconRect);
    }

    // Draw the Text
    Font f = button.getFont();
    g.setFont(f);

    String text = SwingUtilities.layoutCompoundLabel(
      button, g.getFontMetrics(), button.getText(), button.getIcon(),
      button.getVerticalAlignment(), button.getHorizontalAlignment(),
      button.getVerticalTextPosition(), button.getHorizontalTextPosition(),
      viewRect, iconRect, textRect,
      button.getText() == null ? 0 : button.getIconTextGap());
    if (text != null && !text.equals("")) {
      View v = (View)component.getClientProperty(BasicHTML.propertyKey);
      if (v != null) {
        v.paint(g, textRect);
      }
      else {
        paintText(g, button, textRect, text);
      }
    }
  }

  protected void paintButtonPressed(Graphics g, AbstractButton button) {
    button.setOpaque(false);
    Graphics2D g2d = (Graphics2D)g;
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    int width = button.getWidth();
    int height = button.getHeight() - 4;

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
    FontMetrics fm = SwingUtilities2.getFontMetrics(component, g);
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

    SwingUtilities2.drawStringUnderlineCharAt(component, g, text, mnemonicIndex,
                                              textRect.x + getTextShiftOffset(),
                                              textRect.y + fm.getAscent() + getTextShiftOffset());
  }
}
