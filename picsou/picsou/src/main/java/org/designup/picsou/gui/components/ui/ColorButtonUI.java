package org.designup.picsou.gui.components.ui;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;

public class ColorButtonUI extends BasicButtonUI {

  private Color borderTop = Color.gray;
  private Color borderBottom = Color.gray;

  public void installUI(JComponent component) {
    super.installUI(component);

    JButton button = (JButton)component;
    button.setOpaque(false);
    button.setBorderPainted(false);
    button.setFocusPainted(false);
    button.setRolloverEnabled(true);
    button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
  }

  public void paint(Graphics g, JComponent c) {

    JButton button = (JButton)c;
    Graphics2D g2 = (Graphics2D)g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    int w = button.getWidth() - 1;
    int h = button.getHeight() - 1;

    g2.setPaint(getColor(button));
    g2.fillRect(0, 0, w, h);

    g2.setPaint(borderTop);
    g2.drawLine(0,0, w, 0);
    g2.drawLine(0,0, 0, h);

    g2.setPaint(borderBottom);
    g2.drawLine(w,0, w, h);
    g2.drawLine(0,h, w, h);

    g2.dispose();
  }

  private Color getColor(JButton button) {
    if (button.getModel().isRollover()) {
      return button.getForeground().brighter();
    }
    return button.getForeground();
  }

  public void setBorderTop(Color borderTop) {
    this.borderTop = borderTop;
  }

  public void setBorderBottom(Color borderBottom) {
    this.borderBottom = borderBottom;
  }
}