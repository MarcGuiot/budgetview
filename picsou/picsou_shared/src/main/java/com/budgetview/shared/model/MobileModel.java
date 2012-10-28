package com.budgetview.shared.model;

import org.globsframework.metamodel.GlobModel;
import org.globsframework.metamodel.utils.GlobModelBuilder;

public class MobileModel {
  private static GlobModel MODEL =
  GlobModelBuilder.init(AccountEntity.TYPE,
                        MonthEntity.TYPE,
                        BudgetAreaEntity.TYPE,
                        BudgetAreaValues.TYPE,
                        SeriesEntity.TYPE,
                        SeriesValues.TYPE,
                        TransactionValues.TYPE)
      .get();

  public static GlobModel get() {
    return MODEL;
  }
}
