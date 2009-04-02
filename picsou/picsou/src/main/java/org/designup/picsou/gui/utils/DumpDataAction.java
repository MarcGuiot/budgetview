package org.designup.picsou.gui.utils;

import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.SeriesBudget;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobPrinter;
import static org.globsframework.model.utils.GlobMatchers.*;
import org.globsframework.model.utils.GlobMatchers;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class DumpDataAction extends AbstractAction {
  private final GlobRepository repository;

  public DumpDataAction(GlobRepository repository) {
    super("[Dump data]");
    this.repository = repository;
  }

  public void actionPerformed(ActionEvent e) {

//    printEnvelopeSeriesBudgetForMonth(200903);

    GlobPrinter.print(repository);
  }

  private void printEnvelopeSeriesBudgetForMonth(final int monthId) {
    final GlobList budgets =
      repository.getAll(SeriesBudget.TYPE,
                        linkedTo(SeriesBudget.SERIES,
                                 fieldEquals(Series.BUDGET_AREA, BudgetArea.ENVELOPES.getId())));
    budgets.filterSelf(GlobMatchers.fieldEquals(SeriesBudget.MONTH, monthId), repository);
    GlobPrinter.print(budgets);
  }
}
