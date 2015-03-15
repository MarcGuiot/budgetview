package org.designup.picsou.gui.utils.datacheck;

import org.designup.picsou.gui.components.dialogs.MessageAndDetailsDialog;
import org.designup.picsou.gui.components.dialogs.MessageDialog;
import org.designup.picsou.gui.components.dialogs.MessageType;
import org.designup.picsou.gui.time.TimeService;
import org.designup.picsou.model.*;
import org.designup.picsou.triggers.MonthsToSeriesBudgetTrigger;
import org.designup.picsou.utils.TransactionComparator;
import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.annotations.Required;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobFunctor;
import org.globsframework.model.utils.GlobFunctors;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Dates;
import org.globsframework.utils.Log;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.ItemNotFound;

import javax.swing.*;
import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

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
          MessageDialog.show("data.checker.ok.title", MessageType.SUCCESS, directory.get(JFrame.class), directory, "data.checker.ok.message"
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
            repository.delete(series);
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


    checkAccountTotal(report);
    checkTransactionSeries(repository, report);
    return report.hasError();
  }

  private void checkTransactionSeries(GlobRepository repository, DataCheckReport report) {
    Set<Integer> series = repository.getAll(Series.TYPE).getValueSet(Series.ID);
    GlobList all = repository.getAll(Transaction.TYPE);
    for (Glob transaction : all) {
      if (!series.contains(transaction.get(Transaction.SERIES))) {
        if (transaction.isTrue(Transaction.PLANNED)) {
          repository.delete(transaction);
        }
        else {
          Transaction.uncategorize(transaction, repository);
        }
        report.addError("Missing series for " + transaction.get(Transaction.LABEL));
      }
    }
  }

  private void checkAccountTotal(DataCheckReport report) {
    Glob mainSummaryAccount = repository.getAll(Account.TYPE, GlobMatchers.fieldEquals(Account.ID, Account.MAIN_SUMMARY_ACCOUNT_ID)).getFirst();

    Date mainPosDate = mainSummaryAccount.get(Account.POSITION_DATE);
    GlobList allMainAcounts = repository.getAll(Account.TYPE, GlobMatchers.fieldEquals(Account.ACCOUNT_TYPE, AccountType.MAIN.getId()));
    for (Glob mainAccount : allMainAcounts) {
      Date date = mainAccount.get(Account.POSITION_DATE);
      if (date != null && mainPosDate.before(date)) {
        report.addError("main summary date is " + Dates.toString(mainPosDate) +
                        " but is composed with date " + Dates.toString(date) + " for " +
                        mainAccount.get(Account.NAME));

      }
    }

    TransactionComparator comparator = TransactionComparator.ASCENDING_ACCOUNT;

    Glob[] transactions = repository.getSorted(Transaction.TYPE, comparator, GlobMatchers.ALL);
    Glob currentMonth = repository.get(CurrentMonth.KEY);
    Date lastTransactionDate = Month.toDate(currentMonth.get(CurrentMonth.LAST_TRANSACTION_MONTH),
                                            currentMonth.get(CurrentMonth.LAST_TRANSACTION_DAY));
    for (Glob transaction : transactions) {
      Integer accountId = transaction.get(Transaction.ACCOUNT);
      Date positionDate = Month.toDate(transaction.get(Transaction.POSITION_MONTH), transaction.get(Transaction.POSITION_DAY));
      if (transaction.isTrue(Transaction.PLANNED)) {
        if (positionDate.before(lastTransactionDate)) {
          report.addError("Planned before current date " + Dates.toString(positionDate) + " / " +
                          Dates.toString(lastTransactionDate));
        }
      }
      else {
        if (Account.SUMMARY_ACCOUNT_IDS.contains(transaction.get(Transaction.ACCOUNT))) {
          report.addError("Operation in summary account " + transaction.get(Transaction.ACCOUNT) + " for '" + transaction.get(Transaction.LABEL) + "'");
        }
        if (positionDate.after(lastTransactionDate)) {
          report.addError("Current position date before last operation " + Dates.toString(positionDate) + " / " +
                          Dates.toString(lastTransactionDate));
        }
        Date bankDate = Month.toDate(transaction.get(Transaction.POSITION_MONTH), transaction.get(Transaction.POSITION_DAY));
        if (bankDate.after(lastTransactionDate)) {
          report.addError("Current bank date before last operation " + Dates.toString(positionDate) + " / " +
                          Dates.toString(lastTransactionDate));
        }
        if (accountId == Account.MAIN_SUMMARY_ACCOUNT_ID) {
          report.addError("Main summary contains transaction");
        }
      }
    }
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

    repository.startChangeSet();
    try {
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
    finally {
      repository.completeChangeSet();
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
        repository.delete(budget);
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
        .append(Month.toString(transaction.get(Transaction.POSITION_MONTH),
                               transaction.get(Transaction.POSITION_DAY)))
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
            .append(Month.toString(transaction.get(Transaction.POSITION_MONTH), transaction.get(Transaction.POSITION_DAY)))
            .append(". Uncategorized : you must recategorize it");
          Transaction.uncategorize(transaction, repository);
        }
      }
      else {
        Integer fromAccount = savingsSeries.get(Series.FROM_ACCOUNT);
        if (fromAccount != null && !transaction.get(Transaction.ACCOUNT).equals(fromAccount)) {
          buffer.append("savings transaction badly categorized ").append(transaction.get(Transaction.LABEL))
            .append(" at : ")
            .append(Month.toString(transaction.get(Transaction.POSITION_MONTH), transaction.get(Transaction.POSITION_DAY)))
            .append(". Uncategorized : you must recategorize it");
          Transaction.uncategorize(transaction, repository);
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

    public void run(Glob transaction, GlobRepository repository) throws Exception {
      Glob target = repository.findLinkTarget(transaction, Transaction.SERIES);
      if (target == null) {
        if (transaction.isTrue(Transaction.PLANNED)) {
          buffer.append("no series for planned transaction ");
          transactionsToDelete.add(transaction);
        }
        else {
          buffer.append("no series for operations : ");
          Transaction.uncategorize(transaction, repository);
        }
        buffer.append(transaction.get(Transaction.LABEL))
          .append(" bank date : ")
          .append(Month.toString(transaction.get(Transaction.POSITION_MONTH), transaction.get(Transaction.POSITION_DAY)))
          .append(" user date : ")
          .append(Month.toString(transaction.get(Transaction.MONTH), transaction.get(Transaction.DAY)));
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
      if (month != null) {
        if (month > lastMonthForTransaction) {
          lastMonthForTransaction = month;
        }
        if (month < firstMonthForTransaction) {
          firstMonthForTransaction = month;
        }
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
