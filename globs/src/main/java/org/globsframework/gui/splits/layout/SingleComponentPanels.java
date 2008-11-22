package org.globsframework.gui.splits.layout;

import javax.swing.*;
import java.awt.*;

public abstract class SingleComponentPanels {

  public static Container create(Component component, Insets insets) {
    return install(new JPanel(), component, insets);
  }

  public static JPanel install(JPanel containingPanel, Component component) {
    return install(containingPanel, component, new Insets(0, 0, 0, 0));
  }

  public static JPanel install(JPanel containingPanel, Component component, Insets insets) {
    containingPanel.removeAll();
    containingPanel.setLayout(new SingleComponentLayout(insets));
    containingPanel.add(component);
    containingPanel.setOpaque(false);
    return containingPanel;
  }

}
