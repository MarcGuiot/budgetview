package org.designup.picsou.triggers;

import org.designup.picsou.model.*;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.model.utils.GlobUtils;
import org.globsframework.utils.Utils;
import org.globsframework.utils.exceptions.InvalidState;

import java.util.Iterator;
import java.util.Set;

public class TransactionPlannedTrigger implements ChangeSetListener {

  public TransactionPlannedTrigger() {
  }

  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    changeSet.safeVisit(Transaction.TYPE, new ChangeSetVisitor() {

      public void visitCreation(Key key, FieldValues values) throws Exception {
        if (values.get(Transaction.PLANNED)) {
          return;
        }
        Integer series = values.get(Transaction.SERIES);
        if (series == null) {
          return;
        }
        transfertFromPlanned(repository.get(Key.create(Series.TYPE, series)), values.get(Transaction.MONTH),
                             values.get(Transaction.AMOUNT), repository);
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        Glob transaction = repository.find(key);
        if (transaction == null || transaction.get(Transaction.PLANNED)) {
          return;
        }
        Integer previousSeries;
        Integer newSeries;
        Double previousAmount;
        Double newAmount;
        Integer newMonth;
        Integer previousMonth;
        if (values.contains(Transaction.SERIES)) {
          previousSeries = values.getPrevious(Transaction.SERIES);
          newSeries = values.get(Transaction.SERIES);
        }
        else {
          newSeries = transaction.get(Transaction.SERIES);
          previousSeries = newSeries;
        }
        if (values.contains(Transaction.AMOUNT)) {
          previousAmount = values.getPrevious(Transaction.AMOUNT);
          newAmount = values.get(Transaction.AMOUNT);
        }
        else {
          newAmount = transaction.get(Transaction.AMOUNT);
          previousAmount = newAmount;
        }
        if (values.contains(Transaction.MONTH)) {
          previousMonth = values.getPrevious(Transaction.MONTH);
          newMonth = values.get(Transaction.MONTH);
        }
        else {
          newMonth = transaction.get(Transaction.MONTH);
          previousMonth = newMonth;
        }
        Glob currentMonth = repository.get(CurrentMonth.KEY);
        if (Utils.equal(previousMonth, newMonth) && Utils.equal(previousSeries, newSeries)) {
          if (Utils.equal(previousAmount, newAmount) || newSeries == null) {
            return;
          }
          Glob series = repository.get(Key.create(Series.TYPE, newSeries));
          boolean isIncome = BudgetArea.get(series.get(Series.BUDGET_AREA)).isIncome();
          double amount = newAmount - previousAmount;
          transfertAmount(series, amount, newMonth, isIncome,
                          currentMonth.get(CurrentMonth.MONTH_ID),
                          repository);
        }
        else if (!Utils.equal(previousMonth, newMonth) ||
                 !Utils.equal(previousSeries, newSeries) ||
                 !Utils.equal(previousAmount, newAmount)) {
          if (previousAmount != null && previousSeries != null) {
            Glob series = repository.find(Key.create(Series.TYPE, previousSeries));
            if (series != null) {
              transfertAmount(series, -previousAmount,
                              previousMonth, BudgetArea.get(series.get(Series.BUDGET_AREA)).isIncome(),
                              currentMonth.get(CurrentMonth.MONTH_ID), repository);
            }
          }
          if (newAmount != null && newSeries != null) {
            Glob series = repository.get(Key.create(Series.TYPE, newSeries));
            transfertAmount(series, newAmount,
                            newMonth, BudgetArea.get(series.get(Series.BUDGET_AREA)).isIncome(),
                            currentMonth.get(CurrentMonth.MONTH_ID), repository);
          }
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
      }
    });
  }

  public static void transfertAmount(Glob series, double amount, Integer monthId,
                                     boolean isIncome, int monthInCurrentMonth,
                                     GlobRepository repository) {
    if (isIncome) {
      if (amount < 0) {
        transfertToPlanned(series, monthId, amount, monthInCurrentMonth,
                           repository);
      }
      else if (amount > 0) {
        transfertFromPlanned(series, monthId, amount, repository);
      }
    }
    else {
      if (amount > 0) {
        transfertToPlanned(series, monthId, amount, monthInCurrentMonth, repository);
      }
      else if (amount < 0) {
        transfertFromPlanned(series, monthId, amount, repository);
      }
    }
  }

