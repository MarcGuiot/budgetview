package com.budgetview.desktop.components.tabs;

import javax.swing.*;
import javax.swing.plaf.basic.BasicPanelUI;
import java.awt.*;

public class VerticalTabPanelUI extends BasicPanelUI {

  private Color innerBorderColor = Color.GRAY;
  private Color outerBorderColor = Color.GRAY;
  private Color contentBgColor = Color.WHITE;

  public void setInnerBorderColor(Color innerBorderColor) {
    this.innerBorderColor = innerBorderColor;
  }

  public void setOuterBorderColor(Color outerBorderColor) {
    this.outerBorderColor = outerBorderColor;
  }

  public void setContentBgColor(Color contentBgColor) {
    this.contentBgColor = contentBgColor;
  }

  public void paint(Graphics graphics, JComponent component) {

    int width = component.getWidth() - 1;
    int innerWidth = width - 2;
    int height = component.getHeight() - 1;

    Graphics2D g2 = (Graphics2D)graphics;

    g2.setColor(contentBgColor);
    g2.fillRect(innerWidth, 0, width, height);

    g2.setColor(component.getBackground());
    g2.fillRect(1, 1, innerWidth -1, height - 1);
    
    g2.setColor(innerBorderColor);
    g2.drawLine(innerWidth, 0, innerWidth, height - 1);
    g2.drawLine(innerWidth, 0, width, 0);
    g2.drawLine(innerWidth, height - 1, width, height);

    g2.setColor(outerBorderColor);
    g2.drawLine(0, 0, innerWidth - 1, 0);
    g2.drawLine(0, 0, 0, height);
    g2.drawLine(0, height, innerWidth - 1, height);
  }

}
