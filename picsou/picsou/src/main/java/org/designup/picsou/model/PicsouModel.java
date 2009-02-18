package org.designup.picsou.model;

import org.designup.picsou.server.serialization.SerializationManager;
import org.globsframework.metamodel.GlobModel;
import org.globsframework.metamodel.utils.GlobModelBuilder;

public class PicsouModel {
  private static GlobModel model =
    GlobModelBuilder.init(Account.TYPE,
                          Bank.TYPE,
                          BankEntity.TYPE,
                          Category.TYPE,
                          Month.TYPE,
                          CurrentMonth.TYPE,
                          Transaction.TYPE,
                          TransactionType.TYPE,
                          PreTransactionTypeMatcher.TYPE,
                          TransactionTypeMatcher.TYPE,
                          TransactionImport.TYPE,
                          UserPreferences.TYPE,
                          Series.TYPE,
                          SeriesToCategory.TYPE,
                          SeriesBudget.TYPE,
                          BudgetArea.TYPE,
                          ProfileType.TYPE,
                          VersionInformation.TYPE,
                          AccountPositionThreshold.TYPE)
      .get();

  static {
    SerializationManager.init(model);
  }

  public static GlobModel get() {
    return model;
  }
}
