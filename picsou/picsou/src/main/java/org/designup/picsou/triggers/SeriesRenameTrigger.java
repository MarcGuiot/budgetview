package org.designup.picsou.triggers;

import org.designup.picsou.model.Series;
import org.designup.picsou.model.SeriesToCategory;
import org.designup.picsou.model.Transaction;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.DefaultChangeSetVisitor;
import org.globsframework.model.utils.GlobMatchers;

import java.util.HashSet;
import java.util.Set;

public class SeriesRenameTrigger implements ChangeSetListener {

  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    final Set<Integer> updatedSeries = new HashSet<Integer>();

    if (changeSet.containsChanges(Series.TYPE)) {
      changeSet.safeVisit(Series.TYPE, new DefaultChangeSetVisitor() {
        public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
          Integer seriesId = key.get(Series.ID);
          if (values.contains(Series.NAME)) {
            GlobList globs =
              repository.findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES, seriesId).getGlobs()
                .filterSelf(GlobMatchers.fieldEquals(Transaction.PLANNED, true), repository);
            for (Glob transaction : globs) {
              repository.update(transaction.getKey(), Transaction.LABEL,
                                Series.getPlannedTransactionLabel(seriesId, values));
            }
          }
          if (values.contains(Series.DEFAULT_CATEGORY)) {
            updateTransactions(seriesId, updatedSeries, repository);
          }
        }
      });
    }
    changeSet.safeVisit(SeriesToCategory.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        Integer seriesId = values.get(SeriesToCategory.SERIES);
        updateTransactions(seriesId, updatedSeries, repository);
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        Glob seriesToCategory = repository.get(key);
        Integer seriesId = seriesToCategory.get(SeriesToCategory.SERIES);
        updateTransactions(seriesId, updatedSeries, repository);
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
      }
    });
  }

  private void updateTransactions(Integer seriesId, Set<Integer> updatedSeries, GlobRepository repository) {
    if (updatedSeries.contains(seriesId)) {
      return;
    }
    updatedSeries.add(seriesId);
    Integer categoryId = TransactionPlannedTrigger
      .getCategory(repository.get(Key.create(Series.TYPE, seriesId)), repository);
    GlobList globs = repository.findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES,
                                            seriesId).getGlobs()
      .filterSelf(GlobMatchers.fieldEquals(Transaction.PLANNED, true), repository);
    for (Glob transaction : globs) {
      repository.update(transaction.getKey(), Transaction.CATEGORY, categoryId);
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
  }

}
