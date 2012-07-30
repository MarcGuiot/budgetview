package org.globsframework.gui.splits.components;

import org.globsframework.gui.splits.layout.Anchor;
import org.globsframework.gui.splits.layout.Fill;
import org.globsframework.gui.splits.layout.GridBagBuilder;
import org.globsframework.gui.splits.utils.GuiUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class HyperlinkButtonUIDemo {

  public static void main(String[] args) {

    JPanel panel =
      GridBagBuilder.init()
        .add(createButton(SwingConstants.LEFT),
             0, 0, 1, 1, 1, 1, Fill.NONE, Anchor.CENTER)
        .add(createButton(SwingConstants.RIGHT),
             0, 1, 1, 1, 1, 1, Fill.NONE, Anchor.CENTER)
        .getPanel();
    panel.setOpaque(true);
    panel.setBackground(Color.WHITE);
    GuiUtils.show(panel);
  }

  private static JButton createButton(int horizontalTextPosition) {
    final AbstractAction action = new AbstractAction("toto") {
      public void actionPerformed(ActionEvent e) {
        System.out.println("HyperlinkButtonUI.actionPerformed: ");
      }
    };
    action.setEnabled(true);

    final JButton button = new JButton(action);
    button.setText("hello");
    button.setIconTextGap(0);

    ArrowIcon icon = new ArrowIcon(15, 15, ArrowIcon.Orientation.RIGHT);
    icon.setColor(Color.RED);
    button.setIcon(icon);
    button.setUI(new HyperlinkButtonUI());


    button.setHorizontalTextPosition(horizontalTextPosition);

    return button;
  }
}