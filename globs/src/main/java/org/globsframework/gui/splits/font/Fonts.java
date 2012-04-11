package org.globsframework.gui.splits.font;

import org.globsframework.utils.exceptions.InvalidFormat;
import org.globsframework.utils.exceptions.InvalidParameter;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Fonts {
  static Font DEFAULT_LABEL_FONT = new JLabel().getFont();

  static final String FONT_ERROR_MESSAGE =
    "Font should be defined like 'Arial,plain,12' or 'Courier,italic,8'";

  private static Pattern FONT_FORMAT = Pattern.compile("([A-z0-9 _]+),([a-z]+),([0-9]+)");
  private static Pattern DERIVED_FONT_FORMAT = Pattern.compile("-,([a-z]+),([0-9]+)");

  public static Font parseFont(String value, FontLocator locator) throws InvalidParameter {
    if (value.startsWith("$")) {
      if (locator == null) {
        throw new InvalidParameter("Cannot resolve font '" + value + "' with no FontLocator");
      }
      return locator.get(value.substring(1));
    }
    return parseFont(value);
  }
  
  static Map<String, Font> fonts = new ConcurrentHashMap<String, Font>();

  public static Font parseFont(String desc) throws InvalidFormat {
    String trimmed = desc.trim();
    if (fonts.containsKey(trimmed)){
      return fonts.get(trimmed);
    }
    Matcher completeMatcher = FONT_FORMAT.matcher(trimmed);
    if (completeMatcher.matches()) {
      Font font = new Font(completeMatcher.group(1),
                           getFontStyle(completeMatcher.group(2)),
                           Integer.parseInt(completeMatcher.group(3)));
      fonts.put(trimmed, font);
      return font;
    }

    Matcher derivedMatcher = DERIVED_FONT_FORMAT.matcher(trimmed);
    if (derivedMatcher.matches()) {
      Font font = new Font(DEFAULT_LABEL_FONT.getFamily(),
                           getFontStyle(derivedMatcher.group(1)),
                           Integer.parseInt(derivedMatcher.group(2)));
      fonts.put(trimmed, font);
      return font;
    }

    throw new InvalidFormat(FONT_ERROR_MESSAGE);
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
