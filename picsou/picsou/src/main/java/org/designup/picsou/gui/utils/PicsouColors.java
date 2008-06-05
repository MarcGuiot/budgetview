package org.designup.picsou.gui.utils;

import org.crossbowlabs.globs.utils.directory.Directory;
import org.crossbowlabs.splits.color.ColorService;

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
  FRAME_BG_TOP,
  FRAME_BG_BOTTOM,
  SELECTION_BG_BORDER,
  PERIOD_BG_TOP,
  PERIOD_BG_BOTTOM,
  PERIOD_GRID,
  PERIOD_TEXT,
  PERIOD_SELECTION_BG_TOP,
  PERIOD_SELECTION_BG_BOTTOM,
  TRANSACTION_TABLE_HEADER_BORDER,
  TRANSACTION_TABLE_HEADER_LIGHT,
  TRANSACTION_TABLE_HEADER_MEDIUM,
  TRANSACTION_TABLE_HEADER_DARK,
  TRANSACTION_TABLE_HEADER_TITLE,
  TRANSACTION_TEXT,
  TRANSACTION_SELECTED_TEXT,
  TRANSACTION_SELECTED_BG,
  TRANSACTION_SELECTED_ERROR_BG,
  TRANSACTION_EVEN_ROWS_BG,
  TRANSACTION_ODD_ROWS_BG,
  TRANSACTION_EVEN_ERROR_BG,
  TRANSACTION_ODD_ERROR_BG,
  TRANSACTIONS_GRID,
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
  CATEGORY_TABLE_HEADER_BORDER,
  CATEGORY_TABLE_HEADER_LIGHT,
  CATEGORY_TABLE_HEADER_MEDIUM,
  CATEGORY_TABLE_HEADER_DARK,
  CATEGORY_TABLE_HEADER_TITLE,
  INCOME_BAR_LIGHT,
  INCOME_BAR_DARK,
  EXPENSE_BAR_LIGHT,
  EXPENSE_BAR_DARK,
  BALANCE_POSITIVE,
  BALANCE_NEGATIVE,
  CHART_INCOME_LINE,
  CHART_INCOME_SHAPE,
  CHART_INCOME_AVERAGE_LINE,
  CHART_EXPENSES_LINE,
  CHART_EXPENSES_SHAPE,
  CHART_EXPENSES_AVERAGE_LINE,
  CHART_BG_TOP,
  CHART_BG_BOTTOM,
  CHART_MARKER_OUTLINE,
  CHART_MARKER,
  INFOLABEL_FG,
  SCORECARD_TEXT,
  SCORECARD_INCOME,
  SCORECARD_EXPENSES,
  INTRAMONTH_CURRENT_LINE,
  INTRAMONTH_PREVIOUS_LINE,
  ACCOUNT_NAME,
  BUTTON_NO_FOCUS_COLOR_BORDER,
  BUTTON_NO_FOCUS_COLOR_SHADOW,
  BUTTON_FOCUS_COLOR_BORDER,
  BUTTON_FOCUS_COLOR_SHADOW,
  BUTTON_TOP_COLOR_GRADIENT,
  BUTTON_BOTTOM_COLOR_GRADIENT,
  DIALOG_BG_TOP,
  DIALOG_BG_BOTTOM,
  DIALOG_BORDER,
  WAVE_PANEL_TOP,
  WAVE_PANEL_BOTTOM,
  WAVE_PANEL_WAVE;

  private boolean canBeNull;

  PicsouColors() {
    this(false);
  }

  PicsouColors(boolean canBeNull) {
    this.canBeNull = canBeNull;
  }

  public String toString() {
    return name().toLowerCase().replaceAll("_", ".");
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