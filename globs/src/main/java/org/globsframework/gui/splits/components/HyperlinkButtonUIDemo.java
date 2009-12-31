package org.globsframework.gui.splits.components;

import org.globsframework.gui.splits.color.utils.ColorRectIcon;
import org.globsframework.gui.splits.layout.Anchor;
import org.globsframework.gui.splits.layout.Fill;
import org.globsframework.gui.splits.layout.GridBagBuilder;
import org.globsframework.gui.splits.utils.GuiUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class HyperlinkButtonUIDemo {

  public static void main(String[] args) {

    final AbstractAction action = new AbstractAction("toto") {
      public void actionPerformed(ActionEvent e) {
        System.out.println("HyperlinkButtonUI.actionPerformed: ");
      }
    };
    final JButton button = new JButton(action);
    button.setText("hello");
    button.setIcon(new ColorRectIcon(50, 10, Color.RED));
    button.setUI(new HyperlinkButtonUI());

    JPanel panel =
      GridBagBuilder.init()
        .add(button, 0, 0, 1, 1, 1, 1, Fill.NONE, Anchor.CENTER)
        .add(new JButton(new AbstractAction("disable") {
          public void actionPerformed(ActionEvent e) {
            final boolean newState = !button.isEnabled();
            action.setEnabled(newState);
            putValue(Action.NAME, newState ? "disable" : "enable");
          }
        }), 0, 1, 1, 1, 1, 1, Fill.NONE, Anchor.CENTER)
        .getPanel();
    panel.setOpaque(true);
    panel.setBackground(Color.CYAN);
    GuiUtils.show(panel);
  }
}