package org.designup.picsou.gui.signpost.utils;

import org.globsframework.utils.exceptions.UnexpectedApplicationState;

import java.awt.*;

public class SignpostSectionLayout implements LayoutManager {

  private static final int MIN_DESCRIPTION_HEIGHT = 40;

  private boolean initialized = false;
  private Component title;
  private Component description;
  private Component button;

  private static final int HORIZONTAL_MARGIN = 15;
  private static final int TOP_MARGIN = 20;
  private static final int VERTICAL_MARGIN = 10;
  private static final int BOTTOM_MARGIN = 30;

  public void addLayoutComponent(String name, Component comp) {
  }

  public void removeLayoutComponent(Component comp) {
  }

  public Dimension preferredLayoutSize(Container parent) {
    if (!initialized) {
      init(parent);
    }
    return new Dimension(Integer.MAX_VALUE, getMinHeight());
  }

  public Dimension minimumLayoutSize(Container parent) {
    if (!initialized) {
      init(parent);
    }
    return new Dimension(getMinWidth(), getMinHeight());
  }

  private int getMinHeight() {
    return title.getPreferredSize().height + MIN_DESCRIPTION_HEIGHT + button.getPreferredSize().height +
           TOP_MARGIN + BOTTOM_MARGIN + 2 * VERTICAL_MARGIN;
  }

  private int getMinWidth() {
    return title.getPreferredSize().width +
           2 * HORIZONTAL_MARGIN;
  }

  private void init(Container parent) {
    for (Component component : parent.getComponents()) {
      if (component.getName().equals("sectionTitle")) {
        title = component;
      }
      else if (component.getName().equals("sectionDescription")) {
        description = component;
      }
      else if (component.getName().equals("sectionButton")) {
        button = component;
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
    int width = parent.getSize().width;
    int height = parent.getSize().height;

    int componentLeft = left + HORIZONTAL_MARGIN;
    int componentWidth = width - 2 * HORIZONTAL_MARGIN;

    int titleHeight = title.getPreferredSize().height;
    int buttonHeight = button.getPreferredSize().height;
    int descriptionHeight = height - titleHeight - buttonHeight - TOP_MARGIN - BOTTOM_MARGIN - 2 * VERTICAL_MARGIN;

    int titleTop = top + TOP_MARGIN;
    int titleWidth = title.getPreferredSize().width;
    int titleLeft = componentLeft;
    title.setBounds(titleLeft, titleTop, titleWidth, titleHeight);

    int descriptionTop = titleTop + titleHeight + VERTICAL_MARGIN;
    description.setBounds(componentLeft, descriptionTop, componentWidth, descriptionHeight);

    int buttonTop = descriptionTop + descriptionHeight + VERTICAL_MARGIN;
    int buttonWidth = button.getPreferredSize().width;
    int buttonLeft = width / 2 - buttonWidth / 2;
    button.setBounds(buttonLeft, buttonTop, buttonWidth, buttonHeight);
  }
}
