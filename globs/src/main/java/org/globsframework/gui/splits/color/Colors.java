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
    if ((hexaString.length() > 1) && hexaString.startsWith("#")) {
      hexaString = hexaString.substring(1);
    }
    return new Color(Integer.parseInt(hexaString, 16));
  }

  public static boolean isHexaString(String text) {
    return text.startsWith(Colors.HEXA_PREFIX);
  }
}
