package org.designup.picsou.triggers;

import org.designup.picsou.model.Category;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.Transaction;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobMatchers;

import java.util.Set;

public class UncategorizeOnAccountChangeTrigger implements ChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    changeSet.safeVisit(Series.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        if (values.contains(Series.FROM_ACCOUNT) || values.contains(Series.TO_ACCOUNT)) {
          GlobList transactions = repository.findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES, key.get(Series.ID))
            .getGlobs().filterSelf(
            GlobMatchers.and(GlobMatchers.fieldEquals(Transaction.MIRROR, false),
                             GlobMatchers.fieldEquals(Transaction.CREATED_BY_SERIES, false)), repository);
          for (Glob transaction : transactions) {
            repository.update(transaction.getKey(), Transaction.SERIES, Series.UNCATEGORIZED_SERIES_ID);
            repository.update(transaction.getKey(), Transaction.CATEGORY, Category.NONE);
            Integer opposateTransaction = transaction.get(Transaction.NOT_IMPORTED_TRANSACTION);
            if (opposateTransaction != null) {
              repository.delete(Key.create(Transaction.TYPE, opposateTransaction));
              repository.update(transaction.getKey(), Transaction.NOT_IMPORTED_TRANSACTION, null);
            }
            if (transaction.get(Transaction.PLANNED)) {
              repository.delete(transaction.getKey());
            }
          }
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
      }
    });

  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
  }
}
