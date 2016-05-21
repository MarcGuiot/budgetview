package com.budgetview.triggers;

import com.budgetview.model.*;
import org.globsframework.model.*;
import org.globsframework.model.utils.DefaultChangeSetListener;
import org.globsframework.model.utils.GlobFunctor;
import org.globsframework.model.utils.GlobMatchers;

import java.util.Set;

import static org.globsframework.model.FieldValue.value;
import static org.globsframework.model.utils.GlobMatchers.fieldEquals;

public class DeferredOperationTrigger extends DefaultChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    final Set<Integer> deferredAccount =
      repository.getAll(Account.TYPE, fieldEquals(Account.CARD_TYPE, AccountCardType.DEFERRED.getId()))
        .getValueSet(Account.ID);

    changeSet.safeVisit(Transaction.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        Integer accountId = values.get(Transaction.ACCOUNT);
        if (deferredAccount.contains(accountId)) {
          shiftTransaction(key, values, accountId, repository);
          Key accountKey = Key.create(Account.TYPE, accountId);
          Glob account = repository.get(accountKey);
          Integer mainAccountId = account.get(Account.DEFERRED_TARGET_ACCOUNT);
          if (account.get(Account.CARD_TYPE).equals(AccountCardType.DEFERRED.getId()) && mainAccountId != null) {
            repository.update(key, Transaction.ACCOUNT, mainAccountId);
          }
        }
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        if (values.contains(Transaction.SERIES)) {
          Integer seriesId = values.get(Transaction.SERIES);
          Glob series = repository.get(Key.create(Series.TYPE, seriesId));
          if (series.get(Series.BUDGET_AREA).equals(BudgetArea.OTHER.getId())
              && series.get(Series.FROM_ACCOUNT) != null) {
            int accountId = series.get(Series.FROM_ACCOUNT);
            Glob transaction = repository.get(key);
            Integer month = transaction.get(Transaction.BANK_MONTH);
            Integer day = transaction.get(Transaction.BANK_DAY);
            if (month == null) {
              month = transaction.get(Transaction.MONTH);
              day = transaction.get(Transaction.DAY);
            }
            Glob deferredCard = repository.findByIndex(DeferredCardDate.ACCOUNT_AND_DATE, DeferredCardDate.ACCOUNT, accountId)
              .findByIndex(DeferredCardDate.MONTH, month).getGlobs().getFirst();
            if (deferredCard != null) {
              Key deferredCardKey = deferredCard.getKey();
              repository.update(deferredCardKey, DeferredCardDate.DAY, day);
            }
          }
          updateDay(key, repository.get(key), repository, values.getPrevious(Transaction.SERIES));
        }
        if (values.contains(Transaction.BUDGET_MONTH)) {
          Glob account = repository.findLinkTarget(repository.get(key), Transaction.ACCOUNT);
          if (!account.get(Account.CARD_TYPE).equals(AccountCardType.DEFERRED.getId())) {
            return;
          }
          Integer accountId = account.get(Account.ID);
          Glob deferredCard = repository.findByIndex(DeferredCardDate.ACCOUNT_AND_DATE, DeferredCardDate.ACCOUNT, accountId)
            .findByIndex(DeferredCardDate.MONTH, values.get(Transaction.BUDGET_MONTH)).getGlobs().getFirst();
          int day = account.get(Account.DEFERRED_DEBIT_DAY);
          if (deferredCard != null) {
            day = deferredCard.get(DeferredCardDate.DAY);
          }
          repository.update(key,
                            value(Transaction.POSITION_MONTH, values.get(Transaction.BUDGET_MONTH)),
                            value(Transaction.POSITION_DAY, Month.getDay(day, values.get(Transaction.BUDGET_MONTH))));
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        updateDay(key, previousValues, repository, previousValues.get(Transaction.SERIES));
      }
    });
    changeSet.safeVisit(Account.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
      }

      public void visitUpdate(final Key key, final FieldValuesWithPrevious values) throws Exception {
        if (values.contains(Account.DEFERRED_TARGET_ACCOUNT)) {
          Integer newTargetAccount = values.get(Account.DEFERRED_TARGET_ACCOUNT);
          if (newTargetAccount != null) {
            GlobList all = repository.getAll(Transaction.TYPE, GlobMatchers.fieldEquals(Transaction.ORIGINAL_ACCOUNT, key.get(Account.ID)));
            for (Glob glob : all) {
              repository.update(glob.getKey(), Transaction.ACCOUNT, newTargetAccount);
            }
          }
        }
        if (values.contains(Account.DEFERRED_DAY) || values.contains(Account.DEFERRED_MONTH_SHIFT)) {
          if (AccountCardType.DEFERRED.getId().equals(repository.get(key).get(Account.CARD_TYPE))) {
            repository.safeApply(Transaction.TYPE, fieldEquals(Transaction.ORIGINAL_ACCOUNT, key.get(Account.ID)),
                                 new GlobFunctor() {
                                   public void run(Glob glob, GlobRepository repository) throws Exception {
                                     shiftTransaction(glob.getKey(), glob, key.get(Account.ID), repository);
                                   }
                                 });

          }
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
      }
    });
  }

  static public void shiftTransaction(Key key, FieldValues values, Integer accountId, GlobRepository repository) {
    Integer monthId = values.get(Transaction.BANK_MONTH);
    Integer day = values.get(Transaction.BANK_DAY);
    if (monthId == null) {
      monthId = values.get(Transaction.MONTH);
      day = values.get(Transaction.DAY);
    }
    Glob account = repository.get(Key.create(Account.TYPE, accountId));
    Integer startDay = Month.getDay(account.get(Account.DEFERRED_DAY), monthId);
    Integer monthShift = account.get(Account.DEFERRED_MONTH_SHIFT);
    int deferredMonthId = monthId;
    if (day > startDay) {
      deferredMonthId = Month.next(deferredMonthId);
    }
    deferredMonthId = Month.next(deferredMonthId, monthShift);

    Glob deferredCardDate =
      repository.findByIndex(DeferredCardDate.ACCOUNT_AND_DATE, DeferredCardDate.ACCOUNT, account.get(Account.ID))
        .findByIndex(DeferredCardDate.MONTH, deferredMonthId).getGlobs().getFirst();
    Integer deferredDay;
    if (deferredCardDate == null) {
      deferredDay = Month.getDay(account.get(Account.DEFERRED_DEBIT_DAY), deferredMonthId);
    }
    else {
      deferredDay = Month.getDay(deferredCardDate.get(DeferredCardDate.DAY), deferredMonthId);
    }
    repository.update(key, value(Transaction.POSITION_DAY, deferredDay),
                      value(Transaction.POSITION_MONTH, deferredMonthId),
                      value(Transaction.BUDGET_DAY, deferredDay),
                      value(Transaction.BUDGET_MONTH, deferredMonthId));
  }

  private void updateDay(Key key, FieldValues values, GlobRepository repository, final Integer previousSeriesId) {
    Glob previousSeries = repository.find(Key.create(Series.TYPE, previousSeriesId));
    if (previousSeries != null
        && previousSeries.get(Series.BUDGET_AREA).equals(BudgetArea.OTHER.getId())
        && previousSeries.get(Series.FROM_ACCOUNT) != null) {
      Integer accountId = previousSeries.get(Series.FROM_ACCOUNT);
//      Glob transaction = repository.get(key);
      Integer month = values.get(Transaction.POSITION_MONTH);
      // cas ou plusieurs operations auraient été associées
      // a la Serie.
      Glob operation = repository.findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES, previousSeriesId)
        .findByIndex(Transaction.POSITION_MONTH, month).getGlobs().sort(Transaction.BANK_DAY).getFirst();
      Glob account = repository.get(Key.create(Account.TYPE, accountId));
      if (!account.get(Account.CARD_TYPE).equals(AccountCardType.DEFERRED.getId())) {
        return;
      }
      Integer day = account.get(Account.DEFERRED_DEBIT_DAY);
      if (operation != null) {
        day = operation.get(Transaction.BANK_DAY);
      }
      Glob deferredCard = repository
        .findByIndex(DeferredCardDate.ACCOUNT_AND_DATE, DeferredCardDate.ACCOUNT, accountId)
        .findByIndex(DeferredCardDate.MONTH, month).getGlobs().getFirst();
      if (deferredCard != null) {
        Key deferredCardKey = deferredCard.getKey();
        repository.update(deferredCardKey, DeferredCardDate.DAY, Month.getDay(day, month));
      }
    }
  }
}
