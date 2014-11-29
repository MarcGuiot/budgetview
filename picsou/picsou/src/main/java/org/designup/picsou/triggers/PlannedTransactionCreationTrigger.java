package org.designup.picsou.triggers;

import com.budgetview.shared.utils.Amounts;
import org.designup.picsou.gui.model.SeriesShape;
import org.designup.picsou.model.*;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.model.*;
import org.globsframework.utils.Log;
import org.globsframework.utils.Utils;
import org.globsframework.utils.collections.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;

import static org.globsframework.model.FieldValue.value;
import static org.globsframework.model.utils.GlobMatchers.*;

public class PlannedTransactionCreationTrigger implements ChangeSetListener {

  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {

    repository.startChangeSet();
    final Set<Pair<Integer, Integer>> listOfSeriesAndMonths = new HashSet<Pair<Integer, Integer>>();
    if (changeSet.containsChanges(UserPreferences.KEY, UserPreferences.MULTIPLE_PLANNED,
                                  UserPreferences.MONTH_FOR_PLANNED, UserPreferences.PERIOD_COUNT_FOR_PLANNED)) {
      Glob currenMonth = repository.get(CurrentMonth.KEY);
      SortedSet<Integer> seriesId = repository.getAll(Series.TYPE).getSortedSet(Series.ID);
      GlobList months = repository.getAll(Month.TYPE);
      for (Integer id : seriesId) {
        for (Glob glob : months) {
          if (glob.get(Month.ID) >= currenMonth.get(CurrentMonth.LAST_TRANSACTION_MONTH)) {
            listOfSeriesAndMonths.add(new Pair<Integer, Integer>(id, glob.get(Month.ID)));
          }
        }
      }
    }

    changeSet.safeVisit(Series.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        if (values.contains(Series.TARGET_ACCOUNT)) {
          GlobList months = repository.getAll(Month.TYPE);
          for (Glob glob : months) {
            listOfSeriesAndMonths.add(new Pair<Integer, Integer>(key.get(Series.ID), glob.get(Month.ID)));
          }
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
      }
    });

