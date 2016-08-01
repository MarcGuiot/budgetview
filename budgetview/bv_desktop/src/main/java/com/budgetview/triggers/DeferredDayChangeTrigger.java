package com.budgetview.triggers;

import com.budgetview.model.DeferredCardDate;
import com.budgetview.model.Transaction;
import org.globsframework.model.*;
import org.globsframework.model.utils.DefaultChangeSetListener;
import org.globsframework.model.utils.GlobFunctor;

import static org.globsframework.model.utils.GlobMatchers.and;
import static org.globsframework.model.utils.GlobMatchers.fieldEquals;

public class DeferredDayChangeTrigger extends DefaultChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    if (changeSet.containsChanges(DeferredCardDate.TYPE)) {
      changeSet.safeVisit(DeferredCardDate.TYPE, new ChangeSetVisitor() {
        public void visitCreation(Key key, FieldValues values) throws Exception {
          updateTransaction(key, values, repository);
        }

        public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
          if (values.contains(DeferredCardDate.DAY)) {
            updateTransaction(key, repository.get(key), repository);
          }
        }

        public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        }
      });
    }
  }

  private void updateTransaction(Key key, FieldValues values, GlobRepository repository) {
    final int day = values.get(DeferredCardDate.DAY);
    final Integer monthId = values.get(DeferredCardDate.MONTH);
    repository.safeApply(Transaction.TYPE,
                         and(
                           fieldEquals(Transaction.ORIGINAL_ACCOUNT, values.get(DeferredCardDate.ACCOUNT)),
                           fieldEquals(Transaction.POSITION_MONTH, monthId)),
                         new GlobFunctor() {
                           public void run(Glob glob, GlobRepository repository) throws Exception {
                             repository.update(glob.getKey(),
                                               FieldValue.value(Transaction.POSITION_DAY, day),
                                               FieldValue.value(Transaction.BUDGET_DAY, day));
                           }
                         });
  }
}
