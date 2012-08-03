package org.designup.picsou.gui.components.tabs;

import javax.swing.*;
import javax.swing.plaf.basic.BasicPanelUI;
import java.awt.*;

public class VerticalTabPanelUI extends BasicPanelUI {
  
  private Color innerBorderColor = Color.GRAY;

  public void setInnerBorderColor(Color innerBorderColor) {
    this.innerBorderColor = innerBorderColor;
  }

  public void paint(Graphics graphics, JComponent component) {

    int width = component.getWidth() - 3;
    int height = component.getHeight();

    Graphics2D g2 = (Graphics2D)graphics;

    g2.setColor(Color.WHITE);
    g2.fillRect(width, 0, component.getWidth(), height);

    g2.setColor(component.getBackground());
    g2.fillRect(0, 0, width, height - 1);
    
    g2.setColor(innerBorderColor);
    g2.drawLine(width, 0, width, height);
  }

}
