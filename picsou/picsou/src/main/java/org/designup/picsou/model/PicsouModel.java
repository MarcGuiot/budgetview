package org.designup.picsou.model;

import org.designup.picsou.importer.csv.CsvType;
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
                          DeferredCardPeriod.TYPE,
                          Notes.TYPE,
                          SignpostStatus.TYPE,
                          SeriesOrder.TYPE,
                          Project.TYPE,
                          ProjectItem.TYPE,
                          ProjectTransfer.TYPE,
                          RealAccount.TYPE,
                          NumericDateType.TYPE,
                          TextDateType.TYPE,
                          CsvMapping.TYPE,
                          DayOfMonth.TYPE,
                          CsvType.CSV_TYPE,
                          AccountPositionMode.TYPE,
                          AccountPositionError.TYPE,
                          Synchro.TYPE,
                          Picture.TYPE
                          )
      .get();

  static {
    SerializationManager.init(MODEL);
  }

  public static GlobModel get() {
    return MODEL;
  }
}
