package com.budgetview.desktop.description;

import com.budgetview.shared.model.BudgetArea;
import com.budgetview.shared.model.DefaultSeries;
import com.budgetview.utils.Lang;

public class Labels {
  public static String get(BudgetArea budgetArea) {
    return Lang.get("budgetArea." + budgetArea.getName());
  }

  public static String getDescription(BudgetArea budgetArea) {
    return Lang.get("budgetArea.description." + budgetArea.getName());
  }

  public static String getHtmlDescription(BudgetArea budgetArea) {
    return "<html>" + Lang.get("budgetArea.description." + budgetArea.getName()) + "</html>";
  }

  public static String get(DefaultSeries defaultSeries) {
    if (defaultSeries == null) {
      return null;
    }
    return Lang.get(getKey(defaultSeries));
  }

  public static String getKey(DefaultSeries defaultSeries) {
    return "defaultSeries." + defaultSeries.getName();
  }
}
