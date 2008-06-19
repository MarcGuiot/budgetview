package org.globsframework.gui.splits.font;

import org.globsframework.utils.exceptions.InvalidFormat;
import org.globsframework.utils.exceptions.InvalidParameter;

import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Fonts {
  private static Pattern FONT_FORMAT = Pattern.compile("([A-z0-9 _]+),([a-z]+),([0-9]+)");

  static final String FONT_ERROR_MESSAGE =
    "Font should be defined like 'Arial,plain,12' or 'Courier,italic,8'";

  public static Font parseFont(String value, FontLocator locator) throws InvalidParameter {
    if (value.startsWith("$")) {
      if (locator == null) {
        throw new InvalidParameter("Cannot resolve font '" + value + "' with no FontLocator");
      }
      return locator.get(value.substring(1));
    }
    return parseFont(value);
  }

  public static Font parseFont(String desc) throws InvalidFormat {
    String trimmed = desc.trim();
    Matcher shortMatcher = FONT_FORMAT.matcher(trimmed);
    if (!shortMatcher.matches()) {
      throw new InvalidFormat(FONT_ERROR_MESSAGE);
    }
    return new Font(shortMatcher.group(1),
                    getFontStyle(shortMatcher.group(2)),
                    Integer.parseInt(shortMatcher.group(3)));
  }

  private static int getFontStyle(String text) throws InvalidFormat {
    if ("plain".equalsIgnoreCase(text)) {
      return Font.PLAIN;
    }
    if ("bold".equalsIgnoreCase(text)) {
      return Font.BOLD;
    }
    if ("italic".equalsIgnoreCase(text)) {
      return Font.ITALIC;
    }
    throw new InvalidFormat("Unknown font style '" + text + "' - should be one of plain|bold|italic");
  }
}
