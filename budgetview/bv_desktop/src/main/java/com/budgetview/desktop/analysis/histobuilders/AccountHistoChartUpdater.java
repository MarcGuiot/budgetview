package com.budgetview.desktop.analysis.histobuilders;

import com.budgetview.model.Month;
import com.budgetview.model.Transaction;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public abstract class AccountHistoChartUpdater extends HistoChartUpdater {
  public AccountHistoChartUpdater(GlobRepository repository, Directory directory) {
    super(repository, directory, Month.TYPE, Month.ID, Transaction.TYPE);
  }
}
