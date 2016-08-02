package com.budgetview.desktop.signpost.utils;

import com.budgetview.desktop.components.layout.CustomLayout;
import org.globsframework.utils.exceptions.UnexpectedApplicationState;

import java.awt.*;

public class SignpostSectionLayout extends CustomLayout {

  private Component title;
  private Component description;
  private Component button;

  private static final int MIN_DESCRIPTION_HEIGHT = 40;
  private static final int HORIZONTAL_MARGIN = 15;
  private static final int TOP_MARGIN = 20;
  private static final int VERTICAL_MARGIN = 10;
  private static final int BOTTOM_MARGIN = 20;

  public int getPreferredWidth() {
    return Integer.MAX_VALUE;
  }

  public int getPreferredHeight() {
    return getMinHeight();
  }

  public int getMinHeight() {
    return TOP_MARGIN +
           height(title) +
           VERTICAL_MARGIN +
           MIN_DESCRIPTION_HEIGHT +
           VERTICAL_MARGIN +
           height(button) +
           BOTTOM_MARGIN ;
  }

  public int getMinWidth() {
    return title.getPreferredSize().width +
           2 * HORIZONTAL_MARGIN;
  }

  public void init(Container parent) {
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
  }

  public void layoutComponents(int top, int bottom, int left, int right, int width, int height) {

    int componentLeft = left + HORIZONTAL_MARGIN;
    int componentRight = right - HORIZONTAL_MARGIN;
    int componentWidth = componentRight - componentLeft;

    int titleHeight = title.getPreferredSize().height;
    int buttonHeight = button.getPreferredSize().height;

    int titleTop = top + TOP_MARGIN;
    int titleWidth = title.getPreferredSize().width;
    layout(title, componentLeft, titleTop, titleWidth, titleHeight);

    int buttonTop = bottom - BOTTOM_MARGIN - height(button);
    int buttonWidth = width(button);
    int buttonLeft = width / 2 - buttonWidth / 2;
    layout(button, buttonLeft, buttonTop, buttonWidth, buttonHeight);

    int descriptionTop = titleTop + titleHeight + VERTICAL_MARGIN;
    int descriptionHeight = buttonTop - VERTICAL_MARGIN - descriptionTop;
    layout(description, componentLeft, descriptionTop, componentWidth, descriptionHeight);
  }
}
