package org.designup.picsou.gui.utils.datacheck;

import org.designup.picsou.gui.time.TimeService;
import org.designup.picsou.gui.components.dialogs.MessageAndDetailsDialog;
import org.designup.picsou.gui.components.dialogs.MessageDialog;
import org.designup.picsou.model.*;
import org.designup.picsou.triggers.MonthsToSeriesBudgetTrigger;
import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.annotations.Required;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobFunctor;
import org.globsframework.model.utils.GlobFunctors;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Log;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.ItemNotFound;

import javax.swing.*;
import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class DataCheckingService {
  private GlobRepository repository;
  private Directory directory;

  public DataCheckingService(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.directory = directory;
  }

  public boolean check(Throwable ex) {
    DataCheckReport report = new DataCheckReport();
    try {
      doCheck(report);
      return report.hasError();
    }
    finally {
      if (report.hasError()) {
        Toolkit.getDefaultToolkit().beep();
        String message = report.toString();
        String message1 = message + "\n\n";
        if (ex != null) {
          StringWriter writer = new StringWriter();
          writer.append(message1);
          ex.printStackTrace(new PrintWriter(writer));
          message1 = writer.toString();
        }
        MessageAndDetailsDialog dialog =
          new MessageAndDetailsDialog("data.checker.error.title", "data.checker.error.message",
                                      message1 + "\n\n\n", null, directory);
        dialog.show();
        Log.write(report.toString());
      }
      else {
        if (ex != null) {
          String message = "";
          StringWriter writer = new StringWriter();
          writer.append(message);
          ex.printStackTrace(new PrintWriter(writer));
          message = writer.toString();
          MessageAndDetailsDialog dialog =
            new MessageAndDetailsDialog("data.checker.ok.title", "data.checker.ok.message",
                                        message + "\n\n\n", null, directory);
          dialog.show();
        }
        else {
          MessageDialog.show("data.checker.ok.title", directory.get(JFrame.class), directory, "data.checker.ok.message"
          );
        }
      }
    }
  }

  public boolean doCheck(DataCheckReport report) {

    ExtractMonthFromTransaction extractMonthFromTransaction = new ExtractMonthFromTransaction();
    repository.safeApply(Transaction.TYPE, GlobMatchers.ALL, extractMonthFromTransaction);

    GlobList months = repository.getAll(Month.TYPE).sort(Month.ID);
    if (months.size() == 0) {
      report.addError("No month\n");
      return true;
    }

    // on recupere les premier et dernier mois par rapport aux transactions.
    int firstMonth = months.getFirst().get(Month.ID);
    int lastMonth = months.getLast().get(Month.ID);
    if (firstMonth > extractMonthFromTransaction.getFirstMonthForTransaction()) {
      report.append("Mising first month ").append(extractMonthFromTransaction.getFirstMonthForTransaction());
      firstMonth = extractMonthFromTransaction.getFirstMonthForTransaction();
    }
    if (lastMonth < extractMonthFromTransaction.getLastMonthForTransaction()) {
      report.append("Mising last month ").append(extractMonthFromTransaction.getLastMonthForTransaction());
      lastMonth = extractMonthFromTransaction.getLastMonthForTransaction();
    }

    java.util.List<Integer> monthToCreate = new ArrayList<Integer>();

    // Pour s'assurer que le mois courant est bien dans la liste des mois.
    int now = TimeService.getCurrentMonth();
    if (firstMonth > now) {
      firstMonth = now;
    }
    if (now > lastMonth) {
      lastMonth = now;
    }

    // on parcourt les mois pour s'assurer de leur continuité.
    boolean nowFound = false;
    int currentMonth = firstMonth;
    Iterator<Glob> it = months.iterator();
    Glob actual = null;
    while (currentMonth <= lastMonth) {
      if (actual == null && it.hasNext()) {
        actual = it.next();
        if (now == actual.get(Month.ID)) {
          nowFound = true;
        }
      }
      if (actual == null || actual.get(Month.ID) != currentMonth) {
        report.append("Missing month ").append(currentMonth).append("\n");
        monthToCreate.add(currentMonth);
      }
      else {
        actual = null;
      }
      currentMonth = Month.next(currentMonth);
    }

    if (!nowFound) {
      report.append("Missing current month ").append(now).append("\n");
    }
    for (Integer monthId : monthToCreate) {
      repository.create(Key.create(Month.TYPE, monthId));
    }

    // pour chaque series
    GlobList allSeries = repository.getAll(Series.TYPE);

    for (Glob series : allSeries) {
      try {

        if (series.get(Series.MIRROR_SERIES) != null) {
          if (series.get(Series.FROM_ACCOUNT) == null || series.get(Series.TO_ACCOUNT) == null) {
            report.append("Savings series with both imported account has null in it's account : ")
              .append(series.get(Series.NAME))
              .append("\n");
            repository.delete(series.getKey());
            try {
              repository.delete(Key.create(Series.TYPE, series.get(Series.MIRROR_SERIES)));
            }
            catch (ItemNotFound found) {
              report.append("Missing miroir series (can not delete)\n");
            }
            continue;
          }
        }

        checkNotNullable(series, report);

        Integer firstMonthForSeries = series.get(Series.FIRST_MONTH);
        if (firstMonthForSeries == null || firstMonthForSeries < firstMonth) {
          firstMonthForSeries = firstMonth;
        }
        Integer lastMonthForSeries = series.get(Series.LAST_MONTH);
        if (lastMonthForSeries == null || lastMonthForSeries > lastMonth) {
          if (firstMonthForSeries <= lastMonth) {
            lastMonthForSeries = lastMonth;
          }
        }

        // On remet à UNCATEGORIZED les transactions avec SERIES à null
        repository.startChangeSet();
        try {
          repository.safeApply(Transaction.TYPE,
                               GlobMatchers.isNull(Transaction.SERIES),
                               GlobFunctors.update(Transaction.SERIES, Series.UNCATEGORIZED_SERIES_ID));
        }
        finally {
          repository.completeChangeSetWithoutTriggers();
        }

        // On verifie que les dates de debut/fin de series sont bien dans les bornes des transactions
        // associé a la serie
        ExtractMonthFromTransaction monthFromTransaction = new ExtractMonthFromTransaction();
        repository.safeApply(Transaction.TYPE,
                             GlobMatchers.fieldEquals(Transaction.SERIES, series.get(Series.ID)),
                             monthFromTransaction);
        if (monthFromTransaction.getFirstMonthForTransaction() < firstMonthForSeries) {
          firstMonthForSeries = monthFromTransaction.getFirstMonthForTransaction();
          repository.update(series.getKey(), Series.FIRST_MONTH, firstMonthForSeries);
          report.append("Bad begin of series, updated to ").append(firstMonthForSeries);
        }
        if (monthFromTransaction.getLastMonthForTransaction() > lastMonthForSeries) {
          lastMonthForSeries = monthFromTransaction.getLastMonthForTransaction();
          repository.update(series.getKey(), Series.LAST_MONTH, lastMonthForSeries);
          report.append("Bad end of series, updated to ").append(lastMonthForSeries);
        }

        if (firstMonthForSeries > lastMonth) {
          continue;
        }

        checkSeriesBudget(report, series, firstMonthForSeries, lastMonthForSeries);
        GlobList transactions =
          repository.findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES, series.get(Series.ID))
            .getGlobs();

        for (Glob transaction : transactions) {
          checkTransactionBetweenSeriesDate(firstMonthForSeries, lastMonthForSeries, transaction, report);
          checkNotNullable(transaction, report);
          checkSavingsTransaction(transaction, report);
        }
      }
      catch (Throwable found) {
        // TODO: cette exception fait echouer deux tests
        StringBuilder builder = new StringBuilder();
        found.printStackTrace();
        builder.append("For series ")
          .append(series.get(Series.ID))
          .append(series.get(Series.NAME))
          .append("\n")
          ;
        StringWriter writer = new StringWriter();
        found.printStackTrace(new PrintWriter(writer));
        builder.append(writer.toString())
          .append("\n");
        System.out.println(builder.toString());
      }
    }

    checkSplittedTransactions(report);

    checkAllSeriesBudgetAreAssociated(report);

    TransactionToSeriesChecker toSeriesChecker = new TransactionToSeriesChecker(report);
    repository.safeApply(Transaction.TYPE, GlobMatchers.ALL, toSeriesChecker);
    toSeriesChecker.deletePlanned(repository);

    return report.hasError();
  }

  private void checkSeriesBudget(DataCheckReport buf, Glob series, Integer firstMonthForSeries, Integer lastMonthForSeries) {
    int currentMonth;

    currentMonth = firstMonthForSeries;

    java.util.List<Integer> budgetToCreate = new ArrayList<Integer>();

    Set<Integer> budgets =
      repository.getAll(SeriesBudget.TYPE, GlobMatchers.fieldEquals(SeriesBudget.SERIES, series.get(Series.ID)))
        .getValueSet(SeriesBudget.ID);
    ReadOnlyGlobRepository.MultiFieldIndexed index =
      repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, series.get(Series.ID));
    while (currentMonth <= lastMonthForSeries) {
      Glob budget = index.findByIndex(SeriesBudget.MONTH, currentMonth).getGlobs().getFirst();
      if (budget == null) {
        budgetToCreate.add(currentMonth);
        buf.append("Adding SeriesBudget for series : ").append(series.get(Series.NAME))
          .append(" at :").append(currentMonth).append(("\n"));
      }
      else {
        budgets.remove(budget.get(SeriesBudget.ID));
      }
      currentMonth = Month.next(currentMonth);
    }

    for (Integer budgetId : budgets) {
      Key seriesBudgetKey = Key.create(SeriesBudget.TYPE, budgetId);
      Glob seriesBudget = repository.get(seriesBudgetKey);
      buf.append("Deleting SeriesBudget for series : ").append(series.get(Series.NAME))
        .append(" at :").append(seriesBudget.get(SeriesBudget.MONTH)).append(("\n"));
      repository.delete(seriesBudgetKey);
    }
    for (Integer month : budgetToCreate) {
      MonthsToSeriesBudgetTrigger.addMonthForSeries(repository, month, series);
    }
  }

  private void checkAllSeriesBudgetAreAssociated(DataCheckReport buffer) {
    Set<Integer> seriesId = new HashSet<Integer>();
    GlobList seriesBudgets = repository.getAll(SeriesBudget.TYPE);
    for (Glob budget : seriesBudgets) {
      Glob series = repository.findLinkTarget(budget, SeriesBudget.SERIES);
      if (series == null) {
        if (seriesId.add(budget.get(SeriesBudget.SERIES))) {
          buffer.append("SeriesBudget : Missing series ")
            .append(budget.get(SeriesBudget.SERIES))
            .append("\n");
        }
        repository.delete(budget.getKey());
      }
    }
  }

  private void checkSplittedTransactions(DataCheckReport buffer) {
    GlobList transactions = repository.getAll(Transaction.TYPE,
                                              GlobMatchers.not(GlobMatchers.isNull(Transaction.SPLIT_SOURCE)));
    for (Glob transaction : transactions) {
      if (repository.findLinkTarget(transaction, Transaction.SPLIT_SOURCE) == null) {
        buffer.append("Error : split source was deleted for ")
          .append(transaction.get(Transaction.LABEL)).append(" : ")
          .append(Month.toString(transaction.get(Transaction.MONTH), transaction.get(Transaction.DAY)))
          .append(("\n"));
      }
    }
  }

  private void checkTransactionBetweenSeriesDate(Integer firstMonthForSeries, Integer lastMonthForSeries, Glob transaction, DataCheckReport buffer) {
    Integer month = transaction.get(Transaction.MONTH);
    if (month < firstMonthForSeries || month > lastMonthForSeries) {
      buffer.append("Transaction is not in Series dates ")
        .append(Month.toString(transaction.get(Transaction.BANK_MONTH),
                               transaction.get(Transaction.BANK_DAY)))
        .append(" ").append(transaction.get(Transaction.LABEL));
    }
  }

  private void checkSavingsTransaction(Glob transaction, DataCheckReport buffer) {
    Glob target = repository.findLinkTarget(transaction, Transaction.ACCOUNT);
    if (Account.isSavings(target)) {
      Glob savingsSeries = repository.findLinkTarget(transaction, Transaction.SERIES);
      if (transaction.get(Transaction.AMOUNT) >= 0) {
        Integer toAccount = savingsSeries.get(Series.TO_ACCOUNT);
        if (toAccount != null && !transaction.get(Transaction.ACCOUNT).equals(toAccount)) {
          buffer.append("savings transaction badly categorized ")
            .append(transaction.get(Transaction.LABEL))
            .append(" at : ")
            .append(Month.toString(transaction.get(Transaction.BANK_MONTH), transaction.get(Transaction.DAY)))
            .append(". Uncategorized : you must recategorize it");
          try {
            repository.startChangeSet();
            repository.update(transaction.getKey(), Transaction.SERIES, Series.UNCATEGORIZED_SERIES_ID);
            repository.update(transaction.getKey(), Transaction.SUB_SERIES, null);
          }
          finally {
            repository.completeChangeSet();
          }
        }
      }
      else {
        Integer fromAccount = savingsSeries.get(Series.FROM_ACCOUNT);
        if (fromAccount != null && !transaction.get(Transaction.ACCOUNT).equals(fromAccount)) {
          buffer.append("savings transaction badly categorized ").append(transaction.get(Transaction.LABEL))
            .append(" at : ")
            .append(Month.toString(transaction.get(Transaction.BANK_MONTH), transaction.get(Transaction.DAY)))
            .append(". Uncategorized : you must recategorize it");
          try {
            repository.startChangeSet();
            repository.update(transaction.getKey(), Transaction.SERIES, null);
            repository.update(transaction.getKey(), Transaction.SUB_SERIES, null);
          }
          finally {
            repository.completeChangeSet();
          }
        }
      }
    }
  }

  private void checkNotNullable(Glob glob, DataCheckReport buffer) {
    Field[] fields = glob.getType().getFields();
    for (Field field : fields) {
      if (field.hasAnnotation(Required.class)) {
        if (glob.getValue(field) == null) {
          buffer.append(field).append(" should not be null\n");
        }
      }
    }
  }

  private static class TransactionToSeriesChecker implements GlobFunctor {
    private final DataCheckReport buffer;
    private GlobList transactionsToDelete = new GlobList();

    public TransactionToSeriesChecker(DataCheckReport buffer) {
      this.buffer = buffer;
    }

    public void run(Glob glob, GlobRepository repository) throws Exception {
      Glob target = repository.findLinkTarget(glob, Transaction.SERIES);
      if (target == null) {
        if (glob.isTrue(Transaction.PLANNED)) {
          buffer.append("no series for planned transaction ");
          transactionsToDelete.add(glob);
        }
        else {
          buffer.append("no series for operations : ");
          repository.update(glob.getKey(), FieldValue.value(Transaction.SERIES, null),
                            FieldValue.value(Transaction.SUB_SERIES, null));
        }
        buffer.append(glob.get(Transaction.LABEL))
          .append(" bank date : ")
          .append(Month.toString(glob.get(Transaction.BANK_MONTH), glob.get(Transaction.BANK_DAY)))
          .append(" user date : ")
          .append(Month.toString(glob.get(Transaction.MONTH), glob.get(Transaction.DAY)));
      }
    }

    public void deletePlanned(GlobRepository repository) {
      repository.delete(transactionsToDelete);
    }
  }

  private static class ExtractMonthFromTransaction implements GlobFunctor {
    int firstMonthForTransaction = Integer.MAX_VALUE;
    int lastMonthForTransaction = Integer.MIN_VALUE;

    public void run(Glob glob, GlobRepository repository) throws Exception {
      addMonth(glob.get(Transaction.MONTH));
      addMonth(glob.get(Transaction.BANK_MONTH));
    }

    void addMonth(Integer month) {
      if (month > lastMonthForTransaction) {
        lastMonthForTransaction = month;
      }
      if (month < firstMonthForTransaction) {
        firstMonthForTransaction = month;
      }
    }

    public int getFirstMonthForTransaction() {
      return firstMonthForTransaction;
    }

    public int getLastMonthForTransaction() {
      return lastMonthForTransaction;
    }
  }
}
