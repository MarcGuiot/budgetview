package org.designup.picsou.gui.categorization.utils;

import org.globsframework.model.FieldValues;
import org.globsframework.model.FieldValue;
import org.designup.picsou.model.BudgetArea;

public interface SeriesCreationHandler {
  void createSeries(BudgetArea budgetArea, FieldValue... forcedValues);
}
