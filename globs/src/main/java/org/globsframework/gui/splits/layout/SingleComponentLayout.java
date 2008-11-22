package org.globsframework.gui.splits.layout;

import org.globsframework.gui.splits.exceptions.SplitsException;

import java.awt.*;

public class SingleComponentLayout implements LayoutManager {
  private static final Insets NULL_INSETS = new Insets(0, 0, 0, 0);
  private Insets insets = NULL_INSETS;

  public SingleComponentLayout(Insets insets) {
    if (insets != null) {
      this.insets = insets;
    }
  }

  public void addLayoutComponent(String name, Component comp) {
  }

  public void removeLayoutComponent(Component comp) {
  }

  public Dimension preferredLayoutSize(Container parent) {
    Component component = getSingleComponent(parent);
    Dimension size = component.getPreferredSize();
    size.width += insets.right + insets.left;
    size.height += insets.top + insets.bottom;
    return size;
  }

  public Dimension minimumLayoutSize(Container parent) {
    Component component = getSingleComponent(parent);
    Dimension size = component.getMinimumSize();
    size.width += insets.right + insets.left;
    size.height += insets.top + insets.bottom;
    return size;
  }

  public void layoutContainer(Container parent) {
    Component component = getSingleComponent(parent);
    component.setBounds(insets.left, insets.top,
                            parent.getWidth() - insets.right - insets.left,
                            parent.getHeight() - insets.top - insets.bottom);
  }

  private Component getSingleComponent(Container parent) {
    Component[] components = parent.getComponents();
    if (components.length != 1) {
      throw new SplitsException(components.length + " subcomponents but only 1 is expected");
    }
    return components[0];
  }

  public Insets getInsets() {
    return insets;
  }
}
