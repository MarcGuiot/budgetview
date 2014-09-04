package org.designup.picsou.gui.plaf;

import com.jidesoft.plaf.basic.Painter;
import com.jidesoft.swing.JideSplitPane;
import com.jidesoft.swing.JideSplitPaneDivider;

import javax.swing.*;
import java.awt.*;

public class SplitPaneLinePainter implements Painter {

  private static final int HANDLE_SIZE = 8;
  public static final int HANDLE_DISTANCE = 2;

  public void paint(JComponent c, Graphics g, Rectangle rect, int orientation, int state) {

    Graphics2D g2 = (Graphics2D)g;

    JideSplitPaneDivider divider = (JideSplitPaneDivider)c;
    JideSplitPane splitPane = divider.getJideSplitPane();
    Rectangle splitRect = splitPane.getBounds();
    Color foreground = splitPane.getForeground();
    Color handleColor = (Color)splitPane.getClientProperty("handleColor");
    Color background = splitPane.getBackground();

    if (splitPane.isOpaque()) {
      g.fillRect(rect.x, rect.y, rect.width, rect.height);
    }

    int middleX = rect.x + rect.width / 2;
    int middleY = rect.y + rect.height / 2;

    switch (orientation) {
      case SwingConstants.HORIZONTAL:
        int vGradient = splitRect.height / 4;

        g2.setPaint(new GradientPaint(middleX, splitRect.y, background,
                                      middleX, splitRect.y + vGradient, foreground));
        g2.fillRect(middleX, splitRect.y, 1, vGradient);

        g2.setColor(foreground);
        g2.drawLine(middleX, splitRect.y + vGradient, middleX, splitRect.y + splitRect.height - vGradient);

        g2.setPaint(new GradientPaint(middleX, splitRect.y + splitRect.height - vGradient, foreground,
                                      middleX, splitRect.y + splitRect.height, background));
        g2.fillRect(middleX, splitRect.y + splitRect.height - vGradient, 1, vGradient);

        if (handleColor != null) {
          g2.setColor(handleColor);
          g2.drawLine(middleX - HANDLE_DISTANCE, middleY - HANDLE_SIZE, middleX - HANDLE_DISTANCE, middleY + HANDLE_SIZE);
          g2.drawLine(middleX + HANDLE_DISTANCE, middleY - HANDLE_SIZE, middleX + HANDLE_DISTANCE, middleY + HANDLE_SIZE);
        }
        break;

      case SwingConstants.VERTICAL:
        int hGradient = splitRect.width / 4;

        g2.setPaint(new GradientPaint(splitRect.x, middleY, background,
                                      splitRect.x + hGradient, middleY, foreground));
        g2.drawLine(splitRect.x, middleY, splitRect.x + splitRect.width, middleY);

        g2.setColor(foreground);
        g2.drawLine(splitRect.x + hGradient, middleY, splitRect.x + splitRect.width - hGradient, middleY);

        g2.setPaint(new GradientPaint(splitRect.x + splitRect.width - hGradient, middleY, foreground,
                                      splitRect.x + splitRect.width, middleY, background));
        g2.drawLine(splitRect.x + splitRect.width - hGradient, middleY, splitRect.x + splitRect.width, middleY);

        if (handleColor != null) {
          g2.setColor(handleColor);
          g2.drawLine(middleX - HANDLE_SIZE, middleY - HANDLE_DISTANCE, middleX + HANDLE_SIZE, middleY - HANDLE_DISTANCE);
          g2.drawLine(middleX - HANDLE_SIZE, middleY + HANDLE_DISTANCE, middleX + HANDLE_SIZE, middleY + HANDLE_DISTANCE);
        }
        break;
    }
  }
}
