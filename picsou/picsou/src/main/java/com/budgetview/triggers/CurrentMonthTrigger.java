package com.budgetview.triggers;

import com.budgetview.model.Transaction;
import com.budgetview.gui.time.TimeService;
import com.budgetview.model.CurrentMonth;
import com.budgetview.model.Month;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobFunctor;

import static org.globsframework.model.utils.GlobMatchers.*;

public class CurrentMonthTrigger extends AbstractChangeSetListener {

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    boolean hasTransactionChange = changeSet.containsCreationsOrDeletions(Transaction.TYPE)
                                   || changeSet.containsUpdates(Transaction.BANK_MONTH)
                                   || changeSet.containsUpdates(Transaction.BANK_DAY);
    if (!changeSet.containsChanges(CurrentMonth.KEY) && !hasTransactionChange) {
      return;
    }

    repository.startChangeSet();
    try {
      Glob currentMonth = repository.get(CurrentMonth.KEY);
      final int previousLastMonth = currentMonth.get(CurrentMonth.LAST_TRANSACTION_MONTH);
      int previousLastDay = currentMonth.get(CurrentMonth.LAST_TRANSACTION_DAY);
      int lastMonth = previousLastMonth;
      int lastDay = previousLastDay;
      if (hasTransactionChange) {
        DayCallback dayCallback = new DayCallback(TimeService.getCurrentFullDate());
        repository.safeApply(Transaction.TYPE, ALL, dayCallback);
        lastDay = dayCallback.getLastMonthDay();
        lastMonth = dayCallback.getLastMonthId();
        if (previousLastMonth != lastMonth) {
          repository.update(CurrentMonth.KEY, CurrentMonth.LAST_TRANSACTION_MONTH, lastMonth);
        }
        if (previousLastDay != lastDay) {
          repository.update(CurrentMonth.KEY, CurrentMonth.LAST_TRANSACTION_DAY, lastDay);
        }
      }

      if (changeSet.containsChanges(CurrentMonth.KEY) ||
          (previousLastMonth != lastMonth) ||
          (previousLastDay != lastDay)) {
        repository.delete(Transaction.TYPE, and(isTrue(Transaction.PLANNED),
                                                fieldStrictlyLessThan(Transaction.MONTH, lastMonth)));
        repository.safeApply(Transaction.TYPE, ALL,
                             new UpdateDayCallback(lastMonth, lastDay));
      }
    }
    finally {
      repository.completeChangeSet();
    }
  }

  private static class DayCallback implements GlobFunctor {
    private int lastDayId = 0;
    private int currentDayId;

    public DayCallback(int currentDayId) {
      this.currentDayId = currentDayId;
    }

    public void run(Glob glob, GlobRepository repository) {
      if (glob.isTrue(Transaction.PLANNED) || Transaction.isToReconcile(glob) || Transaction.isOpenCloseAccount(glob)) {
        return;
      }
      int dayId = Month.toFullDate(glob.get(Transaction.BANK_MONTH), glob.get(Transaction.BANK_DAY));
      if (dayId > lastDayId && dayId <= currentDayId) {
        lastDayId = dayId;
      }
    }

    public int getLastMonthId() {
      return Month.getMonthIdFromFullDate(lastDayId);
    }

    public int getLastMonthDay() {
      return Month.getDayFromFullDate(lastDayId);
    }
  }

  private static class UpdateDayCallback implements GlobFunctor {
    private final int lastMonth;
    private int lastDay;

    public UpdateDayCallback(int lastMonth, int lastDay) {
      this.lastMonth = lastMonth;
      this.lastDay = lastDay;
    }

    public void run(Glob transaction, GlobRepository repository) {
      if (transaction.isTrue(Transaction.PLANNED) && transaction.get(Transaction.POSITION_MONTH).equals(lastMonth)
          && transaction.get(Transaction.POSITION_DAY) < lastDay) {
        repository.update(transaction.getKey(),
                          FieldValue.value(Transaction.POSITION_DAY, lastDay),
                          FieldValue.value(Transaction.BANK_DAY, lastDay),
                          FieldValue.value(Transaction.DAY, lastDay));
      }
      if (transaction.get(Transaction.TO_RECONCILE, Boolean.FALSE) &&
          (transaction.get(Transaction.POSITION_MONTH) < lastMonth ||
          (transaction.get(Transaction.POSITION_MONTH) == lastMonth && transaction.get(Transaction.POSITION_DAY) < lastDay))){
        repository.update(transaction.getKey(),
                          FieldValue.value(Transaction.POSITION_MONTH, lastMonth),
                          FieldValue.value(Transaction.POSITION_DAY, lastDay),
                          FieldValue.value(Transaction.BANK_MONTH, lastMonth),
                          FieldValue.value(Transaction.BANK_DAY, lastDay),
                          FieldValue.value(Transaction.BUDGET_MONTH, lastMonth),
                          FieldValue.value(Transaction.BUDGET_DAY, lastDay)
        );
      }
    }
  }
}
