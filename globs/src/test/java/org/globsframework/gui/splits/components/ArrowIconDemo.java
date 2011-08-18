package org.globsframework.gui.splits.components;

import org.globsframework.gui.splits.utils.GuiUtils;

import javax.swing.*;
import java.awt.*;

public class ArrowIconDemo {
  public static void main(String[] args) {

    ArrowIcon icon = new ArrowIcon(180, 180, 80, 100, ArrowIcon.Orientation.LEFT);
    icon.setColor(Color.RED);

    JPanel panel = new JPanel();
    JLabel jLabel = new JLabel(icon);
    jLabel.setOpaque(true);
    jLabel.setBackground(Color.BLACK);
    panel.add(jLabel);
    GuiUtils.show(panel);
  }
}
