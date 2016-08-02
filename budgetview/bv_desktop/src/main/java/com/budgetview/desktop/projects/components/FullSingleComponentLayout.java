package com.budgetview.desktop.projects.components;

import javax.swing.*;
import java.awt.*;

public class FullSingleComponentLayout implements LayoutManager {

  private JPanel container;

  public FullSingleComponentLayout(JPanel container) {
    this.container = container;
  }

  public void addLayoutComponent(String name, Component comp) {
  }

  public void removeLayoutComponent(Component comp) {
  }

  public Dimension preferredLayoutSize(Container container) {
    if (container.getComponentCount() == 0) {
      return new Dimension(0, 0);
    }

    return new Dimension(Integer.MAX_VALUE, container.getComponent(0).getPreferredSize().height);
  }

  public Dimension minimumLayoutSize(Container container) {
    if (container.getComponentCount() == 0) {
      return new Dimension(0, 0);
    }

    return container.getComponent(0).getMinimumSize();
  }

  public void layoutContainer(Container container) {
    if (container.getComponentCount() == 0) {
      return;
    }
    Component component = container.getComponent(0);
    component.setBounds(container.getBounds());
  }
}
