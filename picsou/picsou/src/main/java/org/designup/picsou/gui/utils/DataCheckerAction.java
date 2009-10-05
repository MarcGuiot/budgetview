package org.designup.picsou.gui.utils;

import org.designup.picsou.gui.components.dialogs.MessageDialog;
import org.designup.picsou.model.*;
import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.annotations.Required;
import org.globsframework.model.FieldValue;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobFunctor;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Log;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

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

  private boolean check() {
    final StringBuilder buf = new StringBuilder();
    buf.append("Start checking");
    boolean hasError = false;
    try {
      GlobList months = repository.getAll(Month.TYPE).sort(Month.ID);
      if (months.size() == 0) {
        buf.append("No month");
        hasError = true;
        return false;
      }
      int firstMonth = months.getFirst().get(Month.ID);
      int lastMonth = months.getLast().get(Month.ID);

      int currentMonth = firstMonth;
      for (Glob glob : months) {
        if (glob.get(Month.ID) != currentMonth) {
          buf.append("Missing month ").append(currentMonth);
          hasError = true;
        }
        currentMonth = Month.next(currentMonth);
      }

      GlobList allSeries = repository.getAll(Series.TYPE);

      for (Glob series : allSeries) {
        checkNotNullable(series, buf);
        Integer firstMonthForSeries = series.get(Series.FIRST_MONTH);
        if (firstMonthForSeries == null) {
          firstMonthForSeries = firstMonth;
        }
        Integer lastMonthForSeries = series.get(Series.LAST_MONTH);
        if (lastMonthForSeries == null || lastMonthForSeries > lastMonth) {
          lastMonthForSeries = lastMonth;
        }

        GlobList seriesBudgets =
          repository.getAll(SeriesBudget.TYPE, GlobMatchers.fieldEquals(SeriesBudget.SERIES, series.get(Series.ID)))
            .sort(SeriesBudget.MONTH);

        currentMonth = firstMonthForSeries;


        for (Glob budget : seriesBudgets) {
          if (budget.get(SeriesBudget.MONTH) != currentMonth) {
            buf.append("Missing SeriesBudget for series : ")
              .append(series.get(Series.NAME)).append(" budgetArea : ")
              .append(series.get(Series.BUDGET_AREA)).append(" got ")
              .append(budget.get(SeriesBudget.MONTH)).append(" but expect ").append(currentMonth);
            hasError = true;
            break;
          }
          currentMonth = Month.next(currentMonth);

          checkNotNullable(budget, buf);
        }
        if (!seriesBudgets.getLast().get(SeriesBudget.MONTH).equals(lastMonthForSeries)) {
          buf.append("Bad end of series : ").append(series.get(Series.NAME))
            .append(" budgetArea : ").append(series.get(Series.BUDGET_AREA))
            .append(" got ").append(seriesBudgets.getLast().get(SeriesBudget.MONTH))
            .append(" but expect ").append(currentMonth);
          hasError = true;
        }
        GlobList transactions =
          repository.getAll(Transaction.TYPE, GlobMatchers.fieldEquals(Transaction.SERIES, series.get(Series.ID)));

        for (Glob transaction : transactions) {
          hasError |= checkTransactionBetweenSeriesDate(firstMonthForSeries, lastMonthForSeries, transaction, buf);
          hasError |= checkNotNullable(transaction, buf);
          hasError |= checkSavingsTransaction(transaction, buf);
        }
      }

      GlobList transactions = repository.getAll(Transaction.TYPE,
                                                GlobMatchers.not(GlobMatchers.fieldIsNull(Transaction.SPLIT_SOURCE)));
      for (Glob transaction : transactions) {
        if (repository.findLinkTarget(transaction, Transaction.SPLIT_SOURCE) == null) {
          buf.append("Error : split source was deleted for ")
            .append(transaction.get(Transaction.LABEL)).append(" : ")
            .append(Month.toString(transaction.get(Transaction.MONTH), transaction.get(Transaction.DAY)));
          hasError = true;
        }
      }

      TransactionToSeriesChecker toSeriesChecker = new TransactionToSeriesChecker(buf);
      repository.safeApply(Transaction.TYPE, GlobMatchers.ALL, toSeriesChecker);
      toSeriesChecker.deletePlanned(repository);
      hasError |= toSeriesChecker.hasError();
      return hasError;
    }
    finally {
      buf.append("End checking");
      if (hasError) {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        toolkit.beep();
      }
      MessageDialog dialog = new MessageDialog("data.checker.title", "data.checker.message", null, directory, buf.toString());
      dialog.show();
      Log.write(buf.toString());
    }
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
            .append(" ").append(Month.toString(transaction.get(Transaction.BANK_MONTH), transaction.get(Transaction.DAY)));
          return false;
        }
      }
      else {
        Integer fromAccount = savingsSeries.get(Series.FROM_ACCOUNT);
        if (fromAccount != null && !transaction.get(Transaction.ACCOUNT).equals(fromAccount)) {
          buf.append("savings transaction badly categorized ").append(transaction.get(Transaction.LABEL));
          return false;
        }
      }
      return true;
    }
    return false;
  }

  private boolean checkNotNullable(Glob glob, StringBuilder buf) {
    Field[] fields = glob.getType().getFields();
    for (Field field : fields) {
      if (field.hasAnnotation(Required.class)) {
        if (glob.getValue(field) == null) {
          buf.append(field).append(" should not be null");
          return true;
        }
      }
    }
    return false;
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
        if (glob.get(Transaction.PLANNED)) {
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
}
