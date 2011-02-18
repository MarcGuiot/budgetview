package org.designup.picsou.triggers;

import org.globsframework.model.utils.DefaultChangeSetListener;
import org.globsframework.model.*;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.model.SeriesBudget;
import org.designup.picsou.model.Series;

public class UpdateActiveBudgetTrigger extends DefaultChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    if (changeSet.containsChanges(Transaction.TYPE)){
      changeSet.safeVisit(Transaction.TYPE, new ChangeSetVisitor() {
        public void visitCreation(Key key, FieldValues values) throws Exception {
        }

        public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
          if (values.contains(Transaction.SERIES)){
            Integer seriesId = values.get(Transaction.SERIES);
            if (!seriesId.equals(Series.UNCATEGORIZED_SERIES_ID)) {
            Glob transaction = repository.get(key);
              Glob budget = repository
                .findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, seriesId)
                .findByIndex(SeriesBudget.MONTH, transaction.get(Transaction.BUDGET_MONTH))
                .getGlobs().getFirst();
              if (budget != null){
                if (!budget.isTrue(SeriesBudget.ACTIVE)){
                  repository.update(budget.getKey(),
                                    FieldValue.value(SeriesBudget.ACTIVE, true),
                                    FieldValue.value(SeriesBudget.AMOUNT, 0.));
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