  private static void transfertToPlanned(Glob series, Integer monthId, Double amount,
                                         int lastMonthInCurrentMonth, GlobRepository repository) {
    if (Series.UNCATEGORIZED_SERIES_ID.equals(series.get(Series.ID))) {
      return;
    }
    Integer seriesId = series.get(Series.ID);
    if (monthId < lastMonthInCurrentMonth) {
      return;
    }
    GlobList plannedTransaction = getPlannedTransactions(repository, seriesId, monthId);
    if (plannedTransaction.isEmpty()) {
      GlobList budgets = repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, seriesId)
        .findByIndex(SeriesBudget.MONTH, monthId).getGlobs();
      if (budgets.isEmpty()) {
        if (repository.find(Key.create(Series.TYPE, seriesId)) != null) {
          throw new InvalidState("missing budgetSeries for series : " + seriesId + " at " + monthId);
        }
        return;
      }
      Glob budget = budgets.get(0);
      double multiplier = BudgetArea.get(series.get(Series.BUDGET_AREA)).isIncome() ? -1 : 1;
      Double overBurnAmount = budget.get(SeriesBudget.OVERRUN_AMOUNT);
      Double amountToDeduce = overBurnAmount + amount;
      if (multiplier * amountToDeduce <= 10E-6) {
        repository.update(budget.getKey(), SeriesBudget.OVERRUN_AMOUNT, amountToDeduce);
      }
      else {
        repository.update(budget.getKey(), SeriesBudget.OVERRUN_AMOUNT, 0.0);
        SeriesBudgetUpdateTransactionTrigger
          .createPlannedTransaction(series, repository, monthId, budget.get(SeriesBudget.DAY),
                                    -amountToDeduce);
      }
    }
    else {
      Key plannedTransactionKeyToUpdate = plannedTransaction.get(0).getKey();
      Double currentAmount = repository.get(plannedTransactionKeyToUpdate).get(Transaction.AMOUNT);
      double newAmount = currentAmount - amount;
      if (newAmount > -10E-6 && newAmount < 10E-6) {
        repository.delete(plannedTransactionKeyToUpdate);
      }
      else {
        repository.update(plannedTransactionKeyToUpdate,
                          FieldValue.value(Transaction.AMOUNT, newAmount));
      }
    }
  }

  private static void transfertFromPlanned(Glob series, Integer monthId,
                                           Double amountToDeduce, GlobRepository repository) {
    if (Series.UNCATEGORIZED_SERIES_ID.equals(series.get(Series.ID))) {
      return;
    }
    Integer seriesId = series.get(Series.ID);
    double multiplier = BudgetArea.get(series.get(Series.BUDGET_AREA)).isIncome() ? -1 : 1;
    GlobList plannedTransaction = getPlannedTransactions(repository, seriesId, monthId);
    Double newAmount = amountToDeduce;
    for (Iterator it = plannedTransaction.iterator(); it.hasNext();) {
      Glob transaction = (Glob)it.next();
      Double available = transaction.get(Transaction.AMOUNT);
      newAmount = available - newAmount;
      if (multiplier * newAmount < -10E-6) {
        repository.update(transaction.getKey(), FieldValue.value(Transaction.AMOUNT, newAmount));
        return;
      }
      else {
        repository.delete(transaction.getKey());
        it.remove();
      }
      if (newAmount > -10E-6 && newAmount < 10E-6) {
        return;
      }
      newAmount *= -1;
    }
    GlobList budgets = repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, seriesId)
      .findByIndex(SeriesBudget.MONTH, monthId).getGlobs();
    if (budgets.isEmpty()) {
      throw new InvalidState("missing budgetSeries for series : " + seriesId + " at " + monthId);
    }
    GlobUtils.add(budgets.get(0), SeriesBudget.OVERRUN_AMOUNT, newAmount, repository);
  }

  private static GlobList getPlannedTransactions(GlobRepository repository, Integer series, Integer month) {
    return repository.getAll(Transaction.TYPE,
                             GlobMatchers.and(
                               GlobMatchers.fieldEquals(Transaction.SERIES, series),
                               GlobMatchers.fieldEquals(Transaction.PLANNED, true),
                               GlobMatchers.fieldEquals(Transaction.MONTH, month)))
      .sort(Transaction.DAY);
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
  }
}
