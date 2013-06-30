package org.globsframework.gui.splits.utils;

import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.color.ColorUpdater;
import org.globsframework.gui.splits.color.Colors;
import org.globsframework.gui.splits.components.MutableLineBorder;
import org.globsframework.gui.splits.components.MutableMatteBorder;
import org.globsframework.gui.splits.exceptions.SplitsException;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BorderParser {
  private static Pattern EMPTY_SHORT_FORMAT = Pattern.compile("empty\\(" +
                                                              "[ ]*([0-9]+)[ ]*" +
                                                              "\\)");
  private static Pattern EMPTY_LONG_FORMAT = Pattern.compile("empty\\(" +
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

  private static Pattern LINE_FORMAT = Pattern.compile("line\\(" +
                                                       "[ ]*([A-z\\.#0-9]+)[ ]*" +
                                                       "\\)");

  public static Border parse(String desc, ColorService colorService, SplitsContext context) {

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

    Matcher shortEmptyMatcher = EMPTY_SHORT_FORMAT.matcher(desc.trim());
    if (shortEmptyMatcher.matches()) {
      int margin = Integer.parseInt(shortEmptyMatcher.group(1));
      return BorderFactory.createEmptyBorder(margin, margin, margin, margin);
    }

    Matcher emptyMatcher = EMPTY_LONG_FORMAT.matcher(desc.trim());
    if (emptyMatcher.matches()) {
      return BorderFactory.createEmptyBorder(Integer.parseInt(emptyMatcher.group(1)),
                                             Integer.parseInt(emptyMatcher.group(2)),
                                             Integer.parseInt(emptyMatcher.group(3)),
                                             Integer.parseInt(emptyMatcher.group(4)));
    }

    Matcher matteMatcher = MATTE_FORMAT.matcher(desc.trim());
    if (matteMatcher.matches()) {
      int top = Integer.parseInt(matteMatcher.group(1));
      int left = Integer.parseInt(matteMatcher.group(2));
      int bottom = Integer.parseInt(matteMatcher.group(3));
      int right = Integer.parseInt(matteMatcher.group(4));
      String colorValue = matteMatcher.group(5);
      if (Colors.isHexaString(colorValue)) {
        Color color = Colors.toColor(colorValue);
        return BorderFactory.createMatteBorder(top, left, bottom, right, color);
      }
      else {
        Color initialColor = colorService.get(colorValue);
        final MutableMatteBorder border = new MutableMatteBorder(top, left, bottom, right, initialColor);
        ColorUpdater updater = new ColorUpdater(colorValue) {
          public void updateColor(Color color) {
            border.setColor(color);
          }
        };
        updater.install(colorService);
        context.addDisposable(updater);
        return border;
      }
    }

    Matcher lineMatcher = LINE_FORMAT.matcher(desc.trim());
    if (lineMatcher.matches()) {
      String colorValue = lineMatcher.group(1);
      if (Colors.isHexaString(colorValue)) {
        return BorderFactory.createLineBorder(Colors.toColor(colorValue));
      }
      else {
        final MutableLineBorder border = new MutableLineBorder();
        ColorUpdater updater = new ColorUpdater(colorValue) {
          public void updateColor(Color color) {
            border.setColor(color);
          }
        };
        updater.install(colorService);
        context.addDisposable(updater);
        return border;
      }
    }

    throw new SplitsException("Unknown border type '" + desc + "' - possible examples:\n" +
                              "- empty(2,3,4,5)\n" +
                              "- etched\n" +
                              "- bevel(lowered)\n" +
                              "- bevel(raised)\n" +
                              "- matte(1,2,3,4,#00FF00\n" +
                              "- line(#00FF00)");
  }
}
