package org.designup.picsou.gui.plaf;

import org.crossbowlabs.splits.color.ColorService;
import org.crossbowlabs.splits.utils.Java2DUtils;
import org.designup.picsou.gui.utils.PicsouColors;

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
  private int distance = 0;
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
    noFocusColorBorder = colorService.get(PicsouColors.BUTTON_NO_FOCUS_COLOR_BORDER);
    noFocusColorShadow = colorService.get(PicsouColors.BUTTON_NO_FOCUS_COLOR_SHADOW);
    focusColorBorder = colorService.get(PicsouColors.BUTTON_FOCUS_COLOR_BORDER);
    focusColorShadow = colorService.get(PicsouColors.BUTTON_FOCUS_COLOR_SHADOW);
    topColor = colorService.get(PicsouColors.BUTTON_TOP_COLOR_GRADIENT);
    bottomColor = colorService.get(PicsouColors.BUTTON_BOTTOM_COLOR_GRADIENT);
  }

  public void uninstallUI(JComponent c) {
    super.uninstallUI(c);
    c.removeMouseListener(this);
  }

  public void paint(Graphics g, JComponent c) {
    AbstractButton button = (AbstractButton)c;
    Graphics2D g2d = (Graphics2D)g;
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    Insets insets = button.getInsets();
    Insets margin = button.getMargin();
    Dimension size = button.getSize();

    int width = size.width - (insets.left - margin.left) - (insets.right - margin.right) - 1;
    int height = size.height - (insets.top - margin.top) - (insets.bottom - margin.bottom) - 1;

    int x = insets.left - margin.left;
    int y = insets.top - margin.top;

    int rectX = (distance == 0) ? x + shadowWidth : x;
    int rectY = (distance == 0) ? y + shadowWidth : y;
    int rectWidth = (distance == 0) ? width - (shadowWidth * 2) : width - (distance * 2);
    int rectHeight = (distance == 0) ? height - (shadowWidth * 2) : height - (distance * 2);

    if (button.getIcon() == null) {
      ButtonModel buttonModel = button.getModel();
      if (buttonModel.isRollover() && button.isEnabled() && !buttonModel.isPressed()) {
        shadowColor = focusColorShadow;
        borderColor = focusColorBorder;
        opacity = 0.8f;
      }
      else {
        shadowColor = noFocusColorShadow;
        borderColor = noFocusColorBorder;
        opacity = 0.2f;
      }

      int shadowX = x + distance;
      int shadowY = y + distance;

      Java2DUtils.fillShadow(g2d, shadowColor, opacity, shadowWidth, cornerRadius, shadowX,
                             shadowY, width - (distance * 2), height - (distance * 2));

      if (borderWidth > 0) {
        g2d.setColor(borderColor);
        g2d.fillRoundRect(rectX, rectY, rectWidth, rectHeight, cornerRadius, cornerRadius);
      }

      GradientPaint gradient = new GradientPaint(x, y, topColor, x, height, bottomColor);
      g2d.setPaint(gradient);
      g2d.fillRoundRect(rectX + borderWidth, rectY + borderWidth,
                        rectWidth - 2 * borderWidth, rectHeight - 2 * borderWidth,
                        cornerRadius, cornerRadius);
      super.paint(g, c);
      if (!button.isEnabled()) {
        g2d.setColor(Color.WHITE);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        g2d.fillRoundRect(rectX, rectY, rectWidth, rectHeight, cornerRadius, cornerRadius);
      }
      if (button.getModel().isPressed()) {
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
