package com.budgetview.desktop.plaf;

import com.budgetview.desktop.utils.ApplicationColors;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.utils.Java2DUtils;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class PicsouButtonUI extends BasicButtonUI implements MouseListener {
  private static final PicsouButtonUI BUTTON_UI = new PicsouButtonUI();

  private Color noFocusColorShadow;
  private Color focusColorShadow;
  private Color noFocusColorBorder;
  private Color focusColorBorder;
  private Color topColor;
  private Color bottomColor;

  private Color borderColor;
  private int cornerRadius = 5;
  private Color shadowColor;
  private int borderWidth = 1;
  private int shadowWidth = 2;
  private int distance = 1;
  private float opacity;

  public static ComponentUI createUI(JComponent c) {
    return BUTTON_UI;
  }

  public void installUI(JComponent c) {
    super.installUI(c);
    c.setOpaque(false);
    ((AbstractButton)c).setBorderPainted(false);
    ((AbstractButton)c).setRolloverEnabled(true);
    c.addMouseListener(this);

    ColorService colorService = (ColorService)UIManager.get("ColorService");
    noFocusColorBorder = colorService.get(ApplicationColors.BUTTON_NO_FOCUS_COLOR_BORDER);
    noFocusColorShadow = colorService.get(ApplicationColors.BUTTON_NO_FOCUS_COLOR_SHADOW);
    focusColorBorder = colorService.get(ApplicationColors.BUTTON_FOCUS_COLOR_BORDER);
    focusColorShadow = colorService.get(ApplicationColors.BUTTON_FOCUS_COLOR_SHADOW);
    topColor = colorService.get(ApplicationColors.BUTTON_TOP_COLOR_GRADIENT);
    bottomColor = colorService.get(ApplicationColors.BUTTON_BOTTOM_COLOR_GRADIENT);
  }

  public void uninstallUI(JComponent c) {
    super.uninstallUI(c);
    c.removeMouseListener(this);
  }

  public void paint(Graphics g, JComponent c) {
    AbstractButton button = (AbstractButton)c;
    Graphics2D g2d = (Graphics2D)g;
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    Dimension size = button.getSize();

    int width = size.width;
    int height = size.height;

    int x = 0;
    int y = 0;

    int rectX = x;
    int rectY = y;
    int rectWidth = width - 1;
    int rectHeight = height - 1;

    if (button.getIcon() == null) {
      ButtonModel buttonModel = button.getModel();
      if (buttonModel.isRollover() && button.isEnabled() && !buttonModel.isPressed()) {
        shadowColor = focusColorShadow;
        borderColor = noFocusColorBorder; //focusColorBorder;
        opacity = 0.8f;
      }
      else {
        shadowColor = noFocusColorShadow;
        borderColor = noFocusColorBorder;
        opacity = 0.2f;
      }

      GradientPaint gradient = new GradientPaint(x, y, topColor, x, height, bottomColor);
      g2d.setPaint(gradient);
      g2d.fillRoundRect(rectX + borderWidth, rectY + borderWidth,
                        rectWidth - 2 * borderWidth, rectHeight - 2 * borderWidth,
                        cornerRadius, cornerRadius);

      if (borderWidth > 0) {
        g2d.setColor(borderColor);

        g2d.drawRoundRect(rectX, rectY,
                          rectWidth, rectHeight, cornerRadius, cornerRadius);
      }

      if (buttonModel.isRollover()) {
        g2d.setColor(shadowColor);
        Java2DUtils.drawShadow(g2d, shadowColor, opacity, shadowWidth, cornerRadius, x + distance,
                               y + distance, width - (distance * 2), height - (distance * 2));
      }
      super.paint(g, c);

      if (!button.isEnabled()) {
        g2d.setColor(Color.WHITE);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        g2d.fillRoundRect(rectX, rectY, rectWidth, rectHeight, cornerRadius, cornerRadius);
      }
      if (button.getModel().isPressed() && button.getModel().isRollover()) {
        g2d.setColor(Color.DARK_GRAY);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        g2d.fillRoundRect(rectX, rectY, rectWidth, rectHeight, cornerRadius, cornerRadius);
      }
    }
    else {
      super.paint(g, c);
    }
  }

  public void mouseClicked(MouseEvent e) {
  }

  public void mousePressed(MouseEvent e) {
  }

  public void mouseReleased(MouseEvent e) {
  }

  public void mouseEntered(MouseEvent e) {
    AbstractButton source = (AbstractButton)e.getSource();
    if (source.isRolloverEnabled()) {
      source.getModel().setRollover(true);
    }
    source.repaint();
  }

  public void mouseExited(MouseEvent e) {
    AbstractButton source = (AbstractButton)e.getSource();
    if (source.isRolloverEnabled()) {
      source.getModel().setRollover(false);
    }
    source.repaint();
  }
}
