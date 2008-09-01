package org.designup.picsou.gui.plaf;

import org.globsframework.gui.splits.color.Colors;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;

public class ButtonPanelItemUI extends BasicButtonUI {

  private static final int ICON_WIDTH = 20;
  private static final int ICON_HEIGHT = 16;

  public void installUI(JComponent component) {
    super.installUI(component);

    AbstractButton button = (AbstractButton)component;
    button.setText(null);
    button.setOpaque(true);
    button.setBorderPainted(false);
    Dimension size = new Dimension(ICON_WIDTH + 3, ICON_HEIGHT + 5);
    button.setMinimumSize(size);
    button.setPreferredSize(size);
    button.setMaximumSize(size);
  }

  public void paint(Graphics g, JComponent component) {
    Graphics2D g2 = (Graphics2D)g;

    g2.setColor(Colors.toColor("cecece"));
    g2.drawLine(0, 0, ICON_WIDTH + 2, 0);

    g2.setColor(Colors.toColor("7e7e7e"));
    g2.drawRect(0, 1, ICON_WIDTH + 2, ICON_HEIGHT + 2);

    AbstractButton button = (AbstractButton)component;
    boolean isPressed =  button.getModel().isArmed() || button.getModel().isPressed();

    g2.setColor(Colors.toColor(isPressed ? "b8b8b8" : "fbfbfb"));
    g2.fillRect(1, 2, ICON_WIDTH + 1, ICON_HEIGHT / 2);

    g2.setColor(Colors.toColor(isPressed ? "a0a0a0" : "f1f1f1"));
    g2.fillRect(1, 2 + ICON_HEIGHT / 2, ICON_WIDTH + 1, ICON_HEIGHT - ((ICON_HEIGHT - 2) / 2));

    g2.setColor(Colors.toColor("f0f0f0"));
    g2.drawLine(0, ICON_HEIGHT + 4, ICON_WIDTH + 4, ICON_HEIGHT + 4);

    Icon icon = button.isEnabled() ? button.getIcon() : button.getDisabledIcon();
    icon.paintIcon(button, g, 1, 1);
  }
}
