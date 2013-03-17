package com.budgetview.shared.model;

import org.globsframework.metamodel.GlobModel;
import org.globsframework.metamodel.utils.GlobModelBuilder;

public class MobileModel {
  public static final int MAJOR_VERSION = 1;
  public static final int MINOR_VERSION = 0;

  private static GlobModel MODEL =
  GlobModelBuilder.init(AccountEntity.TYPE,
                        AccountPosition.TYPE,
                        MonthEntity.TYPE,
                        BudgetAreaEntity.TYPE,
                        BudgetAreaValues.TYPE,
                        SeriesEntity.TYPE,
                        SeriesValues.TYPE,
                        TransactionValues.TYPE,
                        BudgetViewVersion.TYPE)
      .get();

  public static GlobModel get() {
    return MODEL;
  }
}
