package org.designup.picsou.triggers;

import org.designup.picsou.model.*;
import org.globsframework.model.*;
import org.globsframework.model.utils.DefaultChangeSetListener;
import org.globsframework.model.utils.GlobMatchers;

import java.util.Set;

public class DeferredOperationTrigger extends DefaultChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    final Set<Integer> deferredAccount =
      repository.getAll(Account.TYPE,
                        GlobMatchers.fieldEquals(Account.CARD_TYPE, AccountCardType.DEFERRED.getId()))
        .getValueSet(Account.ID);

    changeSet.safeVisit(Transaction.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        Integer accountId = values.get(Transaction.ACCOUNT);
        if (deferredAccount.contains(accountId)) {
          Integer monthId = values.get(Transaction.BANK_MONTH);
          Integer day = values.get(Transaction.BANK_DAY);
          Glob deferredCardDate = repository.get(Key.create(DeferredCardDate.ACCOUNT, accountId, DeferredCardDate.MONTH, monthId));
          Integer deferredDay = deferredCardDate.get(DeferredCardDate.DAY);
          if (day < deferredDay) {
            repository.update(key, FieldValue.value(Transaction.POSITION_DAY, deferredDay),
                              FieldValue.value(Transaction.POSITION_MONTH, monthId),
                              FieldValue.value(Transaction.BUDGET_DAY, 1),
                              FieldValue.value(Transaction.BUDGET_MONTH, monthId));
          }
          else {
            int nextMonthId = Month.next(monthId);
            Glob deferredCardDateForNextMonth = repository.get(Key.create(DeferredCardDate.ACCOUNT, accountId,
                                                                          DeferredCardDate.MONTH, nextMonthId));
            Integer deferredDayForNextMonth = deferredCardDateForNextMonth.get(DeferredCardDate.DAY);
            repository.update(key, FieldValue.value(Transaction.POSITION_DAY, deferredDayForNextMonth),
                              FieldValue.value(Transaction.POSITION_MONTH, nextMonthId),
                              FieldValue.value(Transaction.BUDGET_DAY, 1),
                              FieldValue.value(Transaction.BUDGET_MONTH, nextMonthId));

          }
        }
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        if (values.contains(Transaction.SERIES)){
          Integer seriesId = values.get(Transaction.SERIES);
          Glob series = repository.get(Key.create(Series.TYPE, seriesId));
          if (series.get(Series.BUDGET_AREA).equals(BudgetArea.OTHER.getId())){
            int accountId = series.get(Series.FROM_ACCOUNT);
            Glob transaction = repository.get(key);
            Integer month = transaction.get(Transaction.BANK_MONTH);
            Integer day = transaction.get(Transaction.BANK_DAY);
            repository.update(Key.create(DeferredCardDate.ACCOUNT, accountId, DeferredCardDate.MONTH, month),
                              DeferredCardDate.DAY, day);

          }
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
      }
    });

    changeSet.safeVisit(DeferredCardDate.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        if (values.contains(DeferredCardDate.DAY)) {

        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
      }
    });
  }
}
