package com.budgetview.gui.categorization.utils;

import com.budgetview.model.BudgetArea;
import org.globsframework.model.FieldValue;

public interface SeriesCreationHandler {
  void createSeries(BudgetArea budgetArea, FieldValue... forcedValues);
}
