package org.designup.picsou.gui.utils.dev;

import org.designup.picsou.gui.model.PeriodSeriesStat;
import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.model.*;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobPrinter;

import javax.swing.*;
import java.awt.event.ActionEvent;

import static org.globsframework.model.utils.GlobMatchers.fieldEquals;

public class DumpRepositoryAction extends AbstractAction {

  private GlobRepository repository;

  public DumpRepositoryAction(GlobRepository repository) {
    super("[Dump repository]");
    this.repository = repository;
  }

  public void actionPerformed(ActionEvent actionEvent) {
    doPrint(Series.TYPE, PeriodSeriesStat.TYPE);
  }

  public void doPrint(GlobType... types) {
    GlobPrinter.init(repository)
      .showOnly(types)
//      .setTextFilters("Voyage", "142")
//      .showFields(Account.TYPE, Account.ID, Account.NAME, Account.NUMBER, Account.ACCOUNT_TYPE, Account.LAST_IMPORT_POSITION, Account.POSITION_DATE)
//      .showFields(PeriodSeriesStat.TYPE, PeriodSeriesStat.TARGET, PeriodSeriesStat.TARGET_TYPE, PeriodSeriesStat.BUDGET_AREA, PeriodSeriesStat.AMOUNT, PeriodSeriesStat.PLANNED_AMOUNT, PeriodSeriesStat.ACTIVE, PeriodSeriesStat.VISIBLE)
//      .showFields(SeriesBudget.TYPE, SeriesBudget.SERIES, SeriesBudget.MONTH, SeriesBudget.ACTIVE, SeriesBudget.ACTUAL_AMOUNT, SeriesBudget.PLANNED_AMOUNT)
//      .showFields(Series.TYPE, Series.ID, Series.NAME, Series.BUDGET_AREA, Series.ACTIVE, Series.TARGET_ACCOUNT, Series.FROM_ACCOUNT, Series.TO_ACCOUNT, Series.MIRROR_SERIES)
//      .showFields(ProjectTransfer.TYPE, ProjectTransfer.PROJECT_ITEM, ProjectTransfer.FROM_ACCOUNT, ProjectTransfer.TO_ACCOUNT)
//      .showFields(ProjectItem.TYPE, ProjectTransfer.PROJECT_ITEM, ProjectTransfer.FROM_ACCOUNT, ProjectTransfer.TO_ACCOUNT)
//      .showFields(Transaction.TYPE, Transaction.ID, Transaction.ACCOUNT, Transaction.PLANNED, Transaction.LABEL, Transaction.AMOUNT, Transaction.SERIES)
      .showFields(SeriesStat.TYPE, SeriesStat.TARGET, SeriesStat.TARGET_TYPE, SeriesStat.MONTH, SeriesStat.ACTIVE, SeriesStat.PLANNED_AMOUNT)
      .showFields(PeriodSeriesStat.TYPE, PeriodSeriesStat.TARGET, PeriodSeriesStat.TARGET_TYPE, PeriodSeriesStat.ACTIVE, PeriodSeriesStat.PLANNED_AMOUNT, PeriodSeriesStat.PREVIOUS_SUMMARY_AMOUNT, PeriodSeriesStat.NEW_SUMMARY_AMOUNT)
      .run();

    GlobPrinter.print(repository.getAll(SeriesStat.TYPE, fieldEquals(SeriesStat.TARGET, 112)));
  }
}
