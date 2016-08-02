package com.budgetview.desktop.plaf;

import org.globsframework.gui.splits.color.Colors;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;

public class ButtonPanelItemUI extends BasicButtonUI {

  private static final int ICON_WIDTH = 20;
  private static final int ICON_HEIGHT = 16;

  private Color borderTop = Colors.toColor("cecece");
  private Color border = Colors.toColor("7e7e7e");
  private Color pressedTopColor = Colors.toColor("b8b8b8");
  private Color normalTopColor = Colors.toColor("fbfbfb");
  private Color pressedBottomColor = Colors.toColor("a0a0a0");
  private Color normalBottomColor = Colors.toColor("f1f1f1");
  private Color borderBottom = Colors.toColor("f0f0f0");

  public void installUI(JComponent component) {
    super.installUI(component);

    AbstractButton button = (AbstractButton)component;
    button.setText(null);
    button.setOpaque(true);
    button.setBorderPainted(false);
    Dimension size = new Dimension(ICON_WIDTH + 3, ICON_HEIGHT + 5);
    button.setMinimumSize(size);
    button.setPreferredSize(size);
    button.setMaximumSize(size);
  }

  public void paint(Graphics g, JComponent component) {
    Graphics2D g2 = (Graphics2D)g;

    g2.setColor(borderTop);
    g2.drawLine(0, 0, ICON_WIDTH + 2, 0);

    g2.setColor(border);
    g2.drawRect(0, 1, ICON_WIDTH + 2, ICON_HEIGHT + 2);

    AbstractButton button = (AbstractButton)component;
    boolean isPressed = button.getModel().isArmed() || button.getModel().isPressed() || button.getModel().isSelected();

    g2.setColor(isPressed ? pressedTopColor : normalTopColor);
    g2.fillRect(1, 2, ICON_WIDTH + 1, ICON_HEIGHT / 2);

    g2.setColor(isPressed ? pressedBottomColor : normalBottomColor);
    g2.fillRect(1, 2 + ICON_HEIGHT / 2, ICON_WIDTH + 1, ICON_HEIGHT - ((ICON_HEIGHT - 2) / 2));

    g2.setColor(borderBottom);
    g2.drawLine(0, ICON_HEIGHT + 4, ICON_WIDTH + 4, ICON_HEIGHT + 4);

    Icon icon = button.isEnabled() ? button.getIcon() : button.getDisabledIcon();
    if (icon != null) {
      icon.paintIcon(button, g, 1, 1);
    }
  }

  public void setBorder(Color border) {
    this.border = border;
  }

  public void setBorderTop(Color borderTop) {
    this.borderTop = borderTop;
  }

  public void setBorderBottom(Color borderBottom) {
    this.borderBottom = borderBottom;
  }

  public void setNormalTopColor(Color normalTopColor) {
    this.normalTopColor = normalTopColor;
  }

  public void setNormalBottomColor(Color normalBottomColor) {
    this.normalBottomColor = normalBottomColor;
  }

  public void setPressedTopColor(Color pressedTopColor) {
    this.pressedTopColor = pressedTopColor;
  }

  public void setPressedBottomColor(Color pressedBottomColor) {
    this.pressedBottomColor = pressedBottomColor;
  }
}
