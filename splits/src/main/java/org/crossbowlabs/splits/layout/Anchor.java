package org.crossbowlabs.splits.layout;

import java.awt.*;

public enum Anchor {
  CENTER(GridBagConstraints.CENTER),
  EAST(GridBagConstraints.EAST),
  WEST(GridBagConstraints.WEST),
  NORTH(GridBagConstraints.NORTH),
  NORTHEAST(GridBagConstraints.NORTHEAST),
  NORTHWEST(GridBagConstraints.NORTHWEST),
  SOUTH(GridBagConstraints.SOUTH),
  SOUTHEAST(GridBagConstraints.SOUTHEAST),
  SOUTHWEST(GridBagConstraints.SOUTHWEST);

  private int value;

  Anchor(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }
}
