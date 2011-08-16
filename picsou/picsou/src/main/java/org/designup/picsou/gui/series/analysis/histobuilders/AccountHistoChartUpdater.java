package org.designup.picsou.gui.series.analysis.histobuilders;

import org.designup.picsou.gui.model.BudgetStat;
import org.designup.picsou.gui.model.SavingsBudgetStat;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Transaction;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public abstract class AccountHistoChartUpdater extends HistoChartUpdater {
  public AccountHistoChartUpdater(HistoChartBuilder histoChartBuilder, GlobRepository repository, Directory directory) {
    super(repository, directory, Month.TYPE, Month.ID, BudgetStat.TYPE, SavingsBudgetStat.TYPE, Transaction.TYPE);
  }
}
