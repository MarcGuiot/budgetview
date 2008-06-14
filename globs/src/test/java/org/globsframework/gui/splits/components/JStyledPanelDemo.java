package org.globsframework.gui.splits.components;

import org.globsframework.gui.splits.utils.GuiUtils;

import javax.swing.*;
import java.awt.*;

public class JStyledPanelDemo {
  public static void main(String[] args) {
    JStyledPanel panel = new JStyledPanel();
    panel.setTopColor(Color.white.darker());
    panel.setBottomColor(Color.white);
    panel.setBorderColor(Color.RED);
    panel.setBorderWidth(1);
    panel.setCornerRadius(10);
    panel.setShadowWidth(20);
    panel.setDistance(4);

    JLabel label = new JLabel("Blah");
    label.setForeground(Color.BLUE);
    panel.add(label);

    GuiUtils.show(panel);
  }
}
