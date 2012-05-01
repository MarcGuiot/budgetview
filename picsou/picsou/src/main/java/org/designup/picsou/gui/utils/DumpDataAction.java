package org.designup.picsou.gui.utils;

import org.designup.picsou.model.Series;
import org.designup.picsou.model.SeriesBudget;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Glob;
import org.globsframework.model.format.GlobPrinter;
import static org.globsframework.model.utils.GlobMatchers.*;

import org.globsframework.utils.TablePrinter;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.FileWriter;
import java.io.File;
import java.io.IOException;

public class DumpDataAction extends AbstractAction {
  private final GlobRepository repository;

  public DumpDataAction(GlobRepository repository) {
    super("[Dump data]");
    this.repository = repository;
  }

  public void actionPerformed(ActionEvent e) {

//    printEnvelopeSeriesBudgetForMonth(200905);

    try {
      File file = File.createTempFile("dump", ".txt");
      GlobPrinter.init(repository).run(new FileWriter(file));
      System.out.println("Dump in : " + file.getAbsolutePath());
    }
    catch (IOException e1) {
    }
  }

  private void printEnvelopeSeriesBudgetForMonth(final int monthId) {

    final Glob uncategorized = repository.get(Series.UNCATEGORIZED_SERIES);
    GlobPrinter.print(new GlobList(uncategorized));

    final GlobList budgets =
      repository.getAll(SeriesBudget.TYPE,
                        and(fieldEquals(SeriesBudget.SERIES, Series.UNCATEGORIZED_SERIES_ID)));

    TablePrinter table = new TablePrinter();
    for (Glob budget : budgets) {
      Glob series = repository.findLinkTarget(budget, SeriesBudget.SERIES);
      table.addRow(
        series.get(Series.BUDGET_AREA),
        series.get(Series.ID),
        budget.get(SeriesBudget.MONTH),
        series.get(Series.NAME),
        budget.get(SeriesBudget.PLANNED_AMOUNT, 0.),
        budget.get(SeriesBudget.ACTIVE)
      );
    }
    
    System.out.println("SeriesBudget");
    table.print();

    System.out.println("Total " + budgets.getSum(SeriesBudget.PLANNED_AMOUNT));
  }
}
