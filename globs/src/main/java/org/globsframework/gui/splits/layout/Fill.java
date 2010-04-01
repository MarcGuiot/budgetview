package org.globsframework.gui.splits.layout;

import org.globsframework.utils.exceptions.ItemNotFound;

import java.awt.*;

public enum Fill {
  HORIZONTAL(GridBagConstraints.HORIZONTAL),
  VERTICAL(GridBagConstraints.VERTICAL),
  BOTH(GridBagConstraints.BOTH),
  NONE(GridBagConstraints.NONE);

  private int value;

  Fill(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }

  public static Fill get(int value) {
    for (Fill fill : values()) {
      if (fill.value == value) {
        return fill;
      }
    }
    throw new ItemNotFound("No Fill defined for value: " + value);
  }
}
