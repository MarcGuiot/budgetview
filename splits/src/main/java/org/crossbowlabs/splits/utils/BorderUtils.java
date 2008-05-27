package org.crossbowlabs.splits.utils;

import org.crossbowlabs.splits.color.ColorService;
import org.crossbowlabs.splits.color.Colors;
import org.crossbowlabs.splits.exceptions.SplitsException;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BorderUtils {
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
    Matcher shortMatcher = MATTE_FORMAT.matcher(desc.trim());
    if (shortMatcher.matches()) {
      String colorValue = shortMatcher.group(5);
      Color color = colorValue.startsWith(Colors.HEXA_PREFIX) ?
                    Colors.toColor(colorValue.substring(1)) :
                    colorService.get(colorValue);
      return BorderFactory.createMatteBorder(Integer.parseInt(shortMatcher.group(1)),
                                             Integer.parseInt(shortMatcher.group(2)),
                                             Integer.parseInt(shortMatcher.group(3)),
                                             Integer.parseInt(shortMatcher.group(4)),
                                             color);
    }

    throw new SplitsException("Unknown border type '" + desc + "'");
  }
}
