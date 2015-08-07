package org.designup.picsou.gui.components.layout;

import java.awt.*;

public abstract class CustomLayout implements LayoutManager {
  private boolean initialized = false;

  public void addLayoutComponent(String name, Component comp) {
  }

  public void removeLayoutComponent(Component comp) {
  }

  public Dimension preferredLayoutSize(Container parent) {
    if (!initialized) {
      init(parent);
      initialized = true;
    }
    return new Dimension(getPreferredWidth(), getPreferredHeight());
  }

  public Dimension minimumLayoutSize(Container parent) {
    if (!initialized) {
      init(parent);
      initialized = true;
    }
    return new Dimension(getMinWidth(), getMinHeight());
  }

  public abstract int getPreferredWidth();

  public abstract int getPreferredHeight();

  protected abstract int getMinHeight();

  protected abstract int getMinWidth();

  protected abstract void init(Container parent);

  public void layoutContainer(Container parent) {
    if (!initialized) {
      init(parent);
    }

    Insets insets = parent.getInsets();
    int width = parent.getSize().width;
    int height = parent.getSize().height;
    int top = insets.top;
    int bottom = height - insets.bottom;
    int left = insets.left;
    int right = width - insets.right;

    layoutComponents(top, bottom, left, right, width, height);
  }

  public abstract void layoutComponents(int top, int bottom, int left, int right, int width, int height);

  protected static int centeredLeft(int left, Component component, int right) {
    return left + (right - left) / 2 - component.getPreferredSize().width / 2;
  }

  protected static int centeredTop(int top, Component component, int bottom) {
    return top + (bottom - top) / 2 - component.getPreferredSize().height / 2;
  }

  protected static int width(Component component) {
    return component.getPreferredSize().width;
  }

  protected static int height(Component component) {
    return component.getPreferredSize().height;
  }

  public void layout(Component component, int left, int top) {
    component.setBounds(left, top, component.getPreferredSize().width, component.getPreferredSize().height);
  }

  public void layout(Component component, int left, int top, int width, int height) {
    component.setBounds(left, top, width, height);
  }
}
