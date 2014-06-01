package org.globsframework.gui.splits.utils;

import org.globsframework.gui.splits.ImageLocator;
import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.color.ColorUpdater;
import org.globsframework.gui.splits.color.Colors;
import org.globsframework.gui.splits.components.*;
import org.globsframework.utils.Strings;

import javax.swing.*;
import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IconParser {

  private static Pattern ARROW_FORMAT = Pattern.compile("([a-z]+)Arrow\\(" +
                                                        "[ ]*([0-9]+)[ ]*," +
                                                        "[ ]*([0-9]+)[ ]*," +
                                                        "[ ]*([0-9]+)[ ]*," +
                                                        "[ ]*([0-9]+)[ ]*," +
                                                        "[ ]*([A-z\\.#0-9]+)[ ]*" +
                                                        "\\)");

  private static Pattern EMPTY_FORMAT = Pattern.compile("empty\\(" +
                                                        "[ ]*([0-9]+)[ ]*," +
                                                        "[ ]*([0-9]+)[ ]*" +
                                                        "\\)");

  private static Pattern CIRCLED_ARROW_FORMAT = Pattern.compile("circledArrow\\(" +
                                                                "[ ]*([A-z\\.#0-9]+)[ ]*" +
                                                                "\\)");

  private static Pattern RECT_FORMAT = Pattern.compile("rect\\(" +
                                                       "[ ]*([0-9]+)[ ]*," +
                                                       "[ ]*([0-9]+)[ ]*," +
                                                       "[ ]*([A-z\\.#0-9]+)[ ]*," +
                                                       "[ ]*([A-z\\.#0-9]+)[ ]*" +
                                                       "\\)");

  private static Pattern ROUNDED_RECT_FORMAT = Pattern.compile("roundedRect\\(" +
                                                               "[ ]*([0-9]+)[ ]*," +
                                                               "[ ]*([0-9]+)[ ]*," +
                                                               "[ ]*([0-9]+)[ ]*," +
                                                               "[ ]*([0-9]+)[ ]*," +
                                                               "[ ]*([A-z\\.#0-9]+)[ ]*," +
                                                               "[ ]*([A-z\\.#0-9]+)[ ]*" +
                                                               "\\)");

  private static Pattern OVAL_FORMAT = Pattern.compile("oval\\(" +
                                                       "[ ]*([0-9]+)[ ]*," +
                                                       "[ ]*([0-9]+)[ ]*," +
                                                       "[ ]*([A-z\\.#0-9]+)[ ]*," +
                                                       "[ ]*([A-z\\.#0-9]+)[ ]*" +
                                                       "\\)");

  public static Icon parse(String text, ColorService colorService, ImageLocator imageLocator, SplitsContext context) {
    if (Strings.isNullOrEmpty(text)) {
      return null;
    }

    EmptyIcon empty = parseEmpty(text);
    if (empty != null) {
      return empty;
    }

    ArrowIcon arrow = parseArrow(text, colorService, context);
    if (arrow != null) {
      return arrow;
    }

    CircledArrowIcon circledArrow = parseCircledArrow(text, colorService, context);
    if (circledArrow != null) {
      return circledArrow;
    }

    RectIcon rect = parseRect(text, colorService, context);
    if (rect != null) {
      return rect;
    }

    RoundedRectIcon roundedRect = parseRoundedRect(text, colorService, context);
    if (roundedRect != null) {
      return roundedRect;
    }

    OvalIcon oval = parseOval(text, colorService, context);
    if (oval != null) {
      return oval;
    }

    return imageLocator.get(text);
  }

  private static ArrowIcon parseArrow(String text, ColorService colorService, SplitsContext context) {
    Matcher arrowMatcher = ARROW_FORMAT.matcher(text.trim());
    if (arrowMatcher.matches()) {
      String orientation = arrowMatcher.group(1);
      int iconWidth = Integer.parseInt(arrowMatcher.group(2));
      int iconHeight = Integer.parseInt(arrowMatcher.group(3));
      int arrowWidth = Integer.parseInt(arrowMatcher.group(4));
      int arrowHeight = Integer.parseInt(arrowMatcher.group(5));

      final ArrowIcon icon = new ArrowIcon(iconWidth, iconHeight,
                                           arrowWidth, arrowHeight,
                                           ArrowIcon.Orientation.get(orientation));

      String colorValue = arrowMatcher.group(6);
      if (Colors.isHexaString(colorValue)) {
        Color color = Colors.toColor(colorValue);
        icon.setColor(color);
      }
      else {
        ColorUpdater updater = new ColorUpdater(colorValue) {
          public void updateColor(Color color) {
            icon.setColor(color);
          }
        };
        updater.install(colorService);
        context.addDisposable(updater);
      }

      return icon;
    }
    return null;
  }

  private static EmptyIcon parseEmpty(String text) {
    Matcher emptyMatcher = EMPTY_FORMAT.matcher(text.trim());
    if (emptyMatcher.matches()) {
      int iconWidth = Integer.parseInt(emptyMatcher.group(1));
      int iconHeight = Integer.parseInt(emptyMatcher.group(2));

      return new EmptyIcon(iconWidth, iconHeight);
    }
    return null;
  }

  private static CircledArrowIcon parseCircledArrow(String text, ColorService colorService, SplitsContext context) {
    Matcher arrowMatcher = CIRCLED_ARROW_FORMAT.matcher(text.trim());
    if (arrowMatcher.matches()) {
      final CircledArrowIcon icon = new CircledArrowIcon();

      String colorValue = arrowMatcher.group(1);
      if (Colors.isHexaString(colorValue)) {
        Color color = Colors.toColor(colorValue);
        icon.setColor(color);
      }
      else {
        ColorUpdater updater = new ColorUpdater(colorValue) {
          public void updateColor(Color color) {
            icon.setColor(color);
          }
        };
        updater.install(colorService);
        context.addDisposable(updater);
      }

      return icon;
    }
    return null;
  }

  private static RectIcon parseRect(String text, ColorService colorService, SplitsContext context) {
    Matcher matcher = RECT_FORMAT.matcher(text.trim());
    if (!matcher.matches()) {
      return null;
    }

    int iconWidth = Integer.parseInt(matcher.group(1));
    int iconHeight = Integer.parseInt(matcher.group(2));

    final RectIcon icon = new RectIcon(iconWidth, iconHeight);

    String background = matcher.group(3);
    if (Colors.isHexaString(background)) {
      Color color = Colors.toColor(background);
      icon.setBackgroundColor(color);
    }
    else {
      ColorUpdater updater = new ColorUpdater(background) {
        public void updateColor(Color color) {
          icon.setBackgroundColor(color);
        }
      };
      updater.install(colorService);
      context.addDisposable(updater);
    }

    String border = matcher.group(4);
    if (Colors.isHexaString(border)) {
      Color color = Colors.toColor(border);
      icon.setBorderColor(color);
    }
    else {
      ColorUpdater updater = new ColorUpdater(border) {
        public void updateColor(Color color) {
          icon.setBorderColor(color);
        }
      };
      updater.install(colorService);
      context.addDisposable(updater);
    }

    return icon;
  }

  private static RoundedRectIcon parseRoundedRect(String text, ColorService colorService, SplitsContext context) {
    Matcher matcher = ROUNDED_RECT_FORMAT.matcher(text.trim());
    if (!matcher.matches()) {
      return null;
    }

    int iconWidth = Integer.parseInt(matcher.group(1));
    int iconHeight = Integer.parseInt(matcher.group(2));
    int arcX = Integer.parseInt(matcher.group(3));
    int arcY = Integer.parseInt(matcher.group(4));

    final RoundedRectIcon icon = new RoundedRectIcon(iconWidth, iconHeight, arcX, arcY);

    String background = matcher.group(5);
    if (Colors.isHexaString(background)) {
      Color color = Colors.toColor(background);
      icon.setBackgroundColor(color);
    }
    else {
      ColorUpdater updater = new ColorUpdater(background) {
        public void updateColor(Color color) {
          icon.setBackgroundColor(color);
        }
      };
      updater.install(colorService);
      context.addDisposable(updater);
    }

    String border = matcher.group(6);
    if (Colors.isHexaString(border)) {
      Color color = Colors.toColor(border);
      icon.setBorderColor(color);
    }
    else {
      ColorUpdater updater = new ColorUpdater(border) {
        public void updateColor(Color color) {
          icon.setBorderColor(color);
        }
      };
      updater.install(colorService);
      context.addDisposable(updater);
    }

    return icon;
  }

  private static OvalIcon parseOval(String text, ColorService colorService, SplitsContext context) {
    Matcher matcher = OVAL_FORMAT.matcher(text.trim());
    if (matcher.matches()) {
      int iconWidth = Integer.parseInt(matcher.group(1));
      int iconHeight = Integer.parseInt(matcher.group(2));

      final OvalIcon icon = new OvalIcon(iconWidth, iconHeight);

      String background = matcher.group(3);
      if (Colors.isHexaString(background)) {
        Color color = Colors.toColor(background);
        icon.setBackgroundColor(color);
      }
      else {
        ColorUpdater updater = new ColorUpdater(background) {
          public void updateColor(Color color) {
            icon.setBackgroundColor(color);
          }
        };
        updater.install(colorService);
        context.addDisposable(updater);
      }

      String border = matcher.group(4);
      if (Colors.isHexaString(border)) {
        Color color = Colors.toColor(border);
        icon.setBorderColor(color);
      }
      else {
        ColorUpdater updater = new ColorUpdater(border) {
          public void updateColor(Color color) {
            icon.setBorderColor(color);
          }
        };
        updater.install(colorService);
        context.addDisposable(updater);
      }

      return icon;
    }
    return null;
  }
}
