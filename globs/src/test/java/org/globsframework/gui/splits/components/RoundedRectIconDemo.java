package org.globsframework.gui.splits.components;

import org.globsframework.gui.splits.layout.GridBagBuilder;
import org.globsframework.gui.splits.utils.GuiUtils;

import javax.swing.*;
import java.awt.*;

public class RoundedRectIconDemo {
  public static void main(String[] args) {

    JPanel panel =
      GridBagBuilder.init()
        .add(createIcon(), 0, 0)
        .getPanel();
    panel.setOpaque(true);
    panel.setBackground(Color.WHITE);
    GuiUtils.show(panel);
  }

  private static JLabel createIcon() {
    RoundedRectIcon icon = new RoundedRectIcon(60, 40, 6, 6);
    icon.setBackgroundColor(Color.WHITE);
    icon.setBorderColor(Color.BLUE);
    return new JLabel(icon);
  }
}
