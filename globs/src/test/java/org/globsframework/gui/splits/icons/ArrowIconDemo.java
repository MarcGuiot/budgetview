package org.globsframework.gui.splits.icons;

import org.globsframework.gui.splits.layout.GridBagBuilder;
import org.globsframework.gui.splits.utils.GuiUtils;

import javax.swing.*;
import java.awt.*;

public class ArrowIconDemo {
  public static void main(String[] args) {

    JPanel panel =
      GridBagBuilder.init()
        .add(createArrow(ArrowIcon.Orientation.LEFT), 0, 0)
        .add(createArrow(ArrowIcon.Orientation.DOWN), 1, 0)
        .add(createArrow(ArrowIcon.Orientation.RIGHT), 0, 1)
        .add(createArrow(ArrowIcon.Orientation.UP), 1, 1)
        .getPanel();
    panel.setOpaque(true);
    panel.setBackground(Color.WHITE);
    GuiUtils.showCentered(panel);
  }

  private static JLabel createArrow(ArrowIcon.Orientation orientation) {
    ArrowIcon icon = new ArrowIcon(60, 40, 50, 20, orientation);
    icon.setColor(Color.RED);
    return new JLabel(icon);
  }
}
