package com.budgetview.triggers;

import com.budgetview.model.Series;
import com.budgetview.model.Transaction;
import org.globsframework.model.*;
import org.globsframework.model.utils.DefaultChangeSetVisitor;

import static org.globsframework.model.utils.GlobMatchers.isTrue;

public class SeriesRenameTrigger extends AbstractChangeSetListener {

  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {

    if (changeSet.containsChanges(Series.TYPE)) {
      changeSet.safeVisit(Series.TYPE, new DefaultChangeSetVisitor() {
        public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
          Integer seriesId = key.get(Series.ID);
          if (values.contains(Series.NAME)) {
            GlobList globs =
              repository.findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES, seriesId).getGlobs()
                .filterSelf(isTrue(Transaction.PLANNED), repository);
            for (Glob transaction : globs) {
              repository.update(transaction.getKey(), Transaction.LABEL,
                                Series.getPlannedTransactionLabel(seriesId, values));
            }
          }
        }
      });
    }
  }
}
