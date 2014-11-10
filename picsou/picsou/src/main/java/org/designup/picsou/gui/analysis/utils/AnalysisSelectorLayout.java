package org.designup.picsou.gui.analysis.utils;

import org.globsframework.utils.exceptions.UnexpectedApplicationState;

import java.awt.*;

public class AnalysisSelectorLayout implements LayoutManager {

  private static final int HEIGHT = 30;
  private static final int HORIZONTAL_MARGIN = 5;
  private static final int TOGGLE_BUTTON_MARGIN = 10;

  private boolean initialized = false;
  private Component button;

  private Component toggle;

  public void addLayoutComponent(String name, Component comp) {
  }

  public void removeLayoutComponent(Component comp) {
  }

  public Dimension preferredLayoutSize(Container parent) {
    if (!initialized) {
      init(parent);
    }
    return new Dimension(Integer.MAX_VALUE, HEIGHT);
  }

  public Dimension minimumLayoutSize(Container parent) {
    if (!initialized) {
      init(parent);
    }
    return new Dimension(getMinWidth(), HEIGHT);
  }

  public int getMinWidth() {
    return button.getWidth() + toggle.getWidth() + 4 * HORIZONTAL_MARGIN;
  }

  private void init(Container parent) {
    for (Component component : parent.getComponents()) {
      if (component.getName().contains("selector:")) {
        button = component;
      }
      else if (component.getName().equals("arrow")) {
        toggle = component;
      }
      else {
        throw new UnexpectedApplicationState("Unexpected component found in layout: " + component);
      }
    }
    initialized = true;
  }

  public void layoutContainer(Container parent) {
    if (!initialized) {
      init(parent);
    }

    Insets insets = parent.getInsets();
    int top = insets.top;
    int left = insets.left;
    int height = parent.getSize().height;

    int toggleLeft = left + HORIZONTAL_MARGIN;
    int toggleTop = top + height / 2 - toggle.getPreferredSize().height / 2;
    toggle.setBounds(toggleLeft, toggleTop, toggle.getPreferredSize().width, toggle.getPreferredSize().height);

    int buttonLeft = toggleLeft + toggle.getPreferredSize().width + TOGGLE_BUTTON_MARGIN;
    int buttonTop = top + height / 2 - button.getPreferredSize().height / 2;
    button.setBounds(buttonLeft, buttonTop, button.getPreferredSize().width, button.getPreferredSize().height);
  }
}
