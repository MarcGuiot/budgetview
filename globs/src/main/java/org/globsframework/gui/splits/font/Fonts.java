package org.globsframework.gui.splits.font;

import org.globsframework.utils.exceptions.InvalidFormat;
import org.globsframework.utils.exceptions.InvalidParameter;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Fonts {

  static Font DEFAULT_LABEL_FONT = new JLabel().getFont();

  static final String FONT_ERROR_MESSAGE =
    "Font should be defined like 'Arial,plain,12' or 'Courier,italic,8'";

  private static Pattern FONT_FORMAT = Pattern.compile("([A-z0-9 _]+),([a-z]+),([0-9]+)");
  private static Pattern DERIVED_FONT_FORMAT = Pattern.compile("-,([a-z]+),([0-9]+)");

  private static Map<String, Font> baseFonts = new ConcurrentHashMap<String, Font>();
  private static Map<String, Font> fonts = new ConcurrentHashMap<String, Font>();

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
    if (fonts.containsKey(trimmed)) {
      return fonts.get(trimmed);
    }
    Matcher completeMatcher = FONT_FORMAT.matcher(trimmed);
    if (completeMatcher.matches()) {
      String baseName = completeMatcher.group(1);
      int style = getFontStyle(completeMatcher.group(2));
      int size = Integer.parseInt(completeMatcher.group(3));
      Font font;
      if (baseFonts.containsKey(baseName)) {
        font = baseFonts.get(baseName).deriveFont(style, size);
      }
      else {
        font = new Font(baseName, style, size);
      }
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

  public static void loadBase(String fontName, String fileName, Class baseClass) throws RuntimeException {
    try {
      InputStream stream = baseClass.getResourceAsStream(fileName);
      Font font = Font.createFont(Font.TRUETYPE_FONT, stream);
      baseFonts.put(fontName, font);
    }
    catch (FontFormatException e) {
      throw new RuntimeException(e);
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static void setDefault(String description) {
    DEFAULT_LABEL_FONT = parseFont(description);
  }
}
