package org.designup.picsou.gui.utils.dev;

import org.designup.picsou.gui.model.PeriodSeriesStat;
import org.designup.picsou.model.*;
import org.designup.picsou.triggers.SeriesBudgetTrigger;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobPrinter;
import org.globsframework.model.utils.GlobMatchers;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class DumpRepositoryAction extends AbstractAction {

  private GlobRepository repository;

  public DumpRepositoryAction(GlobRepository repository) {
    super("[Dump repository]");
    this.repository = repository;
  }

  public void actionPerformed(ActionEvent actionEvent) {
    doPrint(Account.TYPE, Series.TYPE, PeriodSeriesStat.TYPE);
  }

  public void doPrint(GlobType... types) {
    GlobPrinter.init(repository)
      .showOnly(types)
      .showFields(Account.TYPE, Account.ID, Account.NAME, Account.NUMBER, Account.ACCOUNT_TYPE, Account.LAST_IMPORT_POSITION, Account.POSITION_DATE)
      .showFields(PeriodSeriesStat.TYPE, PeriodSeriesStat.TARGET, PeriodSeriesStat.TARGET_TYPE, PeriodSeriesStat.BUDGET_AREA, PeriodSeriesStat.AMOUNT, PeriodSeriesStat.PLANNED_AMOUNT, PeriodSeriesStat.ACTIVE, PeriodSeriesStat.VISIBLE)
      .showFields(Series.TYPE, Series.ID, Series.NAME, Series.BUDGET_AREA, Series.ACTIVE, Series.TARGET_ACCOUNT, Series.FROM_ACCOUNT, Series.FROM_ACCOUNT)
      .run();
  }
}
