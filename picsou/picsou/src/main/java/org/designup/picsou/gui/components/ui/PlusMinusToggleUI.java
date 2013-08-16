package org.designup.picsou.gui.components.ui;

import org.globsframework.gui.splits.utils.GuiUtils;

import javax.swing.*;
import java.awt.*;

public class PlusMinusToggleUI extends SlidingToggleUI {

  protected void drawSelectedSign(Graphics2D g2, int xOffset, int width, int height) {
    g2.drawLine(width / 4, height / 2, width - (width / 4), height / 2);
    g2.drawLine(width / 2, height / 4, width / 2, height - (height / 4));
  }

  protected void drawUnselectedSign(Graphics2D g2, int xOffset, int width, int height) {
    float left = xOffset + width * 0.3f;
    float right = xOffset + width - width * 0.3f;
    int y = height / 2;
    g2.drawLine((int)left, y, (int)right, y);
  }

  public static void main(String[] args) {
    JToggleButton button = new JToggleButton();
    button.setUI(new PlusMinusToggleUI());
    GuiUtils.show(button);
  }
}
