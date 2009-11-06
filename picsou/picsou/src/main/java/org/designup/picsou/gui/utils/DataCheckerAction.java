package org.designup.picsou.gui.utils;

import org.designup.picsou.gui.TimeService;
import org.designup.picsou.gui.components.dialogs.MessageDialog;
import org.designup.picsou.gui.components.dialogs.MessageAndDetailsDialog;
import org.designup.picsou.triggers.MonthsToSeriesBudgetTrigger;
import org.designup.picsou.model.*;
import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.annotations.Required;
import org.globsframework.model.utils.GlobFunctor;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.model.*;
import org.globsframework.utils.Log;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.ItemNotFound;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.List;

public class DataCheckerAction extends AbstractAction {
  private GlobRepository repository;
  private Directory directory;

  public DataCheckerAction(GlobRepository repository, Directory directory) {
    super("[Check data (see logs)]");
    this.repository = repository;
    this.directory = directory;
  }

  public void actionPerformed(ActionEvent e) {
    check();
  }

  public boolean check() {
    final StringBuilder builder = new StringBuilder();
    builder.append("Start checking\n");
    boolean hasError = false;
    try {
      hasError = doCheck(builder);
      return hasError;
    }
    finally {
      builder.append("End checking\n");
      if (hasError) {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        toolkit.beep();
      }
      MessageAndDetailsDialog dialog = new MessageAndDetailsDialog("data.checker.title", "data.checker.message", builder.toString(), null, directory);
      dialog.show();
      Log.write(builder.toString());
    }
  }

