package com.budgetview.shared.model;

import org.globsframework.metamodel.GlobModel;
import org.globsframework.metamodel.utils.GlobModelBuilder;

public class MobileModel {
  public static final int MAJOR_VERSION = 1;
  public static final int MINOR_VERSION = 0;
  public static final String MAJOR_VERSION_NAME = "MAJOR_VERSION";
  public static final String MINOR_VERSION_NAME = "MINOR_VERSION";
  public static final String CRYPTED_INFO = "INFO";

  private static GlobModel MODEL =
  GlobModelBuilder.init(AccountEntity.TYPE,
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
