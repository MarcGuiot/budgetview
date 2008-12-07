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

public enum PicsouColors {
  @NoObfuscation
  BUTTON_BG_TOP,
  BUTTON_BG_BOTTOM,
  BUTTON_INNER_BORDER_TOP,
  BUTTON_INNER_BORDER_BOTTOM,
  BUTTON_OUTER_BORDER_TOP,
  BUTTON_OUTER_BORDER_BOTTOM,
  BUTTON_PRESSED_BG_TOP,
  BUTTON_PRESSED_BG_BOTTOM,
  BUTTON_PRESSED_INNER_BORDER_TOP,
  BUTTON_PRESSED_INNER_BORDER_BOTTOM,
  BUTTON_PRESSED_OUTER_BORDER_TOP,
  BUTTON_PRESSED_OUTER_BORDER_BOTTOM,
  SELECTION_BG_BORDER,

  TIMEVIEW_MONTH_TOP,
  TIMEVIEW_MONTH_BOTTOM,
  TIMEVIEW_CURRENT_MONTH_TOP,
  TIMEVIEW_CURRENT_MONTH_BOTTOM,
  TIMEVIEW_YEAR_SEPARATOR,
  TIMEVIEW_TEXT_FUTURE,
  TIMEVIEW_TEXT_CURRENT,
  TIMEVIEW_TEXT_YEAR,
  TIMEVIEW_TEXT_PAST,
  TIMEVIEW_TEXT_SHADOW,
  TIMEVIEW_SELECTED_MONTH_TOP,
  TIMEVIEW_SELECTED_MONTH_BOTTOM,

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

  TRANSACTION_TABLE_HEADER_BORDER,
  TRANSACTION_TABLE_HEADER_LIGHT,
  TRANSACTION_TABLE_HEADER_MEDIUM,
  TRANSACTION_TABLE_HEADER_DARK,
  TRANSACTION_TABLE_HEADER_TITLE,
  TRANSACTION_TABLE_HEADER_FILTERED_BORDER,
  TRANSACTION_TABLE_HEADER_FILTERED_LIGHT,
  TRANSACTION_TABLE_HEADER_FILTERED_MEDIUM,
  TRANSACTION_TABLE_HEADER_FILTERED_DARK,
  TRANSACTION_TABLE_HEADER_FILTERED_TITLE,
  TRANSACTION_ERROR_TEXT,
  TRANSACTION_TEXT_PLANNED,
  TRANSACTION_TEXT_LINK,
  TRANSACTION_TEXT_ERROR,
  TRANSACTION_TEXT,
  TRANSACTION_SELECTED_TEXT,
  TRANSACTION_SELECTED_BG,
  TRANSACTION_EVEN_ROWS_BG,
  TRANSACTION_ODD_ROWS_BG,
  TRANSACTION_SEARCH_FIELD,
  TRANSACTION_SPLIT_SOURCE_BG,
  TRANSACTION_SPLIT_BG,
  ROLLOVER_CATEGORY_LABEL,
  CATEGORY_LABEL,
  CATEGORY_TITLE,
  CATEGORY_TITLE_GRAYED,
  CATEGORY_TITLE_ERROR,
  CATEGORIES_SELECTED_FG,
  CATEGORIES_SELECTED_BORDER,
  CATEGORIES_SELECTED_BG_TOP,
  CATEGORIES_SELECTED_BG_BOTTOM,
  CATEGORIES_BG,
  BALANCE_POSITIVE,
  BALANCE_NEGATIVE,
  CHART_LABEL,
  CHART_INCOME_BAR_TOP,
  CHART_INCOME_BAR_BOTTOM,
  CHART_EXPENSES_BAR_TOP,
  CHART_EXPENSES_BAR_BOTTOM,
  CHART_BG_TOP,
  CHART_BG_BOTTOM,
  CHART_MARKER_OUTLINE,
  CHART_MARKER,
  BUTTON_NO_FOCUS_COLOR_BORDER,
  BUTTON_NO_FOCUS_COLOR_SHADOW,
  BUTTON_FOCUS_COLOR_BORDER,
  BUTTON_FOCUS_COLOR_SHADOW,
  BUTTON_TOP_COLOR_GRADIENT,
  BUTTON_BOTTOM_COLOR_GRADIENT,
  DIALOG_BG_TOP,
  DIALOG_BG_BOTTOM,
  DIALOG_BORDER;

  private boolean canBeNull;
  private String toString;

  PicsouColors() {
    this(false);
    toString = name().toLowerCase().replaceAll("_", ".");
  }

  PicsouColors(boolean canBeNull) {
    this.canBeNull = canBeNull;
    toString = name().toLowerCase().replaceAll("_", ".");
  }

  public String toString() {
    return toString;
  }

  public static ColorService registerColorService(Directory directory) throws IOException {
    ColorService colorService = createColorService();
    directory.add(ColorService.class, colorService);
    return colorService;
  }

  public static ColorService createColorService() {
    ColorService colorService = new ColorService(PicsouColors.class,
                                                 "/colors/color.properties",
                                                 "/colors/seb.properties",
                                                 "/colors/regis.properties",
                                                 "/colors/darkgreycolors.properties",
                                                 "/colors/blackcolors.properties",
                                                 "/colors/color_purple.properties",
                                                 "/colors/color_green.properties",
                                                 "/colors/color_print.properties",
                                                 "/colors/grey_yellow1.properties");
    check(colorService);
    return colorService;
  }

  private static void check(ColorService service) {
    List<String> names = new ArrayList<String>();
    for (PicsouColors item : values()) {
      if (!item.canBeNull && (service.get(item) == null)) {
        names.add(item.toString());
      }
    }
    if (!names.isEmpty()) {
      System.out.println("PicsouColors.check: Missing colors " + names.toString());
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
    table.setSelectionBackground(colors.get("transaction.selected.bg"));
    table.setSelectionForeground(colors.get("transaction.selected.text"));
  }

  public static void installLinkColor(final JEditorPane editor, final String cssClass, final String colorKey, Directory directory) {
    directory.get(ColorService.class).addListener(new ColorChangeListener() {
      public void colorsChanged(ColorLocator colorLocator) {

        Color color = colorLocator.get(colorKey);

        HTMLEditorKit kit = (HTMLEditorKit)editor.getEditorKit();
        StyleSheet css = kit.getStyleSheet();
        css.addRule("a." + cssClass + " { color: #" + Colors.toString(color) + "; }");
      }
    });
  }

  public static GradientPainter createTableSelectionBackgroundPainter(ColorService colorService) {
    return new GradientPainter(CATEGORIES_SELECTED_BG_TOP,
                               CATEGORIES_SELECTED_BG_BOTTOM,
                               CATEGORIES_SELECTED_BORDER,
                                           colorService);
  }
}