  public boolean doCheck(StringBuilder buf) {
    boolean hasError = false;

    ExtractMonthFromTransaction extractMonthFromTransaction = new ExtractMonthFromTransaction();
    repository.safeApply(Transaction.TYPE, GlobMatchers.ALL, extractMonthFromTransaction);

    GlobList months = repository.getAll(Month.TYPE).sort(Month.ID);
    if (months.size() == 0) {
      buf.append("No month\n");
      return true;
    }

    // on recupere les premier et dernier mois par rapport au transaction.
    int firstMonth = months.getFirst().get(Month.ID);
    int lastMonth = months.getLast().get(Month.ID);
    if (firstMonth > extractMonthFromTransaction.getFirstMonthForTransaction()) {
      buf.append("Mising first month ").append(extractMonthFromTransaction.getFirstMonthForTransaction());
      firstMonth = extractMonthFromTransaction.getFirstMonthForTransaction();
      hasError = true;
    }
    if (lastMonth < extractMonthFromTransaction.getLastMonthForTransaction()) {
      buf.append("Mising last month ").append(extractMonthFromTransaction.getLastMonthForTransaction());
      lastMonth = extractMonthFromTransaction.getLastMonthForTransaction();
      hasError = true;
    }

    List<Integer> monthToCreate = new ArrayList<Integer>();

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
        buf.append("Missing month ").append(currentMonth).append("\n");
        hasError = true;
        monthToCreate.add(currentMonth);
      }
      else {
        actual = null;
      }
      currentMonth = Month.next(currentMonth);
    }

    if (!nowFound) {
      buf.append("Missing current month ").append(now).append("\n");
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
            buf.append("Savings series with both imported account has null in it's account : ")
              .append(series.get(Series.NAME))
              .append("\n");
            repository.delete(series.getKey());
            try {
              repository.delete(Key.create(Series.TYPE, series.get(Series.MIRROR_SERIES)));
            }
            catch (ItemNotFound found) {
              buf.append("Missing miroir series (can not delete)\n");
            }
            continue;
          }
        }

        hasError |= checkNotNullable(series, buf);

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

        // On verifie que les dates de debut/fin de series sont bien dans les bornes des transactions
        // associé a la serie
        ExtractMonthFromTransaction monthFromTransaction = new ExtractMonthFromTransaction();
        repository.safeApply(Transaction.TYPE, GlobMatchers.fieldEquals(Transaction.SERIES, series.get(Series.ID)),
                             monthFromTransaction);

        if (monthFromTransaction.getFirstMonthForTransaction() < firstMonthForSeries) {
          firstMonthForSeries = monthFromTransaction.getFirstMonthForTransaction();
          repository.update(series.getKey(), Series.FIRST_MONTH, firstMonthForSeries);
          buf.append("Bad begin of series, updated to ").append(firstMonthForSeries);
          hasError = true;
        }
        if (monthFromTransaction.getLastMonthForTransaction() > lastMonthForSeries) {
          lastMonthForSeries = monthFromTransaction.getLastMonthForTransaction();
          repository.update(series.getKey(), Series.LAST_MONTH, lastMonthForSeries);
          buf.append("Bad end of series, updated to ").append(lastMonthForSeries);
          hasError = true;
        }

        if (firstMonthForSeries > lastMonth) {
          continue;
        }

        hasError |= checkSeriesBudget(buf, series, firstMonthForSeries, lastMonthForSeries);
        GlobList transactions =
          repository.findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES, series.get(Series.ID))
            .getGlobs();

        for (Glob transaction : transactions) {
          hasError |= checkTransactionBetweenSeriesDate(firstMonthForSeries, lastMonthForSeries, transaction, buf);
          hasError |= checkNotNullable(transaction, buf);
          hasError |= checkSavingsTransaction(transaction, buf);
        }
      }
      catch (Throwable found) {
        found.printStackTrace();
        buf.append("For series ")
          .append(series.get(Series.ID))
          .append(series.get(Series.NAME))
          .append("\n")
          ;
        StringWriter writer = new StringWriter();
        found.printStackTrace(new PrintWriter(writer));
        buf.append(writer.toString())
          .append("\n");
      }
    }

    hasError |= checkSplitedTransactions(buf);

    hasError |= checkAllSeriesBudgetAreAssociated(buf);

    TransactionToSeriesChecker toSeriesChecker = new TransactionToSeriesChecker(buf);
    repository.safeApply(Transaction.TYPE, GlobMatchers.ALL, toSeriesChecker);
    toSeriesChecker.deletePlanned(repository);
    hasError |= toSeriesChecker.hasError();

    return hasError;
  }

  private boolean checkSeriesBudget(StringBuilder buf, Glob series, Integer firstMonthForSeries, Integer lastMonthForSeries) {
    boolean hasError = false;
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
      if (budget == null){
          budgetToCreate.add(currentMonth);
          buf.append("Adding SeriesBudget for series : ").append(series.get(Series.NAME))
            .append(" at :").append(currentMonth).append(("\n"));
        hasError = true;
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
      hasError = true;
    }
    for (Integer month : budgetToCreate) {
      MonthsToSeriesBudgetTrigger.addMonth(repository, month);
    }
    return hasError;
  }

  private boolean checkAllSeriesBudgetAreAssociated(StringBuilder buf) {
    boolean hasError = false;
    Set<Integer> seriesId = new HashSet<Integer>();
    GlobList seriesBudgets = repository.getAll(SeriesBudget.TYPE);
    for (Glob budget : seriesBudgets) {
      Glob series = repository.findLinkTarget(budget, SeriesBudget.SERIES);
      if (series == null) {
        if (seriesId.add(budget.get(SeriesBudget.SERIES))) {
          buf.append("SeriesBudget : Missing series ")
            .append(budget.get(SeriesBudget.SERIES))
            .append("\n");
        }
        repository.delete(budget.getKey());
        hasError = true;
      }
    }
    return hasError;
  }

  private boolean checkSplitedTransactions(StringBuilder buf) {
    boolean hasError = false;
    GlobList transactions = repository.getAll(Transaction.TYPE,
                                              GlobMatchers.not(GlobMatchers.fieldIsNull(Transaction.SPLIT_SOURCE)));
    for (Glob transaction : transactions) {
      if (repository.findLinkTarget(transaction, Transaction.SPLIT_SOURCE) == null) {
        buf.append("Error : split source was deleted for ")
          .append(transaction.get(Transaction.LABEL)).append(" : ")
          .append(Month.toString(transaction.get(Transaction.MONTH), transaction.get(Transaction.DAY)))
          .append(("\n"));
        hasError = true;
      }
    }
    return hasError;
  }

  private boolean checkTransactionBetweenSeriesDate(Integer firstMonthForSeries, Integer lastMonthForSeries, Glob transaction, StringBuilder buf) {
    Integer month = transaction.get(Transaction.MONTH);
    if (month < firstMonthForSeries || month > lastMonthForSeries) {
      buf.append("Transaction is not in Series dates ")
        .append(Month.toString(transaction.get(Transaction.BANK_MONTH),
                               transaction.get(Transaction.BANK_DAY)))
        .append(" ").append(transaction.get(Transaction.LABEL));
      return true;
    }
    return false;
  }

  private boolean checkSavingsTransaction(Glob transaction, StringBuilder buf) {
    Glob target = repository.findLinkTarget(transaction, Transaction.ACCOUNT);
    if (target.get(Account.ACCOUNT_TYPE).equals(AccountType.SAVINGS.getId())) {
      Glob savingsSeries = repository.findLinkTarget(transaction, Transaction.SERIES);
      if (transaction.get(Transaction.AMOUNT) >= 0) {
        Integer toAccount = savingsSeries.get(Series.TO_ACCOUNT);
        if (toAccount != null && !transaction.get(Transaction.ACCOUNT).equals(toAccount)) {
          buf.append("savings transaction badly categorized ")
            .append(transaction.get(Transaction.LABEL))
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
          return false;
        }
      }
      else {
        Integer fromAccount = savingsSeries.get(Series.FROM_ACCOUNT);
        if (fromAccount != null && !transaction.get(Transaction.ACCOUNT).equals(fromAccount)) {
          buf.append("savings transaction badly categorized ").append(transaction.get(Transaction.LABEL))
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
          return false;
        }
      }
      return true;
    }
    return false;
  }

  private boolean checkNotNullable(Glob glob, StringBuilder buf) {
    boolean hasError = false;
    Field[] fields = glob.getType().getFields();
    for (Field field : fields) {
      if (field.hasAnnotation(Required.class)) {
        if (glob.getValue(field) == null) {
          buf.append(field).append(" should not be null\n");
          hasError = true;
        }
      }
    }
    return hasError;
  }

  private static class TransactionToSeriesChecker implements GlobFunctor {
    private final StringBuilder buf;
    private GlobList transactionsToDelete = new GlobList();
    private boolean hasError = false;

    public TransactionToSeriesChecker(StringBuilder buf) {
      this.buf = buf;
    }

    public void run(Glob glob, GlobRepository repository) throws Exception {
      Glob target = repository.findLinkTarget(glob, Transaction.SERIES);
      if (target == null) {
        hasError = true;
        if (glob.isTrue(Transaction.PLANNED)) {
          buf.append("no series for planned transaction ");
          transactionsToDelete.add(glob);
        }
        else {
          buf.append("no series for operations : ");
          repository.update(glob.getKey(), FieldValue.value(Transaction.SERIES, null),
                            FieldValue.value(Transaction.SUB_SERIES, null));
        }
        buf.append(glob.get(Transaction.LABEL))
          .append(" bank date : ")
          .append(Month.toString(glob.get(Transaction.BANK_MONTH), glob.get(Transaction.BANK_DAY)))
          .append(" user date : ")
          .append(Month.toString(glob.get(Transaction.MONTH), glob.get(Transaction.DAY)));
      }
    }

    public void deletePlanned(GlobRepository repository) {
      repository.delete(transactionsToDelete);
    }

    public boolean hasError() {
      return hasError;
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
