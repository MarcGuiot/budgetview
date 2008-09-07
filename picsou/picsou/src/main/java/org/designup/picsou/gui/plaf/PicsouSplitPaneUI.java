package org.designup.picsou.gui.plaf;

import javax.swing.*;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import java.awt.*;

public class PicsouSplitPaneUI extends BasicSplitPaneUI {
  private Color handleColor = Color.WHITE;
  private int handleSize = 30;

  public void setHandleColor(Color handleColor) {
    this.handleColor = handleColor;
  }

  public void setHandleSize(int handleSize) {
    this.handleSize = handleSize;
  }

  public void installUI(JComponent component) {
    super.installUI(component);

    JSplitPane splitPane = (JSplitPane)component;
    splitPane.setBorder(null);
  }

  public void paint(Graphics g, JComponent component) {
    Graphics2D g2 = (Graphics2D)g;
    JSplitPane splitPane = (JSplitPane)component;

    g2.setPaint(handleColor);
    if (splitPane.getOrientation() == JSplitPane.VERTICAL_SPLIT) {
      int handleX = component.getWidth() / 2 - handleSize / 2;
      int handleY = splitPane.getDividerLocation() + splitPane.getDividerSize() / 2;
      g2.fillRect(handleX, handleY, handleSize, 1);
    }
    else {
      int handleX = splitPane.getDividerLocation() + splitPane.getDividerSize() / 2;
      int handleY = component.getHeight() / 2 - handleSize / 2;
      g2.fillRect(handleX, handleY, 1, handleSize);

    }
  }
}
