package org.designup.picsou.triggers;

import org.designup.picsou.model.Transaction;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Utils;

import java.util.Set;

public class TransactionPlannedTrigger implements ChangeSetListener {
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
            transfertToPlanned(previousMonth, previousAmount, previousSeries, repository);
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

  private void transfertToPlanned(Integer monthId, Double amount, Integer series, GlobRepository repository) {
    GlobList plannedTransaction = getPlannedTransactions(repository, series, monthId);
    if (plannedTransaction.isEmpty()) {
      return;
    }
    Key plannedTransactionKeyToUpdate = plannedTransaction.get(0).getKey();
    Double currentAmount = repository.get(plannedTransactionKeyToUpdate).get(Transaction.AMOUNT);
    repository.update(plannedTransactionKeyToUpdate,
                      FieldValue.value(Transaction.AMOUNT, currentAmount + amount));
  }

  private void transfertFromPlanned(GlobRepository repository, Integer series,
                                    Integer monthId, Double amountToDeduce) {
    GlobList plannedTransaction = getPlannedTransactions(repository, series, monthId);
    for (Glob transaction : plannedTransaction) {
      Double available = transaction.get(Transaction.AMOUNT);
      Double newAmount;
      if (available < amountToDeduce) {
        newAmount = available - amountToDeduce;
        amountToDeduce = 0.0;
      }
      else {
        amountToDeduce -= available;
        newAmount = 0.0;
      }
      repository.update(transaction.getKey(),
                        FieldValue.value(Transaction.AMOUNT, newAmount));
      if (amountToDeduce == 0.0) {
        break;
      }
    }
    if (amountToDeduce < 0.0) {
      if (!plannedTransaction.isEmpty()) {
        repository.update(plannedTransaction.get(plannedTransaction.size() - 1).getKey(),
                          FieldValue.value(Transaction.AMOUNT, -amountToDeduce));
      }
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
