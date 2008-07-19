package org.globsframework.gui.splits.utils;

import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.color.Colors;
import org.globsframework.gui.splits.color.ColorUpdater;
import org.globsframework.gui.splits.exceptions.SplitsException;
import org.globsframework.gui.splits.components.MutableMatteBorder;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BorderUtils {
  private static Pattern EMPTY_FORMAT = Pattern.compile("empty\\(" +
                                                        "[ ]*([0-9]+)[ ]*," +
                                                        "[ ]*([0-9]+)[ ]*," +
                                                        "[ ]*([0-9]+)[ ]*," +
                                                        "[ ]*([0-9]+)[ ]*" +
                                                        "\\)");
  private static Pattern MATTE_FORMAT = Pattern.compile("matte\\(" +
                                                        "[ ]*([0-9]+)[ ]*," +
                                                        "[ ]*([0-9]+)[ ]*," +
                                                        "[ ]*([0-9]+)[ ]*," +
                                                        "[ ]*([0-9]+)[ ]*," +
                                                        "[ ]*([A-z\\.#0-9]+)[ ]*" +
                                                        "\\)");

  public static Border parse(String desc, ColorService colorService) {

    if (desc.equalsIgnoreCase("none")) {
      return null;
    }
    if (desc.equalsIgnoreCase("empty")) {
      return BorderFactory.createEmptyBorder();
    }
    if (desc.equalsIgnoreCase("etched")) {
      return BorderFactory.createEtchedBorder();
    }
    if (desc.equalsIgnoreCase("bevel(lowered)")) {
      return BorderFactory.createBevelBorder(BevelBorder.LOWERED);
    }
    if (desc.equalsIgnoreCase("bevel(raised)")) {
      return BorderFactory.createBevelBorder(BevelBorder.RAISED);
    }
    Matcher emptyMatcher = EMPTY_FORMAT.matcher(desc.trim());
    if (emptyMatcher.matches()) {
      return BorderFactory.createEmptyBorder(Integer.parseInt(emptyMatcher.group(1)),
                                             Integer.parseInt(emptyMatcher.group(2)),
                                             Integer.parseInt(emptyMatcher.group(3)),
                                             Integer.parseInt(emptyMatcher.group(4)));
    }

    Matcher matteMatcher = MATTE_FORMAT.matcher(desc.trim());
    if (matteMatcher.matches()) {
      String colorValue = matteMatcher.group(5);
      int top = Integer.parseInt(matteMatcher.group(1));
      int left = Integer.parseInt(matteMatcher.group(2));
      int bottom = Integer.parseInt(matteMatcher.group(3));
      int right = Integer.parseInt(matteMatcher.group(4));
      if (colorValue.startsWith(Colors.HEXA_PREFIX)) {
        Color color = Colors.toColor(colorValue.substring(1));
        return BorderFactory.createMatteBorder(top, left, bottom, right, color);
      }
      else {
        Color initialColor = colorService.get(colorValue);
        final MutableMatteBorder border = new MutableMatteBorder(top, left, bottom, right, initialColor);
        colorService.install(colorValue, new ColorUpdater() {
          public void updateColor(Color color) {
            border.setColor(color);
          }
        });
        return border;
      }
    }

    throw new SplitsException("Unknown border type '" + desc + "'");
  }
}
