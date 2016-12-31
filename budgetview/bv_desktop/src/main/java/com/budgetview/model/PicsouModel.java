package com.budgetview.model;

import com.budgetview.io.importer.csv.CsvType;
import com.budgetview.session.serialization.SerializationManager;
import com.budgetview.shared.model.AccountType;
import com.budgetview.shared.model.BudgetArea;
import com.budgetview.shared.model.Provider;
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
                          AddOns.TYPE,
                          Series.TYPE,
                          SubSeries.TYPE,
                          SeriesToCategory.TYPE,
                          SeriesBudget.TYPE,
                          BudgetArea.TYPE,
                          Provider.TYPE,
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
                          Picture.TYPE,
                          ProjectItemAmount.TYPE,
                          LayoutConfig.TYPE,
                          SeriesGroup.TYPE,
                          AnalysisViewType.TYPE,
                          StandardMessage.TYPE,
                          User.TYPE,
                          AddOns.TYPE,
                          ProjectAccountGraph.TYPE,
                          CloudDesktopUser.TYPE,
                          CloudProviderConnection.TYPE
    )
      .get();

  static {
    SerializationManager.init(MODEL);
  }

  public static GlobModel get() {
    return MODEL;
  }
}
