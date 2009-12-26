package org.designup.picsou.triggers;

import org.designup.picsou.gui.TimeService;
import org.designup.picsou.model.CurrentMonth;
import org.designup.picsou.model.Transaction;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobFunctor;
import static org.globsframework.model.utils.GlobMatchers.*;

import java.util.Set;

public class CurrentMonthTrigger implements ChangeSetListener {

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (!changeSet.containsChanges(CurrentMonth.KEY) &&
        !changeSet.containsCreationsOrDeletions(Transaction.TYPE)) {
      return;
    }
    Glob currentMonth = repository.get(CurrentMonth.KEY);
    final int previousLastMonth = currentMonth.get(CurrentMonth.LAST_TRANSACTION_MONTH);
    int previousLastDay = currentMonth.get(CurrentMonth.LAST_TRANSACTION_DAY);
    int lastMonth = previousLastMonth;
    int lastDay = previousLastDay;
    if (changeSet.containsCreationsOrDeletions(Transaction.TYPE)) {
      MonthCallback monthCallback = new MonthCallback();
      repository.safeApply(Transaction.TYPE, ALL, monthCallback);
      lastMonth = monthCallback.getLastMonthId();
      if (previousLastMonth != lastMonth) {
        repository.update(CurrentMonth.KEY, CurrentMonth.LAST_TRANSACTION_MONTH, lastMonth);
        previousLastDay = -1;
      }
      DayCallback dayCallback = new DayCallback(lastMonth, previousLastDay);
      repository.safeApply(Transaction.TYPE, ALL, dayCallback);
      lastDay = dayCallback.getLastMonthDay();
      if (previousLastDay != lastDay) {
        repository.update(CurrentMonth.KEY, CurrentMonth.LAST_TRANSACTION_DAY, lastDay);
      }
    }

    if (changeSet.containsChanges(CurrentMonth.KEY) ||
        (previousLastMonth != lastMonth) ||
        (previousLastDay != lastDay)) {
      GlobList transactions =
        repository.getAll(Transaction.TYPE,
                          and(isTrue(Transaction.PLANNED),
                              fieldStrictlyLessThan(Transaction.MONTH, lastMonth)));
      repository.delete(transactions);
      repository.safeApply(Transaction.TYPE, ALL,
                           new UpdateDayCallback(lastMonth, lastDay));
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
  }

  private static class MonthCallback implements GlobFunctor {
    private int lastMonthId = 0;

    public void run(Glob transaction, GlobRepository repository) {
      Integer monthId = transaction.get(Transaction.BANK_MONTH);
      if (!transaction.isTrue(Transaction.PLANNED) && monthId > lastMonthId) {
        lastMonthId = monthId;
      }
    }

    public int getLastMonthId() {
      int currentMonthId = TimeService.getCurrentMonth();
      if (lastMonthId > currentMonthId) {
        return currentMonthId;
      }
      return lastMonthId;
    }
  }

  private static class DayCallback implements GlobFunctor {
    private int lastMonthId;
    private int lastMonthDay;

    public DayCallback(int lastMonthId, int lastMonthDay) {
      this.lastMonthId = lastMonthId;
      this.lastMonthDay = lastMonthDay;
    }

    public void run(Glob glob, GlobRepository repository) {
      if (!glob.isTrue(Transaction.PLANNED) && glob.get(Transaction.BANK_MONTH).equals(lastMonthId)
          && glob.get(Transaction.BANK_DAY) > lastMonthDay) {
        lastMonthDay = glob.get(Transaction.BANK_DAY);
      }
    }

    public int getLastMonthDay() {
      if (lastMonthDay > TimeService.getCurrentDay() &&
          lastMonthId == TimeService.getCurrentMonth()) {
        return TimeService.getCurrentDay();
      }
      return lastMonthDay;
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
    }
  }
}
