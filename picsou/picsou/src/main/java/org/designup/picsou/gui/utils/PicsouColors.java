package org.designup.picsou.gui.utils;

import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.utils.directory.Directory;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public enum PicsouColors {
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
  PERIOD_BG_TOP,
  PERIOD_BG_BOTTOM,
  PERIOD_GRID,
  PERIOD_YEAR_SEPARATOR,
  PERIOD_TEXT,
  PERIOD_TEXT_SHADOW,
  PERIOD_SELECTION_BG_TOP,
  PERIOD_SELECTION_BG_BOTTOM,
  TRANSACTION_TABLE_HEADER_BORDER,
  TRANSACTION_TABLE_HEADER_LIGHT,
  TRANSACTION_TABLE_HEADER_MEDIUM,
  TRANSACTION_TABLE_HEADER_DARK,
  TRANSACTION_TABLE_HEADER_TITLE,
  TRANSACTION_ERROR_TEXT,
  TRANSACTION_TEXT_PLANNED,
  TRANSACTION_TEXT,
  TRANSACTION_SELECTED_TEXT,
  TRANSACTION_SELECTED_BG,
  TRANSACTION_EVEN_ROWS_BG,
  TRANSACTION_ODD_ROWS_BG,
  ROLLOVER_CATEGORY_LABEL,
  CATEGORY_LABEL,
  INCOME_TEXT,
  EXPENSE_TEXT,
  CATEGORY_TITLE,
  CATEGORY_TITLE_GRAYED,
  CATEGORY_TITLE_ERROR,
  CATEGORIES_SELECTED_FG,
  CATEGORIES_SELECTED_BORDER,
  CATEGORIES_SELECTED_BG_TOP,
  CATEGORIES_SELECTED_BG_BOTTOM,
  CATEGORIES_BG,
  INCOME_BAR_LIGHT,
  INCOME_BAR_DARK,
  EXPENSE_BAR_LIGHT,
  EXPENSE_BAR_DARK,
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
  INTRAMONTH_CURRENT_LINE,
  INTRAMONTH_PREVIOUS_LINE,
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
                                                 "/color.properties",
                                                 "/seb.properties",
                                                 "/regis.properties",
                                                 "/darkgreycolors.properties",
                                                 "/blackcolors.properties",
                                                 "/purplecolors.properties",
                                                 "/grey_yellow1.properties");
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
}