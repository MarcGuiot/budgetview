package com.budgetview.triggers;

import com.budgetview.desktop.model.SubSeriesStat;
import com.budgetview.model.SubSeries;
import com.budgetview.model.Transaction;
import com.budgetview.shared.utils.Amounts;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.DefaultChangeSetVisitor;

import java.util.Set;

import static org.globsframework.model.utils.GlobMatchers.fieldEquals;
import static org.globsframework.model.utils.GlobMatchers.isNotNull;

public class SubSeriesStatTrigger implements ChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {

    if (changeSet.containsChanges(SubSeries.TYPE)) {
      changeSet.safeVisit(SubSeries.TYPE, new DefaultChangeSetVisitor() {
        public void visitDeletion(Key key, FieldValues values) throws Exception {
          repository.delete(SubSeriesStat.TYPE,
                            fieldEquals(SubSeriesStat.SUB_SERIES, values.get(SubSeries.ID)));
        }
      });
    }

    if (changeSet.containsChanges(Transaction.TYPE)) {
      changeSet.safeVisit(Transaction.TYPE, new ChangeSetVisitor() {
        public void visitCreation(Key key, FieldValues values) throws Exception {
          if (!values.contains(Transaction.SUB_SERIES) || !values.contains(Transaction.AMOUNT)) {
            return;
          }
          add(key, repository);
        }

        public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
          if (values.contains(Transaction.SUB_SERIES) || values.contains(Transaction.BUDGET_MONTH)) {
            FieldValues previousValues = values.getPreviousValues();
            Glob transaction = repository.get(key);

            Integer previousSubSeriesId;
            if (previousValues.contains(Transaction.SUB_SERIES)) {
              previousSubSeriesId = values.getPrevious(Transaction.SUB_SERIES);
            }
            else {
              previousSubSeriesId = transaction.get(Transaction.SUB_SERIES);
            }
            
            if (previousSubSeriesId != null) {

              Integer previousBudgetMonth;
              if (previousValues.contains(Transaction.BUDGET_MONTH)) {
                previousBudgetMonth = previousValues.get(Transaction.BUDGET_MONTH);
              }
              else {
                previousBudgetMonth = transaction.get(Transaction.BUDGET_MONTH);
              }

              Double previousAmount;
              if (previousValues.contains(Transaction.AMOUNT)) {
                previousAmount = previousValues.get(Transaction.AMOUNT);
              }
              else {
                previousAmount = transaction.get(Transaction.AMOUNT);
              }

              substract(previousAmount, previousSubSeriesId, previousBudgetMonth, repository);
            }

            add(key, repository);
          }
        }

        public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
          Integer subSeriesId = previousValues.get(Transaction.SUB_SERIES);
          if (subSeriesId == null) {
            return;
          }

          substract(previousValues.get(Transaction.AMOUNT), subSeriesId,
                    previousValues.get(Transaction.BUDGET_MONTH), repository);
        }
      });
    }
  }

  private void substract(Double transactionAmount, Integer subSeriesId, Integer budgetMonth, GlobRepository repository) {
    Glob stat = repository.find(SubSeriesStat.createKey(subSeriesId, budgetMonth));

    if (stat != null) {
      double newValue = stat.get(SubSeriesStat.ACTUAL_AMOUNT) - transactionAmount;
      if (Amounts.isNearZero(newValue)) {
        repository.delete(stat);
      }
      else {
        repository.update(stat.getKey(), SubSeriesStat.ACTUAL_AMOUNT, newValue);
      }
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (!changedTypes.contains(SubSeries.TYPE) && !changedTypes.contains(Transaction.TYPE)) {
      return;
    }

    repository.deleteAll(SubSeriesStat.TYPE);
    for (Glob transaction : repository.getAll(Transaction.TYPE, isNotNull(Transaction.SUB_SERIES))) {
      add(transaction.getKey(), repository);
    }
  }

  private void add(Key transactionKey, GlobRepository repository) {

    Glob transaction = repository.get(transactionKey);
    Integer subSeriesId = transaction.get(Transaction.SUB_SERIES);
    if (subSeriesId == null) {
      return;
    }

    Glob stat = repository.findOrCreate(SubSeriesStat.createKey(subSeriesId,
                                                                transaction.get(Transaction.BUDGET_MONTH)));

    repository.update(stat.getKey(), SubSeriesStat.ACTUAL_AMOUNT,
                      stat.get(SubSeriesStat.ACTUAL_AMOUNT) + transaction.get(Transaction.AMOUNT));
  }
}
