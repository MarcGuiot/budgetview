package com.budgetview.gui.addons.utils;

import com.budgetview.gui.components.layout.CustomLayout;
import org.globsframework.utils.exceptions.UnexpectedApplicationState;

import java.awt.*;

public class AddOnPanelLayout extends CustomLayout {

  private static final int HORIZONTAL_MARGIN = 5;
  private static final int MIN_DESCRIPTION_WIDTH = 40;
  private static final int VERTICAL_MARGIN = 10;
  private static final int LABEL_MIN_WIDTH = 190;
  private static final int IMAGE_MIN_WIDTH = 190;
  private static final int IMAGE_MIN_HEIGHT = 100;
  private static final int SEPARATOR_MARGIN = 10;
  private static final int SEPARATOR_HEIGHT = 2;

  private Component image;
  private Component label;
  private Component cards;
  private Component description;
  private Component separator;

  public void addLayoutComponent(String name, Component comp) {
  }

  public void removeLayoutComponent(Component comp) {
  }

  public int getPreferredWidth() {
    return Integer.MAX_VALUE;
  }

  public int getPreferredHeight() {
    return getMinHeight();
  }

  public int getMinWidth() {
    return getLabelWidth() + image.getWidth() + MIN_DESCRIPTION_WIDTH + 4 * HORIZONTAL_MARGIN;
  }

  public int getMinHeight() {
    return getImageHeight() + 2 * SEPARATOR_MARGIN + SEPARATOR_HEIGHT;
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

  public void init(Container parent) {
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
  }

  public void layoutComponents(int top, int bottom, int left, int right, int width, int height) {

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
    int imageTop = top + height / 2 - imageHeight / 2;
    int imageRight = labelRight + HORIZONTAL_MARGIN + imageMaxWidth;
    image.setBounds(imageLeft, imageTop, imageRealWidth, imageHeight);

    int descriptionLeft = imageRight + HORIZONTAL_MARGIN;
    int descriptionWidth = width - descriptionLeft;
    int descriptionHeight = innerHeight;
    int descriptionTop = top;
    description.setBounds(descriptionLeft, descriptionTop, descriptionWidth, descriptionHeight);
  }
}
