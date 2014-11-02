package org.designup.picsou.gui.analysis.histobuilders;

import org.designup.picsou.model.Month;
import org.designup.picsou.model.Transaction;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public abstract class AccountHistoChartUpdater extends HistoChartUpdater {
  public AccountHistoChartUpdater(GlobRepository repository, Directory directory) {
    super(repository, directory, Month.TYPE, Month.ID, Transaction.TYPE);
  }
}
