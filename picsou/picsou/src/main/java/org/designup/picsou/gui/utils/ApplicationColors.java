package org.designup.picsou.gui.utils;

import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.color.Colors;
import org.globsframework.gui.splits.painters.GradientPainter;
import org.globsframework.metamodel.annotations.NoObfuscation;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public enum ApplicationColors {
  @NoObfuscation
  SELECTION_BG_BORDER,

  PERIOD_BALANCE_ZERO,
  PERIOD_BALANCE_PLUS_4,
  PERIOD_BALANCE_PLUS_3,
  PERIOD_BALANCE_PLUS_2,
  PERIOD_BALANCE_PLUS_1,
  PERIOD_BALANCE_PLUS_05,
  PERIOD_BALANCE_MINUS_4,
  PERIOD_BALANCE_MINUS_3,
  PERIOD_BALANCE_MINUS_2,
  PERIOD_BALANCE_MINUS_1,
  PERIOD_BALANCE_MINUS_05,

  BUTTON_NO_FOCUS_COLOR_BORDER,
  BUTTON_NO_FOCUS_COLOR_SHADOW,
  BUTTON_FOCUS_COLOR_BORDER,
  BUTTON_FOCUS_COLOR_SHADOW,
  BUTTON_TOP_COLOR_GRADIENT,
  BUTTON_BOTTOM_COLOR_GRADIENT;

  private boolean canBeNull;
  private String toString;

  public static final String[] COLOR_FILES = {
    "/colors/color.properties",
    "/colors/color_blue.properties",
    "/colors/color_black.properties",
    "/colors/color_turquoise.properties",
    "/colors/color_pink.properties",
    "/colors/color_green.properties"
  };

  ApplicationColors() {
    this(false);
    toString = name().toLowerCase().replaceAll("_", ".");
  }

  ApplicationColors(boolean canBeNull) {
    this.canBeNull = canBeNull;
    toString = name().toLowerCase().replaceAll("_", ".");
  }

  public String toString() {
    return toString;
  }

  public static ColorService registerColorService(Directory directory) throws IOException {
    ColorService colorService = createColorService();
    directory.add(ColorService.class, colorService);
    UIManager.put("ColorService", directory.get(ColorService.class));
    return colorService;
  }

  public static ColorService createColorService() {
    ColorService colorService = new ColorService(ApplicationColors.class, COLOR_FILES);
    check(colorService);
    return colorService;
  }

  private static void check(ColorService service) {
    List<String> names = new ArrayList<String>();
    for (ApplicationColors item : values()) {
      if (!item.canBeNull && (service.get(item) == null)) {
        names.add(item.toString());
      }
    }
    if (!names.isEmpty()) {
      System.out.println("ApplicationColors.check: Missing colors " + names.toString());
      for (String name : names) {
        service.set(name, Color.RED);
      }
    }
  }

  public static void installSelectionColors(final JTable table, Directory directory) {
    final ColorService colorService = directory.get(ColorService.class);
    colorService.addListener(new ColorChangeListener() {
      public void colorsChanged(ColorLocator colorLocator) {
        setSelectionColors(table, colorLocator);
      }
    });
  }

  public static void setSelectionColors(final JTable table, Directory directory) {
    setSelectionColors(table, directory.get(ColorService.class));
  }

  public static void setSelectionColors(JTable table, ColorLocator colors) {
    table.setSelectionBackground(colors.get("transactionTable.selected.bg"));
    table.setSelectionForeground(colors.get("transactionTable.text.selected"));
  }

  public static void installLinkColor(final JEditorPane editor, final String cssClass, final String colorKey, Directory directory) {
    directory.get(ColorService.class).addListener(new ColorChangeListener() {
      public void colorsChanged(ColorLocator colorLocator) {

        Color color = colorLocator.get(colorKey);

        // Warning: the editorKit is a singleton
        HTMLEditorKit kit = (HTMLEditorKit) editor.getEditorKit();
        StyleSheet css = kit.getStyleSheet();
        css.addRule("a." + cssClass + " { color: #" + Colors.toString(color) + "; }");
      }
    });
  }
}