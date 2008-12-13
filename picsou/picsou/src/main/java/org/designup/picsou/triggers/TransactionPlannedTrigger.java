package org.designup.picsou.triggers;

import org.designup.picsou.model.CurrentMonth;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.SeriesBudget;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.PicsouUtils;
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
        if (values.get(Transaction.PLANNED) || Transaction.isMirrorTransaction(values)) {
          return;
        }
        Integer seriesId = values.get(Transaction.SERIES);
        if (seriesId == null) {
          return;
        }
        Glob series = repository.get(Key.create(Series.TYPE, seriesId));
        Integer monthId = values.get(Transaction.MONTH);
        GlobList budgets = repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, seriesId)
          .findByIndex(SeriesBudget.MONTH, monthId).getGlobs();
        if (budgets.isEmpty()) {
          if (repository.find(Key.create(Series.TYPE, seriesId)) != null) {
            throw new InvalidState("missing budgetSeries for series : " + seriesId + " at " + monthId);
          }
          return;
        }

        Glob seriesBudget = budgets.get(0);
        if (PicsouUtils.isNearZero(seriesBudget.get(SeriesBudget.AMOUNT))) {
          repository.update(seriesBudget.getKey(), SeriesBudget.OVERRUN_AMOUNT,
                            seriesBudget.get(SeriesBudget.AMOUNT) - values.get(Transaction.AMOUNT));
        }
        else {
          transfertFromPlanned(series, seriesBudget, monthId, values.get(Transaction.AMOUNT), repository);
        }
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        Glob transaction = repository.find(key);
        if (transaction == null ||
            transaction.get(Transaction.PLANNED) ||
            Transaction.isMirrorTransaction(transaction)) {
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
          double amount = newAmount - previousAmount;
          transfertAmount(series, amount, newMonth, currentMonth.get(CurrentMonth.LAST_TRANSACTION_MONTH),
                          repository);
        }
        else if (!Utils.equal(previousMonth, newMonth) ||
                 !Utils.equal(previousSeries, newSeries) ||
                 !Utils.equal(previousAmount, newAmount)) {
          if (previousAmount != null && previousSeries != null) {
            Glob series = repository.find(Key.create(Series.TYPE, previousSeries));
            if (series != null) {
              transfertAmount(series, -previousAmount, previousMonth,
                              currentMonth.get(CurrentMonth.LAST_TRANSACTION_MONTH), repository);
            }
          }
          if (newAmount != null && newSeries != null) {
            Glob series = repository.get(Key.create(Series.TYPE, newSeries));
            transfertAmount(series, newAmount, newMonth,
                            currentMonth.get(CurrentMonth.LAST_TRANSACTION_MONTH), repository);
          }
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
      }
    });
  }

  public static void transfertAmount(Glob series, double amount, Integer monthId, int monthInCurrentMonth,
                                     GlobRepository repository) {
    Integer seriesId = series.get(Series.ID);
    GlobList budgets = repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, seriesId)
      .findByIndex(SeriesBudget.MONTH, monthId).getGlobs();
    if (budgets.isEmpty()) {
      if (repository.find(Key.create(Series.TYPE, seriesId)) != null) {
        throw new InvalidState("missing budgetSeries for series : " + seriesId + " at " + monthId);
      }
      return;
    }

    Glob seriesBudget = budgets.get(0);
    boolean isPositiveBudget = seriesBudget.get(SeriesBudget.AMOUNT) > 0;
    if (PicsouUtils.isNearZero(seriesBudget.get(SeriesBudget.AMOUNT))) {
      repository.update(seriesBudget.getKey(), SeriesBudget.OVERRUN_AMOUNT,
                        seriesBudget.get(SeriesBudget.OVERRUN_AMOUNT) + amount);
      GlobList plannedTransaction = getPlannedTransactions(repository, seriesId, monthId);
      repository.delete(plannedTransaction);
      return;
    }
    if (isPositiveBudget) {
      if (amount < 0) {
        transfertToPlanned(series, seriesBudget, monthId, amount, monthInCurrentMonth,
                           repository);
      }
      else if (amount > 0) {
        transfertFromPlanned(series, seriesBudget, monthId, amount, repository);
      }
    }
    else {
      if (amount > 0) {
        transfertToPlanned(series, seriesBudget, monthId, amount, monthInCurrentMonth, repository);
      }
      else if (amount < 0) {
        transfertFromPlanned(series, seriesBudget, monthId, amount, repository);
      }
    }
  }

  private static void transfertToPlanned(Glob series, Glob budget, Integer monthId, Double amount,
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
      double multiplier = budget.get(SeriesBudget.AMOUNT) > 0 ? -1 : 1;
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
      if (PicsouUtils.isNearZero(newAmount)) {
        repository.delete(plannedTransactionKeyToUpdate);
      }
      else {
        repository.update(plannedTransactionKeyToUpdate,
                          FieldValue.value(Transaction.AMOUNT, newAmount));
      }
    }
  }

  private static void transfertFromPlanned(Glob series, Glob budget, Integer monthId,
                                           Double amountToDeduce, GlobRepository repository) {
    if (Series.UNCATEGORIZED_SERIES_ID.equals(series.get(Series.ID))) {
      return;
    }
    Integer seriesId = series.get(Series.ID);
    double multiplier = budget.get(SeriesBudget.AMOUNT) > 0 ? -1 : 1;
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
      if (PicsouUtils.isNearZero(newAmount)) {
        return;
      }
      newAmount *= -1;
    }
    GlobUtils.add(budget, SeriesBudget.OVERRUN_AMOUNT, newAmount, repository);
  }

  private static GlobList getPlannedTransactions(GlobRepository repository, Integer series, Integer month) {
    return repository.getAll(Transaction.TYPE,
                             GlobMatchers.and(
                               GlobMatchers.fieldEquals(Transaction.SERIES, series),
                               GlobMatchers.fieldEquals(Transaction.PLANNED, true),
                               GlobMatchers.not(
                                 GlobMatchers.fieldEquals(Transaction.MIRROR, true)),
                               GlobMatchers.fieldEquals(Transaction.MONTH, month)))
      .sort(Transaction.DAY);
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
  }
}
