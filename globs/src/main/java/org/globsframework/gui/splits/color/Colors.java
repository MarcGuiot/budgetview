package org.globsframework.gui.splits.color;

import org.globsframework.utils.exceptions.InvalidParameter;

import java.awt.*;

public class Colors {
  public static final String HEXA_PREFIX = "#";
  private static final int LABEL_SWITCH_LIMIT = 180;

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

  public static Color brighten(Color color, float factor) {
    if ((factor < 0) || (factor > 1.0)) {
      throw new InvalidParameter("Invalid factor " + factor + " - should be between 0.0 and 1.0");
    }
    if (Math.abs(1 - factor) < 0.05) {
      return color;
    }
    int red = color.getRed();
    int green = color.getGreen();
    int blue = color.getBlue();

    int adjustedRed = brigthen(factor, red);
    int adjustedGreen = brigthen(factor, green);
    int adjustedBlue = brigthen(factor, blue);

    return new Color(adjustedRed, adjustedGreen, adjustedBlue);
  }

  private static int brigthen(float factor, int color) {
    float distance = factor * (255 - color);
    return (int)(color + distance);
  }

  public static  Color getLabelColor(Color background, Color lightLabelColor, Color darkLabelColor) {
    int luminance = getLuminance(background);
    return luminance < LABEL_SWITCH_LIMIT ? lightLabelColor : darkLabelColor;
  }

  public static int getLuminance(Color background) {
    int red = background.getRed();
    int green = background.getGreen();
    int blue = background.getBlue();
    return (int)(0.2126 * red + 0.7152 * blue + 0.0722 * green);
  }
}
