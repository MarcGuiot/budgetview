package org.globsframework.gui.splits.color;

import java.awt.*;

public class Colors {
  public static final String HEXA_PREFIX = "#";

  private Colors() {
  }

  public static String toString(Color color) {
    if (color == null) {
      return "";
    }
    return Integer.toHexString(color.getRGB()).substring(2);
  }

  public static Color toColor(String hexaString) throws NumberFormatException {
    if ((hexaString == null) || "".equals(hexaString)) {
      return null;
    }
    return new Color(Integer.parseInt(hexaString, 16));
  }
}
