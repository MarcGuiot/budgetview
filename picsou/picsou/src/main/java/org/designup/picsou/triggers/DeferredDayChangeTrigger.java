package org.designup.picsou.triggers;

import org.designup.picsou.model.DeferredCardDate;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.model.Month;
import org.globsframework.model.*;
import org.globsframework.model.utils.DefaultChangeSetListener;
import org.globsframework.model.utils.GlobFunctor;
import org.globsframework.model.utils.GlobMatchers;
import static org.globsframework.model.utils.GlobMatchers.*;

public class DeferredDayChangeTrigger extends DefaultChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    if (changeSet.containsChanges(DeferredCardDate.TYPE)) {
      changeSet.safeVisit(DeferredCardDate.TYPE, new ChangeSetVisitor() {
        public void visitCreation(Key key, FieldValues values) throws Exception {
        }

        public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
          if (values.contains(DeferredCardDate.DAY)) {
            final int day = values.get(DeferredCardDate.DAY);
            final int nextMonthId = Month.next(key.get(DeferredCardDate.MONTH));
            Glob nextCardDay = repository.find(Key.create(DeferredCardDate.ACCOUNT, key.get(DeferredCardDate.ACCOUNT),
                                                         DeferredCardDate.MONTH, nextMonthId));
            final int nextDay;
            if (nextCardDay != null){
              nextDay = nextCardDay.get(DeferredCardDate.DAY);
            }
            else {
              nextDay = 1;
            }
            final Integer monthId = key.get(DeferredCardDate.MONTH);
            repository.safeApply(Transaction.TYPE,
                                 and(
                                   fieldEquals(Transaction.ACCOUNT, key.get(DeferredCardDate.ACCOUNT)),
                                   or(fieldEquals(Transaction.POSITION_MONTH, monthId),
                                      fieldEquals(Transaction.BANK_MONTH, monthId))),
                                 new GlobFunctor() {
                                   public void run(Glob glob, GlobRepository repository) throws Exception {
                                     if (glob.get(Transaction.BANK_DAY) > day){
                                       repository.update(glob.getKey(),
                                                         FieldValue.value(Transaction.POSITION_DAY, nextDay),
                                                         FieldValue.value(Transaction.POSITION_MONTH, nextMonthId),
                                                         FieldValue.value(Transaction.BUDGET_DAY, nextDay),
                                                         FieldValue.value(Transaction.BUDGET_MONTH, nextMonthId)
                                                         );
                                     }
                                     else{
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

        public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        }
      });
    }

  }
}
