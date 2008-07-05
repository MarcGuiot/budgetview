package org.globsframework.gui.splits.components;

import org.globsframework.gui.splits.utils.GuiUtils;

import javax.swing.*;
import java.awt.*;

public class JStyledPanelDemo {
  public static void main(String[] args) {
    StyledPanelUI ui = new StyledPanelUI();
    ui.setTopColor(Color.white.darker());
    ui.setBottomColor(Color.white);
    ui.setBorderColor(Color.RED);
    ui.setBorderWidth(1);
    ui.setCornerRadius(10);
    ui.setShadowWidth(20);
    ui.setDistance(4);

    JPanel panel = new JPanel();

    JLabel label = new JLabel("Blah");
    label.setForeground(Color.BLUE);
    panel.add(label);

    GuiUtils.show(panel);
  }
}
