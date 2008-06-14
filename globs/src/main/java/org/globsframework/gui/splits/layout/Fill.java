package org.globsframework.gui.splits.layout;

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
}