    changeSet.safeVisit(Transaction.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        Integer seriesId = values.get(Transaction.SERIES);
        Integer monthId = values.get(Transaction.BUDGET_MONTH);
        if (seriesId != null && monthId != null) {
          listOfSeriesAndMonths.add(new Pair<Integer, Integer>(seriesId, monthId));
        }
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        Integer previousSeries;
        Integer newSeries;
        Integer newMonth;
        Integer previousMonth;
        Glob transaction = repository.get(key);
        if (values.contains(Transaction.SERIES)) {
          previousSeries = values.getPrevious(Transaction.SERIES);
          newSeries = values.get(Transaction.SERIES);
        }
        else {
          newSeries = transaction.get(Transaction.SERIES);
          previousSeries = newSeries;
        }
        if (values.contains(Transaction.BUDGET_MONTH)) {
          previousMonth = values.getPrevious(Transaction.BUDGET_MONTH);
          newMonth = values.get(Transaction.BUDGET_MONTH);
        }
        else {
          newMonth = transaction.get(Transaction.BUDGET_MONTH);
          previousMonth = newMonth;
        }
        if (values.contains(Transaction.AMOUNT)
            || Utils.equal(newSeries, previousSeries)
            || Utils.equal(newMonth, previousMonth)) {
          if (previousSeries != null) {
            listOfSeriesAndMonths.add(new Pair<Integer, Integer>(previousSeries, previousMonth));
          }
          if (newSeries != null) {
            listOfSeriesAndMonths.add(new Pair<Integer, Integer>(newSeries, newMonth));
          }
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        Integer series = previousValues.get(Transaction.SERIES);
        Integer monthId = previousValues.get(Transaction.BUDGET_MONTH);
        if (series != null) {
          listOfSeriesAndMonths.add(new Pair<Integer, Integer>(series, monthId));
        }
      }
    });

    changeSet.safeVisit(SeriesBudget.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        listOfSeriesAndMonths.add(new Pair<Integer, Integer>(values.get(SeriesBudget.SERIES),
                                                            values.get(SeriesBudget.MONTH)));
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        if (values.contains(SeriesBudget.PLANNED_AMOUNT)
            || values.contains(SeriesBudget.ACTIVE)) {
          Glob seriesBudget = repository.get(key);
          listOfSeriesAndMonths.add(new Pair<Integer, Integer>(seriesBudget.get(SeriesBudget.SERIES),
                                                              seriesBudget.get(SeriesBudget.MONTH)));
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        GlobList transactions = getPlannedTransactions(previousValues.get(SeriesBudget.SERIES), previousValues.get(SeriesBudget.MONTH), repository
        );
        repository.delete(transactions);
      }
    });

    changeSet.safeVisit(SeriesShape.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        for (Glob month : repository.getAll(Month.TYPE)) {
          listOfSeriesAndMonths.add(new Pair<Integer, Integer>(key.get(SeriesShape.SERIES_ID), month.get(Month.ID)));
        }
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        for (Glob month : repository.getAll(Month.TYPE)) {
          listOfSeriesAndMonths.add(new Pair<Integer, Integer>(key.get(SeriesShape.SERIES_ID), month.get(Month.ID)));
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        for (Glob month : repository.getAll(Month.TYPE)) {
          listOfSeriesAndMonths.add(new Pair<Integer, Integer>(key.get(SeriesShape.SERIES_ID), month.get(Month.ID)));
        }
      }
    });

    if (!listOfSeriesAndMonths.isEmpty()) {
      updatePlannedTransactions(listOfSeriesAndMonths, repository);
    }

    repository.completeChangeSet();
  }

  private void updatePlannedTransactions(Set<Pair<Integer, Integer>> listOfSeriesAndMonth, GlobRepository repository) {

    Glob currentMonth = repository.get(CurrentMonth.KEY);
    final Integer currentMonthId = currentMonth.get(CurrentMonth.LAST_TRANSACTION_MONTH);
    for (Pair<Integer, Integer> seriesAndMonth : listOfSeriesAndMonth) {
      final Integer monthId = seriesAndMonth.getSecond();
      final Integer seriesId = seriesAndMonth.getFirst();

      GlobList plannedTransactionsForMonth = getPlannedTransactions(seriesId, monthId, repository);
      Glob seriesBudget =
        repository
          .findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, seriesId)
          .findByIndex(SeriesBudget.MONTH, monthId)
          .getGlobs().getFirst();
      if (seriesBudget == null) {
        repository.delete(plannedTransactionsForMonth);
        continue;
      }

      Double actualAmount = seriesBudget.get(SeriesBudget.ACTUAL_AMOUNT);
      if (Amounts.isNullOrZero(seriesBudget.get(SeriesBudget.PLANNED_AMOUNT)) || !seriesBudget.isTrue(SeriesBudget.ACTIVE)) {
        repository.delete(plannedTransactionsForMonth);
        continue;
      }

      if (monthId >= currentMonthId) {
        Double plannedAmount = seriesBudget.get(SeriesBudget.PLANNED_AMOUNT, 0);
        double remainder = plannedAmount - Utils.zeroIfNull(actualAmount);
        if (((plannedAmount > 0 && remainder > 0) || (plannedAmount < 0 && remainder < 0)) && !Amounts.isNearZero(remainder)) {
          if (repository.get(UserPreferences.KEY).isTrue(UserPreferences.MULTIPLE_PLANNED)) {
            distributePlannedAmountOnShape(seriesBudget, monthId, plannedTransactionsForMonth, repository);
          }
          else {
            Glob transaction = plannedTransactionsForMonth.getFirst();
            if (transaction == null) {
              Glob series = repository.findLinkTarget(seriesBudget, SeriesBudget.SERIES);
              if (series == null) { // on a un bug : une serie a disparu on continue
                Log.write("Missing series " + seriesBudget.get(SeriesBudget.SERIES));
              }
              else {
                createPlannedTransaction(series, monthId, seriesBudget.get(SeriesBudget.DAY), remainder, repository);
              }
            }
            else {
              repository.update(transaction.getKey(), Transaction.AMOUNT, remainder);
              plannedTransactionsForMonth.remove(0);
              repository.delete(plannedTransactionsForMonth);
            }
          }
        }
        else {
          repository.delete(plannedTransactionsForMonth);
        }
      }
      else {
        repository.delete(plannedTransactionsForMonth);
      }
    }
  }

  private void distributePlannedAmountOnShape(Glob seriesBudget, Integer monthId, GlobList plannedTransactionsForMonth, GlobRepository repository) {
    Glob series = repository.findLinkTarget(seriesBudget, SeriesBudget.SERIES);
    if (series == null) { // bug : une series a disparu - on continue
      repository.delete(plannedTransactionsForMonth);
      Log.write("Missing series " + seriesBudget.get(SeriesBudget.SERIES));
    }
    else {
      AmountForDay[] amountsForDays =
        getDayAmounts(series, monthId, seriesBudget.get(SeriesBudget.PLANNED_AMOUNT), repository);
      double actualAmount = seriesBudget.get(SeriesBudget.ACTUAL_AMOUNT, 0.00);
      for (AmountForDay amountForDay : amountsForDays) {
        Double plannedAmount = amountForDay.amount;
        double remainder = plannedAmount - actualAmount;
        if (((plannedAmount > 0 && remainder > 0) || (plannedAmount < 0 && remainder < 0)) && !Amounts.isNearZero(remainder)) {
          amountForDay.amount = remainder;
          break;
        }
        else {
          actualAmount -= amountForDay.amount;
          amountForDay.amount = 0;
        }
      }

      for (AmountForDay amountForDay : amountsForDays) {
        Glob existingTransaction = null;
        if (plannedTransactionsForMonth.size() != 0) {
          existingTransaction = plannedTransactionsForMonth.remove(0);
        }
        if (amountForDay.amount != 0) {
          if (existingTransaction != null) {
            repository.update(existingTransaction.getKey(),
                              value(Transaction.AMOUNT, amountForDay.amount),
                              value(Transaction.DAY, amountForDay.day),
                              value(Transaction.POSITION_DAY, amountForDay.day),
                              value(Transaction.BANK_DAY, amountForDay.day),
                              value(Transaction.BUDGET_DAY, amountForDay.day));
          }
          else {
            createPlannedTransaction(amountForDay.accountId, series, monthId, amountForDay.day, amountForDay.amount, repository);
          }
        }
        else {
          if (existingTransaction != null) {
            repository.delete(existingTransaction);
          }
        }
      }
      repository.delete(plannedTransactionsForMonth);
    }
  }

  private static GlobList getPlannedTransactions(Integer series, Integer month, GlobRepository repository) {
    return repository.findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES, series)
      .findByIndex(Transaction.POSITION_MONTH, month)
      .getGlobs()
      .filter(and(isTrue(Transaction.PLANNED),
                  isNotTrue(Transaction.MIRROR)), repository)
      .sort(Transaction.DAY);
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
  }

  private static class AmountForDay {
    final int accountId;
    final int day;
    double amount;

    AmountForDay(int accountId, int day, double amount) {
      this.accountId = accountId;
      this.day = day;
      this.amount = amount;
    }
  }

  public static AmountForDay[] getDayAmounts(Glob series, int monthId, double amount, GlobRepository repository) {
    final ArrayList<AmountForDay> planneds = new ArrayList<AmountForDay>();
    computePlanned(series, repository, monthId, amount, new DeclarePlanned() {
      public void declare(int account, int day, double amountForPeriod) {
        planneds.add(new AmountForDay(account, day, amountForPeriod));
      }
    });
    return planneds.toArray(new AmountForDay[planneds.size()]);
  }

  private static void computePlanned(Glob series, GlobRepository repository, int monthId, double amount, final DeclarePlanned declarePlanned) {
    Integer minDay = 0;
    Glob currentMonth = repository.get(CurrentMonth.KEY);
    if ((currentMonth.get(CurrentMonth.LAST_TRANSACTION_MONTH) == monthId)) {
      minDay = currentMonth.get(CurrentMonth.LAST_TRANSACTION_DAY);
    }

    if (series.get(Series.TARGET_ACCOUNT) == null) {
      return;
    }
    int account;
    Glob fromAccount = repository.findLinkTarget(series, Series.FROM_ACCOUNT);
    Glob toAccount = repository.findLinkTarget(series, Series.TO_ACCOUNT);
    if (series.get(Series.MIRROR_SERIES) != null) {
      if (fromAccount == null || toAccount == null) {
        Log.write("Series " + series.get(Series.NAME) + " is a saving series with both accounts imported" +
                  " but one of the account is missing.");
        return;
      }
      account = series.get(Series.TARGET_ACCOUNT);
    }
    else {
      account = series.get(Series.TARGET_ACCOUNT); // Account.MAIN_SUMMARY_ACCOUNT_ID;
    }
    computePlanned(series, repository, monthId, amount, minDay, declarePlanned, account);
  }

  interface DeclarePlanned {
    void declare(int account, int day, double amountForPeriod);
  }

  private static void computePlanned(Glob series, GlobRepository repository, int monthId, double amount,
                                     Integer minDay, final DeclarePlanned planned, int account) {
    Integer seriesId = series.get(Series.ID);

    Integer period = repository.get(UserPreferences.KEY).get(UserPreferences.PERIOD_COUNT_FOR_PLANNED);
    Key key = Key.create(SeriesShape.TYPE, seriesId);
    Glob seriesShape = repository.find(key);
    if (seriesShape == null) {
      Integer mirrorId = series.get(Series.MIRROR_SERIES);
      if (mirrorId != null) {
        seriesShape = repository.find(Key.create(SeriesShape.TYPE, mirrorId));
      }
    }
    if (seriesShape != null && seriesShape.get(SeriesShape.FIXED_DATE) != null) {
      planned.declare(account, Math.max(minDay, Month.getDay(seriesShape.get(SeriesShape.FIXED_DATE), monthId)), amount);
    }
    else {
      if (seriesShape == null
          || seriesShape.get(SeriesShape.TOTAL, 0) == 0
          || Math.abs(amount / seriesShape.get(SeriesShape.TOTAL)) > 3) {
        seriesShape = SeriesShape.getDefault(key, period);
      }
      Integer percentToPropagate = 0;
      double alreadyAssignedAmount = 0;
      for (int i = 1; i <= period; i++) {
        IntegerField field = SeriesShape.getField(i);
        Integer percent = seriesShape.get(field);
        percent = percent == null ? 0 : percent;
        int day = SeriesShape.getDay(period, i, monthId);
        if (minDay != 0 && day < minDay) {
          int nextDay = SeriesShape.getDay(period, i + 1, monthId);
          if (nextDay < minDay && i != period) {
            percentToPropagate += percent;
          }
          else {
            double amountForPeriod = getAmount(alreadyAssignedAmount, amount, percentToPropagate + percent, i == period);
            if (!Amounts.isNearZero(amountForPeriod)) {
              alreadyAssignedAmount += amountForPeriod;
              percentToPropagate = 0;
              planned.declare(account, minDay, amountForPeriod);
            }
          }
        }
        else {
          double amountForPeriod = getAmount(alreadyAssignedAmount, amount, percentToPropagate + percent, i == period);
          if (!Amounts.isNearZero(amountForPeriod)) {
            alreadyAssignedAmount += amountForPeriod;
            percentToPropagate = 0;
            planned.declare(account, day, amountForPeriod);
          }
        }
      }
    }
  }

  private static double getAmount(double alreadyAssignedAmount, double totalAmount, int percent, boolean isLast) {
    if (isLast) {
      return totalAmount - alreadyAssignedAmount;
    }
    else {
      int amount = (int) ((totalAmount * percent) / 100);
      if (Math.abs(amount + alreadyAssignedAmount - totalAmount) < 10) {
        return totalAmount - alreadyAssignedAmount;
      }
      return amount;
    }
  }

  public static void createPlannedTransaction(Glob series, int monthId, Integer day, double amount, GlobRepository repository) {
    Glob month = repository.get(CurrentMonth.KEY);
    if ((month.get(CurrentMonth.LAST_TRANSACTION_MONTH) == monthId)
        && ((day == null) ||
            (day < month.get(CurrentMonth.LAST_TRANSACTION_DAY)))) {
      day = month.get(CurrentMonth.LAST_TRANSACTION_DAY);
    }

    Integer account = series.get(Series.TARGET_ACCOUNT);
    if (account == null) {
      account = Account.MAIN_SUMMARY_ACCOUNT_ID;
    }

    createPlannedTransaction(account, series, monthId, day, amount, repository);
  }

  private static Glob createPlannedTransaction(int accountId, Glob series, int monthId, int day, double amount, GlobRepository repository) {
    Integer seriesId = series.get(Series.ID);
    return repository.create(Transaction.TYPE,
                             value(Transaction.ACCOUNT, accountId),
                             value(Transaction.AMOUNT, amount),
                             value(Transaction.SERIES, seriesId),
                             value(Transaction.BANK_MONTH, monthId),
                             value(Transaction.BANK_DAY, day),
                             value(Transaction.POSITION_MONTH, monthId),
                             value(Transaction.POSITION_DAY, day),
                             value(Transaction.MONTH, monthId),
                             value(Transaction.DAY, day),
                             value(Transaction.BUDGET_MONTH, monthId),
                             value(Transaction.BUDGET_DAY, day),
                             value(Transaction.LABEL, Series.getPlannedTransactionLabel(seriesId, series)),
                             value(Transaction.PLANNED, true),
                             value(Transaction.TRANSACTION_TYPE,
                                   amount > 0 ? TransactionType.VIREMENT.getId() : TransactionType.PRELEVEMENT.getId()));
  }
}
