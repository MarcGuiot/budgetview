package com.budgetview.desktop.importer.utils;

import com.budgetview.desktop.accounts.utils.MonthDay;
import com.budgetview.model.*;
import com.budgetview.shared.model.BudgetArea;
import com.budgetview.triggers.AutomaticSeriesBudgetTrigger;
import com.budgetview.triggers.SeriesBudgetTrigger;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.repository.LocalGlobRepository;
import org.globsframework.model.repository.LocalGlobRepositoryBuilder;

public class Importer {
  public static LocalGlobRepository loadLocalRepository(GlobRepository repository) {
      GlobType[] globTypes = {Bank.TYPE, BankEntity.TYPE, MonthDay.TYPE,
        Account.TYPE, AccountUpdateMode.TYPE, BudgetArea.TYPE,
        Transaction.TYPE, Month.TYPE, UserPreferences.TYPE, CurrentMonth.TYPE, RealAccount.TYPE,
        Series.TYPE, SubSeries.TYPE, ImportedSeries.TYPE, TransactionImport.TYPE, CsvMapping.TYPE,
        User.TYPE, CloudDesktopUser.TYPE, CloudProviderConnection.TYPE};

    LocalGlobRepository localRepository = LocalGlobRepositoryBuilder.init(repository)
        .copy(globTypes).get();
    localRepository.addTrigger(new AutomaticSeriesBudgetTrigger());
    localRepository.addTrigger(new SeriesBudgetTrigger(repository));
    return localRepository;
  }
}
