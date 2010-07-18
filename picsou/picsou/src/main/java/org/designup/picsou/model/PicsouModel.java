package org.designup.picsou.model;

import org.designup.picsou.server.serialization.SerializationManager;
import org.globsframework.metamodel.GlobModel;
import org.globsframework.metamodel.utils.GlobModelBuilder;

public class PicsouModel {
  private static GlobModel MODEL =
    GlobModelBuilder.init(Account.TYPE,
                          AccountType.TYPE,
                          AccountCardType.TYPE,
                          AccountUpdateMode.TYPE,
                          Bank.TYPE,
                          BankFormat.TYPE,
                          BankEntity.TYPE,
                          Category.TYPE,
                          Month.TYPE,
                          CurrentMonth.TYPE,
                          Transaction.TYPE,
                          TransactionType.TYPE,
                          PreTransactionTypeMatcher.TYPE,
                          TransactionImport.TYPE,
                          UserPreferences.TYPE,
                          Series.TYPE,
                          SubSeries.TYPE,
                          SeriesToCategory.TYPE,
                          SeriesBudget.TYPE,
                          BudgetArea.TYPE,
                          ProfileType.TYPE,
                          AppVersionInformation.TYPE,
                          UserVersionInformation.TYPE,
                          AccountPositionThreshold.TYPE,
                          DeferredCardDate.TYPE,
                          Notes.TYPE,
                          SignpostStatus.TYPE)
      .get();

  static {
    SerializationManager.init(MODEL);
  }

  public static GlobModel get() {
    return MODEL;
  }
}
