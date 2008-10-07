package org.designup.picsou.triggers;

import org.designup.picsou.model.Series;
import org.designup.picsou.model.Transaction;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.DefaultChangeSetVisitor;
import org.globsframework.model.utils.GlobMatchers;

import java.util.Set;

public class SeriesRenameTrigger implements ChangeSetListener {

  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    if (changeSet.containsChanges(Series.TYPE)) {
      changeSet.safeVisit(Series.TYPE, new DefaultChangeSetVisitor() {
        public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
          if (values.contains(Series.LABEL)) {
            GlobList globs = repository.findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES,
                                                    key.get(Series.ID)).getGlobs()
              .filterSelf(GlobMatchers.fieldEquals(Transaction.PLANNED, true), repository);
            for (Glob transaction : globs) {
              repository.update(transaction.getKey(), Transaction.LABEL,
                                Series.getPlannedTransactionLabel(values));
            }
          }
        }
      });
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
  }
}
