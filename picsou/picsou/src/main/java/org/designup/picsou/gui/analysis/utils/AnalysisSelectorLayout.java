package org.designup.picsou.gui.analysis.utils;

import org.designup.picsou.gui.components.layout.CustomLayout;
import org.globsframework.utils.exceptions.UnexpectedApplicationState;

import java.awt.*;

public class AnalysisSelectorLayout extends CustomLayout {

  private static final int HEIGHT = 30;
  private static final int HORIZONTAL_MARGIN = 5;
  private static final int TOGGLE_BUTTON_MARGIN = 10;

  private Component button;
  private Component toggle;

  public int getPreferredWidth() {
    return Integer.MAX_VALUE;
  }

  public int getPreferredHeight() {
    return HEIGHT;
  }

  protected int getMinHeight() {
    return HEIGHT;
  }

  public int getMinWidth() {
    return button.getWidth() + toggle.getWidth() + 4 * HORIZONTAL_MARGIN;
  }

  public void init(Container parent) {
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
  }

  public void layoutComponents(int top, int bottom, int left, int right, int width, int height) {
    int toggleLeft = left + HORIZONTAL_MARGIN;
    int toggleTop = centeredTop(top, toggle, bottom);
    layout(toggle, toggleLeft, toggleTop);

    int buttonLeft = toggleLeft + toggle.getPreferredSize().width + TOGGLE_BUTTON_MARGIN;
    int buttonTop = centeredTop(top, button, bottom);
    layout(button, buttonLeft, buttonTop);
  }
}
