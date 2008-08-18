package org.designup.picsou.triggers;

import org.designup.picsou.gui.TimeService;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.SeriesBudget;
import org.designup.picsou.model.Transaction;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.model.utils.GlobUtils;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidState;

import java.util.Iterator;
import java.util.Set;

public class TransactionPlannedTrigger implements ChangeSetListener {
  private TimeService timeService;

  public TransactionPlannedTrigger(Directory directory) {
    timeService = directory.get(TimeService.class);
  }

  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    if (!changeSet.containsUpdates(Transaction.SERIES) &&
        !changeSet.containsChanges(Transaction.TYPE) &&
        !changeSet.containsCreationsOrDeletions(Transaction.TYPE)) {
      return;
    }

    changeSet.safeVisit(Transaction.TYPE, new ChangeSetVisitor() {

      public void visitCreation(Key key, FieldValues values) throws Exception {
        if (values.get(Transaction.PLANNED)) {
          return;
        }
        Integer series = values.get(Transaction.SERIES);
        if (series == null) {
          return;
        }
        transfertFromPlanned(repository, series, values.get(Transaction.MONTH), values.get(Transaction.AMOUNT));
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        Glob transaction = repository.get(key);
        if (transaction.get(Transaction.PLANNED)) {
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
        if (!Utils.equal(previousMonth, newMonth) ||
            !Utils.equal(previousSeries, newSeries) ||
            !Utils.equal(previousAmount, newAmount)) {
          if (previousAmount != null && previousSeries != null) {
            transfertToPlanned(previousMonth, previousAmount, previousSeries,
                               transaction.get(Transaction.DAY), repository);
          }
          if (newAmount != null && newSeries != null) {
            transfertFromPlanned(repository, newSeries, newMonth, newAmount);
          }
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
      }
    });
  }

  private void transfertToPlanned(Integer monthId, Double amount, Integer seriesId, Integer day, GlobRepository repository) {
    int id = timeService.getLastAvailableTransactionMonthId();
    if (monthId < id) {
      return;
    }
    GlobList plannedTransaction = getPlannedTransactions(repository, seriesId, monthId);
    if (plannedTransaction.isEmpty()) {
      GlobList budgets = repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, seriesId)
        .findByIndex(SeriesBudget.MONTH, monthId).getGlobs();
      if (budgets.isEmpty()) {
        throw new InvalidState("missing budgetSeries for series : " + seriesId + " at " + monthId);
      }
      Glob series = repository.get(Key.create(Series.TYPE, seriesId));
      double multiplier = BudgetArea.get(series.get(Series.BUDGET_AREA)).isIncome() ? -1 : 1;
      Double overBurnAmount = budgets.get(0).get(SeriesBudget.OVER_BURN_AMOUNT);
      if (amount * multiplier < overBurnAmount * multiplier) {
        Double amountToDeduce = overBurnAmount - amount;
        GlobUtils.add(budgets.get(0), SeriesBudget.OVER_BURN_AMOUNT, amountToDeduce, repository);
      }
      else {
        Double amountToDeduce = overBurnAmount - amount;
        repository.update(budgets.get(0).getKey(), SeriesBudget.OVER_BURN_AMOUNT, 0.0);
        SeriesBudgetUpdateTransactionTrigger
          .createPlannedTransaction(series, repository, monthId, day, amountToDeduce);
      }
    }
    else {
      Key plannedTransactionKeyToUpdate = plannedTransaction.get(0).getKey();
      Double currentAmount = repository.get(plannedTransactionKeyToUpdate).get(Transaction.AMOUNT);
      repository.update(plannedTransactionKeyToUpdate,
                        FieldValue.value(Transaction.AMOUNT, currentAmount + amount));
    }
  }

  private void transfertFromPlanned(GlobRepository repository, Integer seriesId,
                                    Integer monthId, Double amountToDeduce) {
    Glob series = repository.get(Key.create(Series.TYPE, seriesId));
    double multiplier = BudgetArea.get(series.get(Series.BUDGET_AREA)).isIncome() ? -1 : 1;
    GlobList plannedTransaction = getPlannedTransactions(repository, seriesId, monthId);
    for (Iterator it = plannedTransaction.iterator(); it.hasNext();) {
      Glob transaction = (Glob)it.next();
      Double available = transaction.get(Transaction.AMOUNT);
      Double newAmount;
      if (available * multiplier < amountToDeduce * multiplier) {
        newAmount = available - amountToDeduce;
        amountToDeduce = 0.0;
      }
      else {
        amountToDeduce -= available;
        newAmount = 0.0;
      }
      if (newAmount == 0.0) {
        repository.delete(transaction.getKey());
        it.remove();
      }
      else {
        repository.update(transaction.getKey(), FieldValue.value(Transaction.AMOUNT, newAmount));
      }
      if (amountToDeduce == 0.0) {
        break;
      }
    }
    if (amountToDeduce != 0.0) {
      GlobList budgets = repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, seriesId)
        .findByIndex(SeriesBudget.MONTH, monthId).getGlobs();
      if (budgets.isEmpty()) {
        throw new InvalidState("missing budgetSeries for series : " + seriesId + " at " + monthId);
      }
      GlobUtils.add(budgets.get(0), SeriesBudget.OVER_BURN_AMOUNT, amountToDeduce, repository);
    }
  }

  private GlobList getPlannedTransactions(GlobRepository repository, Integer series, Integer month) {
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
