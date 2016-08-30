package com.budgetview.desktop.categorization.utils;

import com.budgetview.shared.model.BudgetArea;
import org.globsframework.model.FieldValue;

public interface SeriesCreationHandler {
  void createSeries(BudgetArea budgetArea, FieldValue... forcedValues);
}
