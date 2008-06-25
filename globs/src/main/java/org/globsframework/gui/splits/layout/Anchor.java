package org.globsframework.gui.splits.layout;

import org.globsframework.utils.Utils;

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

  public static Anchor get(String anchor) {
    if ("right".equalsIgnoreCase(anchor)) {
      return EAST;
    }
    if ("left".equalsIgnoreCase(anchor)) {
      return WEST;
    }
    if ("top".equalsIgnoreCase(anchor)) {
      return NORTH;
    }
    if ("bottom".equalsIgnoreCase(anchor)) {
      return SOUTH;
    }

    return Utils.toEnum(Anchor.class, anchor);
  }
}
