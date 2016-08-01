package com.budgetview.gui.components.ui;

import org.globsframework.gui.splits.utils.GuiUtils;

import javax.swing.*;
import java.awt.*;

public class OnOffToggleUI extends SlidingToggleUI {

  protected void drawSelectedSign(Graphics2D g2, int xOffset, int width, int height) {
    g2.drawLine(width / 2, height / 4, width / 2, height - (height / 4));
  }

  protected void drawUnselectedSign(Graphics2D g2, int xOffset, int width, int height) {
    float diameter = width * 0.5f;
    float left = xOffset + width * 0.3f;
    float top = height / 2 - diameter / 2;
    g2.drawOval((int)left, (int)top, (int)diameter, (int)diameter);
  }

  public static void main(String[] args) {
    JToggleButton button = new JToggleButton();
    button.setUI(new OnOffToggleUI());
    GuiUtils.show(button);
  }
}
