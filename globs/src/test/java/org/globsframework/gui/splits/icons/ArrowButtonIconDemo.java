package org.globsframework.gui.splits.icons;

import org.globsframework.gui.splits.layout.GridBagBuilder;
import org.globsframework.gui.splits.utils.GuiUtils;

import javax.swing.*;
import java.awt.*;

public class ArrowButtonIconDemo {

  public static void main(String[] args) {
    JPanel panel =
      GridBagBuilder.init()
        .add(createIcon(), 0, 0)
        .getPanel();
    panel.setOpaque(true);
    panel.setBackground(Color.WHITE);
    GuiUtils.showCentered(panel);
  }

  private static JLabel createIcon() {
    ArrowButtonIcon icon = new ArrowButtonIcon(40, 40);
    icon.setColor(Color.RED);
    return new JLabel(icon);
  }
}
