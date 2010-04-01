package org.globsframework.gui.splits.layout;

import org.globsframework.gui.splits.exceptions.SplitsException;
import org.globsframework.utils.exceptions.ItemNotFound;

import java.awt.*;

public enum Anchor {
  CENTER("center", GridBagConstraints.CENTER),
  EAST("east", GridBagConstraints.EAST),
  WEST("west", GridBagConstraints.WEST),
  NORTH("north", GridBagConstraints.NORTH),
  NORTHEAST("northeast", GridBagConstraints.NORTHEAST),
  NORTHWEST("northwest", GridBagConstraints.NORTHWEST),
  SOUTH("south", GridBagConstraints.SOUTH),
  SOUTHEAST("southeast", GridBagConstraints.SOUTHEAST),
  SOUTHWEST("southwest", GridBagConstraints.SOUTHWEST);

  private String name;
  private int value;

  Anchor(String name, int value) {
    this.name = name;
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

    Anchor[] anchors = Anchor.values();
    for (Anchor value : anchors) {
      if (anchor.equalsIgnoreCase(value.name)) {
        return value;
      }
    }
    throw new SplitsException("No enum Anchor found for value: " + anchor);
  }

  public static Anchor get(int value) {
    for (Anchor anchor : values()) {
      if (anchor.value == value) {
        return anchor;
      }
    }
    throw new ItemNotFound("No anchor defined for value: " + value);
  }
}
