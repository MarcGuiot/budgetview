package com.budgetview.server.cloud.model;

import org.globsframework.metamodel.GlobModel;
import org.globsframework.metamodel.utils.GlobModelBuilder;

public class CloudModel {
  public static final int MAJOR_VERSION = 1;
  public static final int MINOR_VERSION = 0;

  private static GlobModel MODEL =
  GlobModelBuilder.init(CloudAccount.TYPE,
                        AccountPosition.TYPE,
                        CloudMonth.TYPE,
                        CloudBudgetArea.TYPE,
                        CloudBudgetAreaValues.TYPE,
                        CloudSeries.TYPE,
                        CloudSeriesValues.TYPE,
                        CloudTransaction.TYPE,
                        CloudVersion.TYPE)
      .get();

  public static GlobModel get() {
    return MODEL;
  }
}
