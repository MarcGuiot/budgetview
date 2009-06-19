package org.designup.picsou.gui.utils;

import org.designup.picsou.model.*;
import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.annotations.Required;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Log;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class DataCheckerAction extends AbstractAction {
  private GlobRepository repository;

  public DataCheckerAction(GlobRepository repository) {
    super("Check data (see logs)");
    this.repository = repository;
  }

  public void actionPerformed(ActionEvent e) {
    check();
  }

  private boolean check() {
    Log.write("Start checking");
    boolean hasError = false;
    try {
      GlobList months = repository.getAll(Month.TYPE).sort(Month.ID);
      if (months.size() == 0) {
        Log.write("No month");
        hasError = true;
        return false;
      }
      int firstMonth = months.getFirst().get(Month.ID);
      int lastMonth = months.getLast().get(Month.ID);

      int currentMonth = firstMonth;
      for (Glob glob : months) {
        if (glob.get(Month.ID) != currentMonth) {
          Log.write("Missing month " + currentMonth);
          hasError = true;
        }
        currentMonth = Month.next(currentMonth);
      }

      GlobList allSeries = repository.getAll(Series.TYPE);

      for (Glob series : allSeries) {
        checkNotNullable(series);
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
            Log.write("Missing SeriesBudget for series : " + series.get(Series.NAME) + " budgetArea : " +
                      series.get(Series.BUDGET_AREA) + " got " + budget.get(SeriesBudget.MONTH) +
                      " but expect " + currentMonth);
            hasError = true;
            break;
          }
          currentMonth = Month.next(currentMonth);

          checkNotNullable(budget);
        }
        if (!seriesBudgets.getLast().get(SeriesBudget.MONTH).equals(lastMonthForSeries)) {
          Log.write("Bad end of series : " + series.get(Series.NAME) + " budgetArea : " +
                    series.get(Series.BUDGET_AREA) + " got " + seriesBudgets.getLast().get(SeriesBudget.MONTH) +
                    " but expect " + currentMonth);
          hasError = true;
        }
        GlobList transactions =
          repository.getAll(Transaction.TYPE, GlobMatchers.fieldEquals(Transaction.SERIES, series.get(Series.ID)));

        for (Glob transaction : transactions) {
          Integer month = transaction.get(Transaction.MONTH);
          if (month < firstMonthForSeries || month > lastMonthForSeries) {
            Log.write("Transaction is not in Series dates " +
                      Month.toString(transaction.get(Transaction.BANK_MONTH),
                                     transaction.get(Transaction.BANK_DAY))
                      + " " + transaction.get(Transaction.LABEL));
            hasError = true;
          }
          checkNotNullable(transaction);

          Glob target = repository.findLinkTarget(transaction, Transaction.ACCOUNT);
          if (target.get(Account.ACCOUNT_TYPE).equals(AccountType.SAVINGS.getId())) {
            Glob savingsSeries = repository.findLinkTarget(transaction, Transaction.SERIES);
            if (transaction.get(Transaction.AMOUNT) >= 0) {
              Integer toAccount = savingsSeries.get(Series.TO_ACCOUNT);
              if (toAccount != null && !transaction.get(Transaction.ACCOUNT).equals(toAccount)) {
                Log.write("savings transaction badly categorized " + transaction.get(Transaction.LABEL)
                + " " + Month.toString(transaction.get(Transaction.BANK_MONTH), transaction.get(Transaction.DAY)));
              }
            }
            else {
              Integer fromAccount = savingsSeries.get(Series.FROM_ACCOUNT);
              if (fromAccount != null && !transaction.get(Transaction.ACCOUNT).equals(fromAccount)) {
                Log.write("savings transaction badly categorized " + transaction.get(Transaction.LABEL));
              }
            }
          }
        }
      }
      return hasError;
    }
    finally {
      if (hasError) {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        toolkit.beep();
      }
      Log.write("End checking");
    }
  }

  private void checkNotNullable(Glob glob) {
    Field[] fields = glob.getType().getFields();
    for (Field field : fields) {
      if (field.hasAnnotation(Required.class)) {
        if (glob.getValue(field) == null) {
          Log.write(field + " should not be null");
        }
      }
    }
  }
}
