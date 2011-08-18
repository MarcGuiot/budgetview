package org.globsframework.gui.splits.utils;

import org.globsframework.gui.splits.ImageLocator;
import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.color.ColorUpdater;
import org.globsframework.gui.splits.color.Colors;
import org.globsframework.gui.splits.components.ArrowIcon;
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

  public static Icon parse(String text, ColorService colorService, ImageLocator imageLocator, SplitsContext context) {
    if (Strings.isNullOrEmpty(text)) {
      return null;
    }

    ArrowIcon arrow = parseArrow(text, colorService, context);
    if (arrow != null) {
      return arrow;
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

}
