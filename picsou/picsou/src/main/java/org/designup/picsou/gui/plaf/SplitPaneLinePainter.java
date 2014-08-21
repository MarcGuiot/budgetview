package org.designup.picsou.gui.plaf;

import com.jidesoft.plaf.basic.Painter;
import com.jidesoft.swing.JideSplitPane;
import com.jidesoft.swing.JideSplitPaneDivider;

import javax.swing.*;
import java.awt.*;

public class SplitPaneLinePainter implements Painter {
  public void paint(JComponent c, Graphics g, Rectangle rect, int orientation, int state) {

    JideSplitPaneDivider divider = (JideSplitPaneDivider)c;
    JideSplitPane splitPane = divider.getJideSplitPane();
    Color foreground = splitPane.getForeground();
    Color background = splitPane.getForeground();

    if (splitPane.isOpaque()) {
      g.fillRect(rect.x, rect.y, rect.width, rect.height);
    }


    switch (orientation) {
      case SwingConstants.HORIZONTAL:
        g.setColor(foreground);
        int middleX = rect.x + rect.width / 2;
        g.drawLine(middleX, rect.y, middleX, rect.y + rect.height);
        break;
      case SwingConstants.VERTICAL:
        g.setColor(foreground);
        int middleY = rect.y + rect.height / 2;
        g.drawLine(rect.x, middleY, rect.x + rect.width, middleY);
        break;
    }
  }
}
