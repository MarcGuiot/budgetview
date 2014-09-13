package org.designup.picsou.gui.utils.dev;

import org.designup.picsou.gui.model.PeriodBudgetAreaStat;
import org.designup.picsou.gui.model.PeriodSeriesStat;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Series;
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
    doPrint(repository);
  }

  public static void doPrint(GlobRepository repository) {

    BudgetArea area = BudgetArea.INCOME;

    System.out.println("\n\n******************************************************************************************");
    GlobPrinter.print(repository.getAll(Account.TYPE, Account.userCreatedAccounts()));
    GlobPrinter.print(repository.getAll(Series.TYPE, fieldEquals(Series.BUDGET_AREA, area.getId())));
    GlobPrinter.print(repository.getAll(PeriodSeriesStat.TYPE, fieldEquals(PeriodSeriesStat.BUDGET_AREA, area.getId())));
    GlobPrinter.print(repository.getAll(PeriodBudgetAreaStat.TYPE, fieldEquals(PeriodBudgetAreaStat.BUDGET_AREA, area.getId())));
    System.out.println("******************************************************************************************\n\n");
  }
}
