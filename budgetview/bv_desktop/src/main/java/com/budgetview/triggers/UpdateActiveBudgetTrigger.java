package com.budgetview.triggers;

import com.budgetview.model.Series;
import com.budgetview.model.SeriesBudget;
import com.budgetview.model.Transaction;
import org.globsframework.model.*;
import org.globsframework.model.utils.DefaultChangeSetListener;

import static org.globsframework.model.FieldValue.value;

public class UpdateActiveBudgetTrigger extends DefaultChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    if (changeSet.containsChanges(Transaction.TYPE)) {
      changeSet.safeVisit(Transaction.TYPE, new ChangeSetVisitor() {
        public void visitCreation(Key key, FieldValues values) throws Exception {
        }

        public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
          if (values.contains(Transaction.SERIES)) {
            Integer seriesId = values.get(Transaction.SERIES);
            if (!seriesId.equals(Series.UNCATEGORIZED_SERIES_ID)) {
              Glob transaction = repository.get(key);
              Glob budget = repository
                .findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, seriesId)
                .findByIndex(SeriesBudget.MONTH, transaction.get(Transaction.BUDGET_MONTH))
                .getGlobs().getFirst();
              if (budget != null) {
                if (!budget.isTrue(SeriesBudget.ACTIVE)) {
                  repository.update(budget.getKey(),
                                    value(SeriesBudget.ACTIVE, true),
                                    value(SeriesBudget.PLANNED_AMOUNT, 0.00));
                }
              }
            }
          }
        }

        public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        }
      });
    }
  }
}
