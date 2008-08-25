package org.designup.picsou.gui.components;

import org.designup.picsou.gui.utils.PicsouColors;
import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.gui.splits.color.ColorService;

import javax.swing.*;
import java.awt.*;

public abstract class RoundedButton extends JButton implements ColorChangeListener {
  protected static final int ARC_WIDTH = 10;
  protected static final int ARC_HEIGHT = 10;
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
  private ColorService colorService;

  public static RoundedButton createRoundedRectangle(Action action, ColorService colorService) {
    return new RoundedButton(action, colorService) {
      public Dimension getPreferredSize() {
        return computeSize();
      }

      public Dimension getMinimumSize() {
        return computeSize();
      }

      public Dimension getMaximumSize() {
        return computeSize();
      }

      protected void paintBackground(Graphics2D g2,
                                     Color topColor, Color bottomColor,
                                     Color innerBorderTopColor, Color innerBorderBottomColor,
                                     Color outerBorderTopColor, Color outerBorderBottomColor) {

        g2.setPaint(new GradientPaint(0, 0, outerBorderTopColor, getWidth() / 2, getHeight(), outerBorderBottomColor));
        g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 2, ARC_WIDTH, ARC_HEIGHT);

        g2.setPaint(new GradientPaint(0, 0, innerBorderTopColor, getWidth() / 2, getHeight(), innerBorderBottomColor));
        g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, ARC_WIDTH, ARC_HEIGHT);

        g2.setPaint(new GradientPaint(0, 0, topColor, 0, getHeight(), bottomColor));
        g2.fillRoundRect(2, 2, getWidth() - 5, getHeight() - 5, ARC_WIDTH, ARC_HEIGHT);
      }

      private Dimension computeSize() {
        Icon icon = getIcon();
        return new Dimension(icon.getIconWidth() + 20, icon.getIconHeight() + 10);
      }
    };
  }

  public static RoundedButton createCircle(Action action, ColorService colorService) {
    return new RoundedButton(action, colorService) {
      public Dimension getPreferredSize() {
        return computeSize();
      }

      public Dimension getMinimumSize() {
        return computeSize();
      }

      public Dimension getMaximumSize() {
        return computeSize();
      }

      protected void paintBackground(Graphics2D g2,
                                     Color topColor, Color bottomColor,
                                     Color innerBorderTopColor, Color innerBorderBottomColor,
                                     Color outerBorderTopColor, Color outerBorderBottomColor) {
        g2.setPaint(new GradientPaint(0, 0, topColor, 0, getHeight(), bottomColor));
        g2.fillOval(2, 2, getWidth() - 5, getHeight() - 5);

        g2.setPaint(new GradientPaint(0, 0, innerBorderTopColor, 0, getHeight(), innerBorderBottomColor));
        g2.drawOval(1, 1, getWidth() - 3, getHeight() - 3);

        g2.setPaint(new GradientPaint(0, 0, outerBorderTopColor, 0, getHeight(), outerBorderBottomColor));
        g2.drawOval(0, 0, getWidth() - 1, getHeight() - 1);
      }

      private Dimension computeSize() {
        Icon icon = getIcon();
        int diameter = Math.max(icon.getIconWidth(), icon.getIconHeight()) + 20;
        return new Dimension(diameter, diameter);
      }
    };
  }

  private RoundedButton(Action action, ColorService colorService) {
    super(action);
    this.colorService = colorService;
    this.colorService.addListener(this);
    setOpaque(false);
    setBorderPainted(false);
    setFocusPainted(false);
  }

  public void colorsChanged(ColorLocator colorLocator) {
    topColor = colorLocator.get(PicsouColors.BUTTON_BG_TOP);
    bottomColor = colorLocator.get(PicsouColors.BUTTON_BG_BOTTOM);
    innerBorderTopColor = colorLocator.get(PicsouColors.BUTTON_INNER_BORDER_TOP);
    innerBorderBottomColor = colorLocator.get(PicsouColors.BUTTON_INNER_BORDER_BOTTOM);
    outerBorderTopColor = colorLocator.get(PicsouColors.BUTTON_OUTER_BORDER_TOP);
    outerBorderBottomColor = colorLocator.get(PicsouColors.BUTTON_OUTER_BORDER_BOTTOM);

    pressedTopColor = colorLocator.get(PicsouColors.BUTTON_PRESSED_BG_TOP);
    pressedBottomColor = colorLocator.get(PicsouColors.BUTTON_PRESSED_BG_BOTTOM);
    pressedInnerBorderTopColor = colorLocator.get(PicsouColors.BUTTON_PRESSED_INNER_BORDER_TOP);
    pressedInnerBorderBottomColor = colorLocator.get(PicsouColors.BUTTON_PRESSED_INNER_BORDER_BOTTOM);
    pressedOuterBorderTopColor = colorLocator.get(PicsouColors.BUTTON_PRESSED_OUTER_BORDER_TOP);
    pressedOuterBorderBottomColor = colorLocator.get(PicsouColors.BUTTON_PRESSED_OUTER_BORDER_BOTTOM);
  }

  protected void paintComponent(Graphics graphics) {
    Graphics2D g2 = (Graphics2D)graphics.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    if (getModel().isPressed()) {
      paintBackground(g2, pressedTopColor, pressedBottomColor, pressedInnerBorderTopColor, pressedInnerBorderBottomColor, pressedOuterBorderTopColor, pressedOuterBorderBottomColor);
    }
    else {
      paintBackground(g2, topColor, bottomColor, innerBorderTopColor, innerBorderBottomColor, outerBorderTopColor, outerBorderBottomColor);
    }

    Icon icon = getIconToPaint();
    int x = getWidth() / 2 - (icon.getIconWidth() / 2);
    int y = getHeight() / 2 - (icon.getIconHeight() / 2);
    icon.paintIcon(this, g2, x, y);

    g2.dispose();
  }

  protected abstract void paintBackground(Graphics2D g2,
                                          Color pressedTopColor,
                                          Color pressedBottomColor,
                                          Color pressedInnerBorderTopColor,
                                          Color pressedInnerBorderBottomColor,
                                          Color pressedOuterBorderTopColor,
                                          Color pressedOuterBorderBottomColor);

  private Icon getIconToPaint() {
    Icon icon = getIcon();
    if (getModel().isPressed() && (getPressedIcon() != null)) {
      icon = getPressedIcon();
    }
    else if (getModel().isRollover() && (getRolloverIcon() != null)) {
      icon = getRolloverIcon();
    }
    return icon;
  }

  protected void finalize() throws Throwable {
    super.finalize();
    colorService.removeListener(this);
  }
}
