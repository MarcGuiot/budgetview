package org.globsframework.gui.views;

import javax.swing.*;

public enum Alignment {
  CENTER(JLabel.CENTER),
  RIGHT(JLabel.RIGHT),
  LEFT(JLabel.LEFT);

  private int value;

  Alignment(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }
}
