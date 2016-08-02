package com.budgetview.desktop.components.ui;

import javax.swing.*;
import javax.swing.plaf.basic.BasicLabelUI;
import java.awt.*;

public class LineLabelUI extends BasicLabelUI {

  private int orientation = SwingConstants.HORIZONTAL;

  public void paint(Graphics graphics, JComponent component) {
    Graphics2D g2 = (Graphics2D) graphics;

    Color foreground = component.getForeground();
    Color background = component.getBackground();
    Rectangle bounds = component.getBounds();
    Rectangle rect = new Rectangle(0, 0, bounds.width, bounds.height);

    if (component.isOpaque()) {
      g2.fillRect(rect.x, rect.y, rect.width, rect.height);
    }

    int middleX = rect.x + rect.width / 2;
    int middleY = rect.y + rect.height / 2;

    switch (orientation) {
      case SwingConstants.VERTICAL:
        int vGradient = rect.height / 4;

        g2.setPaint(new GradientPaint(middleX, rect.y, background,
                                      middleX, rect.y + vGradient, foreground));
        g2.fillRect(middleX, rect.y, 1, vGradient);

        g2.setColor(foreground);
        g2.drawLine(middleX, rect.y + vGradient, middleX, rect.y + rect.height - vGradient);

        g2.setPaint(new GradientPaint(middleX, rect.y + rect.height - vGradient, foreground,
                                      middleX, rect.y + rect.height, background));
        g2.fillRect(middleX, rect.y + rect.height - vGradient, 1, vGradient);

        break;

      case SwingConstants.HORIZONTAL:
        int hGradient = rect.width / 4;

        g2.setPaint(new GradientPaint(rect.x, middleY, background,
                                      rect.x + hGradient, middleY, foreground));
        g2.drawLine(rect.x, middleY, rect.x + rect.width, middleY);

        g2.setColor(foreground);
        g2.drawLine(rect.x + hGradient, middleY, rect.x + rect.width - hGradient, middleY);

        g2.setPaint(new GradientPaint(rect.x + rect.width - hGradient, middleY, foreground,
                                      rect.x + rect.width, middleY, background));
        g2.drawLine(rect.x + rect.width - hGradient, middleY, rect.x + rect.width, middleY);
        break;
    }
  }

  public Dimension getPreferredSize(JComponent var1) {
    return orientation == SwingConstants.HORIZONTAL ? new Dimension(10, 4) : new Dimension(4, 10);
  }

  public Dimension getMinimumSize(JComponent var1) {
    return new Dimension(4, 4);
  }

  public Dimension getMaximumSize(JComponent var1) {
    return null;
  }

  public void setOrientation(int orientation) {
    this.orientation = orientation;
  }
}
