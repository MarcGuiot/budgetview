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
    Color dark = color.darker();

    int middleX = rect.x + rect.width / 2;
    int middleY = rect.y + rect.height / 2;

    switch (orientation) {
      case SwingConstants.HORIZONTAL:
        g.setColor(bright);
        g.drawLine(middleX, middleY - 1, middleX, middleY + 1);
        g.drawLine(middleX, middleY - 4, middleX, middleY - 6);
        g.drawLine(middleX, middleY + 4, middleX, middleY + 6);
        g.setColor(dark);
        g.drawLine(middleX + 1, middleY - 1, middleX + 1, middleY + 1);
        g.drawLine(middleX + 1, middleY - 4, middleX + 1, middleY - 6);
        g.drawLine(middleX + 1, middleY + 4, middleX + 1, middleY + 6);
        break;
      case SwingConstants.VERTICAL:
        g.setColor(bright);
        g.drawLine(middleX - 1, middleY, middleX + 1, middleY);
        g.drawLine(middleX - 4, middleY, middleX - 6, middleY);
        g.drawLine(middleX + 4, middleY, middleX + 6, middleY);
        g.setColor(dark);
        g.drawLine(middleX - 1, middleY + 1, middleX + 1, middleY + 1);
        g.drawLine(middleX - 4, middleY + 1, middleX - 6, middleY + 1);
        g.drawLine(middleX + 4, middleY + 1, middleX + 6, middleY + 1);
        break;
    }
  }
}
