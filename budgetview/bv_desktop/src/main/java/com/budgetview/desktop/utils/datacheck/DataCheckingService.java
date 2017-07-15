package com.budgetview.desktop.utils.datacheck;

import com.budgetview.desktop.components.dialogs.MessageAndDetailsDialog;
import com.budgetview.desktop.components.dialogs.MessageDialog;
import com.budgetview.desktop.components.dialogs.MessageType;
import com.budgetview.desktop.utils.datacheck.check.*;
import com.budgetview.desktop.utils.datacheck.utils.TransactionMonthRange;
import com.budgetview.model.Series;
import com.budgetview.model.Transaction;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobFunctor;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Log;
import org.globsframework.utils.collections.Range;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.globsframework.model.utils.GlobMatchers.fieldEquals;

public class DataCheckingService {
  private GlobRepository repository;
  private Directory directory;

  public DataCheckingService(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.directory = directory;
  }

  public boolean check(Throwable ex) {
    StringWriter writer = new StringWriter();
    DataCheckReport report = new DataCheckReport(writer);
    try {
      doCheck(report);
      return report.hasErrors();
    }
    finally {
      if (report.hasErrors()) {
        Toolkit.getDefaultToolkit().beep();
        writer.append("\n\n");
        if (ex != null) {
          ex.printStackTrace(new PrintWriter(writer));
        }
        MessageAndDetailsDialog dialog =
          new MessageAndDetailsDialog("data.checker.error.title", "data.checker.error.message",
                                      writer.toString() + "\n\n\n", null, directory);
        dialog.show();
        Log.write(writer.toString());
      }
      else {
        if (ex != null) {
          writer.append("\n\n");
          ex.printStackTrace(new PrintWriter(writer));
          MessageAndDetailsDialog dialog =
            new MessageAndDetailsDialog("data.checker.ok.title", "data.checker.ok.message",
                                        writer.toString() + "\n\n\n", null, directory);
          dialog.show();
        }
        else {
          MessageDialog.show("data.checker.ok.title", MessageType.SUCCESS, directory.get(JFrame.class), directory, "data.checker.ok.message");
        }
      }
    }
  }

  public boolean doCheck(DataCheckReport report) {

    // ---- Months ----

    Range<Integer> months = MonthCheck.allMonthsPresent(repository, report);

    // ---- Transaction ----

    TransactionCheck.allTransactionAreLinkedToSeries(repository, report);

    // ---- Series ----

    SeriesCheck.allSeriesMirrorsAreProperlySet(repository, report);

    for (Glob series : repository.getAll(Series.TYPE)) {

      report.setCurrentSeries(series);
      try {
        GlobCheck.requiredFieldsAreSet(series, report);

        Integer firstMonthForSeries = series.get(Series.FIRST_MONTH);
        if (firstMonthForSeries == null || firstMonthForSeries < months.getMin()) {
          firstMonthForSeries = months.getMin();
        }
        Integer lastMonthForSeries = series.get(Series.LAST_MONTH);
        if (lastMonthForSeries == null || lastMonthForSeries > months.getMax()) {
          if (firstMonthForSeries <= months.getMax()) {
            lastMonthForSeries = months.getMax();
          }
        }

        // On verifie que les dates de debut/fin de series sont bien dans les bornes des transactions
        // associé a la serie
        GlobMatcher matcher = fieldEquals(Transaction.SERIES, series.get(Series.ID));
        TransactionMonthRange range = TransactionMonthRange.get(repository, matcher);
        if (range.first() < firstMonthForSeries) {
          firstMonthForSeries = range.first();
          repository.update(series.getKey(), Series.FIRST_MONTH, firstMonthForSeries);
          report.addFix("Bad begin of series, updated to " + firstMonthForSeries);
        }
        if (range.last() > lastMonthForSeries) {
          lastMonthForSeries = range.last();
          repository.update(series.getKey(), Series.LAST_MONTH, lastMonthForSeries);
          report.addFix("Bad end of series, updated to " + lastMonthForSeries);
        }

        if (firstMonthForSeries > months.getMax()) {
          continue;
        }

        SeriesCheck.seriesBudgetArePresent(series, firstMonthForSeries, lastMonthForSeries, repository, report);

        for (Glob transaction : Transaction.getAllForSeries(series.get(Series.ID), repository)) {
          TransactionCheck.transactionIsBetweenSeriesDates(transaction, firstMonthForSeries, lastMonthForSeries, report);
          TransactionCheck.savingsTransactionLinkedToProperAccount(transaction, repository, report);
        }
      }
      catch (Throwable ex) {
        report.addError(ex);
      }
    }
    report.clearCurrentSeries();

    TransactionCheck.allSplitTransactionsHaveASource(repository, report);
    SeriesCheck.allSeriesBudgetAreProperlyAssociated(repository, report);

    TransactionToSeriesChecker toSeriesChecker = new TransactionToSeriesChecker(report);
    repository.safeApply(Transaction.TYPE, GlobMatchers.ALL, toSeriesChecker);
    toSeriesChecker.deletePlanned(repository);
    AccountCheck.accountTotalAlignedWithTransactions(repository, report);

    return report.hasErrors();
  }

  private static class TransactionToSeriesChecker implements GlobFunctor {
    private final DataCheckReport report;
    private GlobList transactionsToDelete = new GlobList();

    public TransactionToSeriesChecker(DataCheckReport report) {
      this.report = report;
    }

    public void run(Glob transaction, GlobRepository repository) throws Exception {
      Glob target = repository.findLinkTarget(transaction, Transaction.SERIES);
      if (target == null) {
        if (transaction.isTrue(Transaction.PLANNED)) {
          report.addFix("No series for planned transaction", Transaction.toString(transaction));
          transactionsToDelete.add(transaction);
        }
        else {
          report.addFix("No series for real transaction", Transaction.toString(transaction));
          Transaction.uncategorize(transaction, repository);
        }
      }
    }

    public void deletePlanned(GlobRepository repository) {
      repository.delete(transactionsToDelete);
    }
  }

}
