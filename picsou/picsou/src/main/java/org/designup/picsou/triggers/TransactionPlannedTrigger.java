package org.designup.picsou.triggers;

import org.designup.picsou.model.Month;
import org.designup.picsou.model.Transaction;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.exceptions.InvalidData;

import java.util.List;

public class TransactionPlannedTrigger implements ChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    if (changeSet.containsUpdates(Transaction.SERIES) ||
        changeSet.containsCreationsOrDeletions(Transaction.TYPE)) {
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
          if (values.contains(Transaction.SERIES)) {
            if (values.getPrevious(Transaction.SERIES) != null) {
              Double amount = transaction.get(Transaction.AMOUNT);
              if (values.contains(Transaction.AMOUNT)) {
                amount = values.getPrevious(Transaction.AMOUNT);
              }
              transfertToPlanned(transaction.get(Transaction.MONTH), amount,
                                 values.getPrevious(Transaction.SERIES), repository);
            }
            if (values.contains(Transaction.AMOUNT)) {
              transfertFromPlanned(repository, values.get(Transaction.SERIES),
                                   transaction.get(Transaction.MONTH), values.get(Transaction.AMOUNT));
            }
            else {
              transfertFromPlanned(repository, values.get(Transaction.SERIES),
                                   transaction.get(Transaction.MONTH), transaction.get(Transaction.AMOUNT));
            }
          }
          else if (values.contains(Transaction.AMOUNT)) {
            transfertFromPlanned(repository, transaction.get(Transaction.SERIES),
                                 transaction.get(Transaction.MONTH), values.getPrevious(Transaction.AMOUNT) -
                                                                     values.get(Transaction.AMOUNT));
          }
        }

        public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        }
      });
    }
  }

  private void transfertToPlanned(Integer monthId, Double amount, Integer oldSeries, GlobRepository repository) {
    GlobList plannedTransaction = getPlannedTransactions(repository, oldSeries, monthId);
    if (plannedTransaction.isEmpty()) {
      throw new InvalidData("no planned transaction for month " + Month.toString(monthId));
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
      if (available >= amountToDeduce) {
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
  }

  private GlobList getPlannedTransactions(GlobRepository repository, Integer series, Integer month) {
    return repository.getAll(Transaction.TYPE,
                             GlobMatchers.and(
                               GlobMatchers.fieldEquals(Transaction.SERIES, series),
                               GlobMatchers.fieldEquals(Transaction.PLANNED, true),
                               GlobMatchers.fieldEquals(Transaction.MONTH, month)))
      .sort(Transaction.DAY);
  }

  public void globsReset(GlobRepository repository, List<GlobType> changedTypes) {
  }
}
