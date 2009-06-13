package org.designup.picsou.gui.components.dialogs;

import org.globsframework.gui.splits.utils.GuiUtils;
import org.designup.picsou.gui.components.DialogMovingListener;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import javax.swing.border.Border;
import java.awt.*;

public class MovingDialog {
  public static final int TITLE_BAR_HEIGHT = 19;

  public static void installWindowTitle(JComponent mainComponent, final PicsouDialogPainter painter,
                                        final String title, int insets) {
    MatteBorder titleBorder = new MatteBorder(TITLE_BAR_HEIGHT, 0, 0, 0, Color.gray) {
      public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        drawTitle((Graphics2D)g, title, x, y, width, painter);
      }
    };

    Border emptyBorder = BorderFactory.createEmptyBorder(insets, insets, insets, insets);
    Border lineBorder = BorderFactory.createLineBorder(painter.getBorderColor(), 1);
    mainComponent.setBorder(BorderFactory.createCompoundBorder(lineBorder,
                                                               BorderFactory.createCompoundBorder(titleBorder, emptyBorder)));

    mainComponent.invalidate();
    mainComponent.revalidate();
  }

  public static void drawTitle(Graphics2D g2d, String title, int x, int y, int width, PicsouDialogPainter painter) {
    g2d.setFont(GuiUtils.getDefaultLabelFont());
    FontMetrics metrics = g2d.getFontMetrics();
    int fontHeight = (metrics.getMaxAscent() - metrics.getMaxDescent());
    int stringWidth = SwingUtilities.computeStringWidth(metrics, title);

    int titleX = x + (width / 2) - (stringWidth / 2);
    int titleY = y + (TITLE_BAR_HEIGHT / 2) + (fontHeight / 2);
    int titleHeight = TITLE_BAR_HEIGHT;

    g2d.setPaint(new GradientPaint(x, y, Color.WHITE, x, y + titleHeight, Color.GRAY));
    g2d.fillRect(x, y, width, titleHeight);

    g2d.setColor(Color.BLACK);
    g2d.drawString(title, titleX, titleY);

    g2d.setColor(painter.getBorderColor());
    g2d.drawLine(x, titleHeight, x + width, titleHeight);
  }

  public static void installMovingWindowTitle(Window movingWindow) {
    DialogMovingListener dialogMovingListener = new DialogMovingListener(movingWindow, TITLE_BAR_HEIGHT);
    movingWindow.addMouseListener(dialogMovingListener);
    movingWindow.addMouseMotionListener(dialogMovingListener);
  }
}
