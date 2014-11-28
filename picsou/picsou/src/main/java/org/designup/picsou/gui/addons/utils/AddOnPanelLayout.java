package org.designup.picsou.gui.addons.utils;

import org.globsframework.utils.exceptions.UnexpectedApplicationState;

import java.awt.*;

public class AddOnPanelLayout implements LayoutManager {

  private static final int HEIGHT = 30;
  private static final int HORIZONTAL_MARGIN = 5;
  private static final int MIN_DESCRIPTION_WIDTH = 40;
  private static final int VERTICAL_MARGIN = 10;
  private static final int LABEL_MIN_WIDTH = 190;
  private static final int IMAGE_MIN_WIDTH = 190;
  private static final int IMAGE_MIN_HEIGHT = 100;
  private static final int SEPARATOR_MARGIN = 10;
  private static final int SEPARATOR_HEIGHT = 2;

  private boolean initialized = false;
  private Component image;
  private Component label;
  private Component cards;
  private Component description;
  private Component separator;

  public void addLayoutComponent(String name, Component comp) {
  }

  public void removeLayoutComponent(Component comp) {
  }

  public Dimension preferredLayoutSize(Container parent) {
    if (!initialized) {
      init(parent);
    }
    return new Dimension(Integer.MAX_VALUE, getImageHeight() + 2 * SEPARATOR_MARGIN + SEPARATOR_HEIGHT);
  }

  public Dimension minimumLayoutSize(Container parent) {
    if (!initialized) {
      init(parent);
    }
    return new Dimension(getMinWidth(), getImageHeight() + 2 * SEPARATOR_MARGIN + SEPARATOR_HEIGHT);
  }

  public int getMinWidth() {
    return getLabelWidth() + image.getWidth() + MIN_DESCRIPTION_WIDTH + 4 * HORIZONTAL_MARGIN;
  }

  public int getLabelWidth() {
    return Math.max(label.getPreferredSize().width, LABEL_MIN_WIDTH);
  }

  public int getImageWidth() {
    return Math.max(image.getPreferredSize().width, IMAGE_MIN_WIDTH);
  }

  public int getImageHeight() {
    return Math.max(image.getPreferredSize().height, IMAGE_MIN_HEIGHT);
  }

  private void init(Container parent) {
    for (Component component : parent.getComponents()) {
      if (component.getName().contains("image")) {
        image = component;
      }
      else if (component.getName().equals("label")) {
        label = component;
      }
      else if (component.getName().equals("cards")) {
        cards = component;
      }
      else if (component.getName().equals("description")) {
        description = component;
      }
      else if (component.getName().equals("separator")) {
        separator = component;
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
    int height = parent.getSize().height - 1;
    int width = parent.getSize().width - 1;

    separator.setBounds(left, height - SEPARATOR_MARGIN - SEPARATOR_HEIGHT, width, SEPARATOR_HEIGHT);
    int innerHeight = height - 2 * SEPARATOR_MARGIN - SEPARATOR_HEIGHT;

    int labelLeft = left;
    int labelTop = top;
    int labelWidth = getLabelWidth();
    int labelHeight = label.getPreferredSize().height;
    int labelRight = labelLeft + labelWidth;
    label.setBounds(labelLeft, labelTop, labelWidth, labelHeight);

    int cardsLeft = left;
    int cardsTop = label.getPreferredSize().height + VERTICAL_MARGIN;
    cards.setBounds(cardsLeft, cardsTop, labelWidth, innerHeight - labelHeight);

    int imageMaxWidth = getImageWidth();
    int imageRealWidth = image.getPreferredSize().width;
    int imageLeft = labelRight + HORIZONTAL_MARGIN + imageMaxWidth / 2 - imageRealWidth / 2;
    int imageHeight = image.getPreferredSize().height;
    int imageTop = top;
    int imageRight = labelRight + HORIZONTAL_MARGIN + imageMaxWidth;
    image.setBounds(imageLeft, imageTop, imageRealWidth, imageHeight);

    int descriptionLeft = imageRight + HORIZONTAL_MARGIN;
    int descriptionWidth = width - descriptionLeft;
    int descriptionHeight = innerHeight;
    int descriptionTop = top;
    description.setBounds(descriptionLeft, descriptionTop, descriptionWidth, descriptionHeight);
  }
}
