package org.designup.picsou.gui.components;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class RoundButtonUI extends BasicButtonUI {

  protected Color topColor;
  protected Color bottomColor;
  protected Color innerBorderTopColor;
  protected Color innerBorderBottomColor;
  protected Color outerBorderTopColor;
  protected Color outerBorderBottomColor;
  protected Color pressedTopColor;
  protected Color pressedBottomColor;
  protected Color pressedInnerBorderTopColor;
  protected Color pressedInnerBorderBottomColor;
  protected Color pressedOuterBorderTopColor;
  protected Color pressedOuterBorderBottomColor;

  public void installUI(JComponent component) {
    super.installUI(component);

    JButton button = (JButton)component;
    button.setOpaque(false);
    button.setBorderPainted(false);
    button.setFocusPainted(false);
    button.setRolloverEnabled(true);
    button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
  }

  protected void installListeners(final AbstractButton button) {
    super.installListeners(button);
    button.addPropertyChangeListener("icon", new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        Icon icon = button.getIcon();
        if (icon != null) {
          int diameter = Math.max(icon.getIconWidth(), icon.getIconHeight()) + 15;
          Dimension size = new Dimension(diameter, diameter);
          button.setSize(diameter, diameter);
          button.setPreferredSize(size);
          button.setMaximumSize(size);
          button.setMinimumSize(size);
        }
      }
    });
  }

  public void paint(Graphics g, JComponent c) {

    JButton button = (JButton)c;
    Graphics2D g2 = (Graphics2D)g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    if (button.getModel().isPressed()) {
      paintBackground(g2, button, pressedTopColor, pressedBottomColor, pressedInnerBorderTopColor, pressedInnerBorderBottomColor, pressedOuterBorderTopColor, pressedOuterBorderBottomColor);
    }
    else {
      paintBackground(g2, button, topColor, bottomColor, innerBorderTopColor, innerBorderBottomColor, outerBorderTopColor, outerBorderBottomColor);
    }

    Icon icon = getIconToPaint(button);
    int x = button.getWidth() / 2 - (icon.getIconWidth() / 2);
    int y = button.getHeight() / 2 - (icon.getIconHeight() / 2);
    icon.paintIcon(button, g2, x, y);

    g2.dispose();
  }

  protected void paintBackground(Graphics2D g2,
                                 JButton button,
                                 Color topColor,
                                 Color bottomColor,
                                 Color innerBorderTopColor,
                                 Color innerBorderBottomColor,
                                 Color outerBorderTopColor,
                                 Color outerBorderBottomColor) {

    g2.setPaint(new GradientPaint(0, 0, topColor, 0, button.getHeight(), bottomColor));
    g2.fillOval(2, 2, button.getWidth() - 4, button.getHeight() - 4);

    g2.setPaint(new GradientPaint(0, 0, innerBorderTopColor, 0, button.getHeight(), innerBorderBottomColor));
    g2.drawOval(1, 1, button.getWidth() - 3, button.getHeight() - 3);

    g2.setPaint(new GradientPaint(0, 0, outerBorderTopColor, 0, button.getHeight(), outerBorderBottomColor));
    g2.drawOval(0, 0, button.getWidth() - 1, button.getHeight() - 1);
  }

  private Icon getIconToPaint(JButton button) {
    if (!button.isEnabled() && (button.getDisabledIcon() != null)) {
      return button.getDisabledIcon();
    }
    else if (button.getModel().isPressed() && (button.getPressedIcon() != null)) {
      return button.getPressedIcon();
    }
    else if (button.getModel().isRollover() && (button.getRolloverIcon() != null)) {
      return button.getRolloverIcon();
    }
    return button.getIcon();
  }

  public void setTopColor(Color topColor) {
    this.topColor = topColor;
  }

  public void setBottomColor(Color bottomColor) {
    this.bottomColor = bottomColor;
  }

  public void setInnerBorderTopColor(Color innerBorderTopColor) {
    this.innerBorderTopColor = innerBorderTopColor;
  }

  public void setInnerBorderBottomColor(Color innerBorderBottomColor) {
    this.innerBorderBottomColor = innerBorderBottomColor;
  }

  public void setOuterBorderTopColor(Color outerBorderTopColor) {
    this.outerBorderTopColor = outerBorderTopColor;
  }

  public void setOuterBorderBottomColor(Color outerBorderBottomColor) {
    this.outerBorderBottomColor = outerBorderBottomColor;
  }

  public void setPressedTopColor(Color pressedTopColor) {
    this.pressedTopColor = pressedTopColor;
  }

  public void setPressedBottomColor(Color pressedBottomColor) {
    this.pressedBottomColor = pressedBottomColor;
  }

  public void setPressedInnerBorderTopColor(Color pressedInnerBorderTopColor) {
    this.pressedInnerBorderTopColor = pressedInnerBorderTopColor;
  }

  public void setPressedInnerBorderBottomColor(Color pressedInnerBorderBottomColor) {
    this.pressedInnerBorderBottomColor = pressedInnerBorderBottomColor;
  }

  public void setPressedOuterBorderTopColor(Color pressedOuterBorderTopColor) {
    this.pressedOuterBorderTopColor = pressedOuterBorderTopColor;
  }

  public void setPressedOuterBorderBottomColor(Color pressedOuterBorderBottomColor) {
    this.pressedOuterBorderBottomColor = pressedOuterBorderBottomColor;
  }
}