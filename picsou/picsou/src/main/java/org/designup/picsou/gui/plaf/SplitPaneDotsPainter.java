package org.designup.picsou.gui.plaf;

import com.jidesoft.plaf.basic.Painter;
import com.jidesoft.swing.JideSplitPane;
import com.jidesoft.swing.JideSplitPaneDivider;

import javax.swing.*;
import java.awt.*;

public class SplitPaneDotsPainter implements Painter {
  public void paint(JComponent c, Graphics g, Rectangle rect, int orientation, int state) {

    JideSplitPaneDivider divider = (JideSplitPaneDivider)c;
    JideSplitPane splitPane = divider.getJideSplitPane();
    Color color = splitPane.getForeground();

    Color bright = color.brighter();
    Color brighter = bright.brighter();
    Color dark = color.darker();

    int middleX = rect.x + rect.width / 2;
    int middleY = rect.y + rect.height / 2;

    switch (orientation) {
      case SwingConstants.HORIZONTAL:
        g.setColor(bright);
        g.drawLine(middleX - 1, middleY - 1, middleX - 1, middleY + 1);
        g.drawLine(middleX - 1, middleY - 4, middleX - 1, middleY - 6);
        g.drawLine(middleX - 1, middleY + 4, middleX - 1, middleY + 6);
        g.drawLine(middleX - 1, middleY - 10, middleX - 1, middleY - 11);
        g.drawLine(middleX - 1, middleY + 10, middleX - 1, middleY + 11);
        g.setColor(brighter);
        g.drawLine(middleX, middleY - 1, middleX, middleY + 1);
        g.drawLine(middleX, middleY - 4, middleX, middleY - 6);
        g.drawLine(middleX, middleY + 4, middleX, middleY + 6);
        g.drawLine(middleX, middleY - 10, middleX, middleY - 11);
        g.drawLine(middleX, middleY + 10, middleX, middleY + 11);
        g.setColor(dark);
        g.drawLine(middleX + 1, middleY - 1, middleX + 1, middleY + 1);
        g.drawLine(middleX + 1, middleY - 4, middleX + 1, middleY - 6);
        g.drawLine(middleX + 1, middleY + 4, middleX + 1, middleY + 6);
        g.drawLine(middleX + 1, middleY - 10, middleX + 1, middleY - 11);
        g.drawLine(middleX + 1, middleY + 10, middleX + 1, middleY + 11);
        g.drawLine(middleX + 2, middleY - 1, middleX + 2, middleY + 1);
        g.drawLine(middleX + 2, middleY - 4, middleX + 2, middleY - 6);
        g.drawLine(middleX + 2, middleY + 4, middleX + 2, middleY + 6);
        g.drawLine(middleX + 2, middleY - 10, middleX + 2, middleY - 11);
        g.drawLine(middleX + 2, middleY + 10, middleX + 2, middleY + 11);
        break;
      case SwingConstants.VERTICAL:
        g.setColor(bright);
        g.drawLine(middleX - 1, middleY - 1, middleX + 1, middleY - 1);
        g.drawLine(middleX - 4, middleY - 1, middleX - 6, middleY - 1);
        g.drawLine(middleX + 4, middleY - 1, middleX + 6, middleY - 1);
        g.drawLine(middleX - 10, middleY - 1, middleX - 11, middleY - 1);
        g.drawLine(middleX + 10, middleY - 1, middleX + 11, middleY - 1);
        g.setColor(brighter);
        g.drawLine(middleX - 1, middleY, middleX + 1, middleY);
        g.drawLine(middleX - 4, middleY, middleX - 6, middleY);
        g.drawLine(middleX + 4, middleY, middleX + 6, middleY);
        g.drawLine(middleX - 10, middleY, middleX - 11, middleY);
        g.drawLine(middleX + 10, middleY, middleX + 11, middleY);
        g.setColor(dark);
        g.drawLine(middleX - 1, middleY + 1, middleX + 1, middleY + 1);
        g.drawLine(middleX - 4, middleY + 1, middleX - 6, middleY + 1);
        g.drawLine(middleX + 4, middleY + 1, middleX + 6, middleY + 1);
        g.drawLine(middleX - 10, middleY + 1, middleX - 11, middleY + 1);
        g.drawLine(middleX + 10, middleY + 1, middleX + 11, middleY + 1);
        g.setColor(color);
        g.drawLine(middleX - 1, middleY + 2, middleX + 1, middleY + 2);
        g.drawLine(middleX - 4, middleY + 2, middleX - 6, middleY + 2);
        g.drawLine(middleX + 4, middleY + 2, middleX + 6, middleY + 2);
        g.drawLine(middleX - 10, middleY + 2, middleX - 11, middleY + 2);
        g.drawLine(middleX + 10, middleY + 2, middleX + 11, middleY + 2);
        break;
    }
  }
}
