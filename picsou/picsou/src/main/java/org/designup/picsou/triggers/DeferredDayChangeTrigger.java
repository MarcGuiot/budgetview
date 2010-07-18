package org.designup.picsou.triggers;

import org.designup.picsou.model.DeferredCardDate;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Transaction;
import org.globsframework.model.*;
import org.globsframework.model.utils.DefaultChangeSetListener;
import org.globsframework.model.utils.GlobFunctor;
import static org.globsframework.model.utils.GlobMatchers.*;

public class DeferredDayChangeTrigger extends DefaultChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    if (changeSet.containsChanges(DeferredCardDate.TYPE)) {
      changeSet.safeVisit(DeferredCardDate.TYPE, new ChangeSetVisitor() {
        public void visitCreation(Key key, FieldValues values) throws Exception {
          updateTransaction(key, values, repository);
        }

        public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
          if (values.contains(DeferredCardDate.DAY)) {
            updateTransaction(key, values, repository);
          }
        }

        public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        }
      });
    }

  }

  private void updateTransaction(Key key, FieldValues values, GlobRepository repository) {
    Glob defererredCard = repository.get(key);
    final int day = values.get(DeferredCardDate.DAY);
    final int nextMonthId = Month.next(defererredCard.get(DeferredCardDate.MONTH));
    Glob nextCardDay = repository.findByIndex(DeferredCardDate.ACCOUNT_AND_DATE, DeferredCardDate.ACCOUNT, defererredCard.get(DeferredCardDate.ACCOUNT))
      .findByIndex(DeferredCardDate.MONTH, nextMonthId).getGlobs().getFirst();
    final int nextDay;
    if (nextCardDay != null) {
      nextDay = nextCardDay.get(DeferredCardDate.DAY);
    }
    else {
      nextDay = 1;
    }
    final Integer monthId = defererredCard.get(DeferredCardDate.MONTH);
    repository.safeApply(Transaction.TYPE,
                         and(
                           fieldEquals(Transaction.ACCOUNT, defererredCard.get(DeferredCardDate.ACCOUNT)),
                           or(fieldEquals(Transaction.POSITION_MONTH, monthId),
                              fieldEquals(Transaction.BANK_MONTH, monthId))),
                         new GlobFunctor() {
                           public void run(Glob glob, GlobRepository repository) throws Exception {
                             if (glob.get(Transaction.BANK_DAY) > day
                                 && glob.get(Transaction.BANK_MONTH).equals(monthId)) {
                               repository.update(glob.getKey(),
                                                 FieldValue.value(Transaction.POSITION_DAY, nextDay),
                                                 FieldValue.value(Transaction.POSITION_MONTH, nextMonthId),
                                                 FieldValue.value(Transaction.BUDGET_DAY, nextDay),
                                                 FieldValue.value(Transaction.BUDGET_MONTH, nextMonthId)
                               );
                             }
                             else {
                               repository.update(glob.getKey(),
                                                 FieldValue.value(Transaction.POSITION_DAY, day),
                                                 FieldValue.value(Transaction.POSITION_MONTH, monthId),
                                                 FieldValue.value(Transaction.BUDGET_DAY, day),
                                                 FieldValue.value(Transaction.BUDGET_MONTH, monthId)
                               );
                             }
                           }
                         });
  }
}
