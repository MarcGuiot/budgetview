package org.designup.picsou.triggers;

import org.designup.picsou.gui.TimeService;
import org.designup.picsou.model.*;
import org.designup.picsou.model.util.Amounts;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobFunctor;
import org.globsframework.model.utils.GlobMatchers;
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
      repository.delete(Transaction.TYPE,
                        and(isTrue(Transaction.PLANNED),
                            fieldStrictlyLessThan(Transaction.MONTH, lastMonth)));
      repository.safeApply(Transaction.TYPE, ALL,
                           new UpdateDayCallback(lastMonth, lastDay));
    }

    if (changeSet.containsChanges(CurrentMonth.KEY)) {
      FieldValues value = changeSet.getPreviousValue(CurrentMonth.KEY);
      if (value.contains(CurrentMonth.CURRENT_MONTH)) {
        Integer previousMonth = value.get(CurrentMonth.CURRENT_MONTH);
        if (previousMonth != null) {
          GlobList series = repository.getAll(Series.TYPE, GlobMatchers.isTrue(Series.SHOULD_REPORT));
          repository.startChangeSet();
          try {
            for (Glob aSeries : series) {
              if (aSeries.get(Series.BUDGET_AREA).equals(BudgetArea.SAVINGS.getId())){
                if (Account.shouldCreateMirror(repository.findLinkTarget(aSeries, Series.FROM_ACCOUNT),
                                               repository.findLinkTarget(aSeries, Series.TO_ACCOUNT))
                  && aSeries.isTrue(Transaction.MIRROR)){
                  continue;
                }
              }

              ReadOnlyGlobRepository.MultiFieldIndexed index =
                repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, aSeries.get(Series.ID));
              Glob previousBudget = index.findByIndex(SeriesBudget.MONTH, previousMonth).getGlobs().getFirst();
              double diff = previousBudget.get(SeriesBudget.AMOUNT, 0.) - previousBudget.get(SeriesBudget.OBSERVED_AMOUNT, 0.);
              int maxMonthCount = 3;
              for (int month = Month.next(previousMonth); !Amounts.isNearZero(diff) && maxMonthCount != 0; month = Month.next(month), maxMonthCount--) {
                Glob newBudget = index.findByIndex(SeriesBudget.MONTH, month).getGlobs().getFirst();
                if (newBudget != null) {
                  Double newAmount = newBudget.get(SeriesBudget.AMOUNT, 0.);
                  if (Amounts.sameSign(newAmount + diff, newAmount) || Amounts.isNearZero(newAmount)) {
                    newAmount += diff;
                    diff = 0;
                  }
                  else {
                    diff += newAmount;
                    newAmount = 0.;
                  }
                  repository.update(newBudget.getKey(), SeriesBudget.AMOUNT, newAmount);
                }
              }
              repository.update(previousBudget.getKey(), SeriesBudget.AMOUNT,
                                previousBudget.get(SeriesBudget.OBSERVED_AMOUNT, 0.) + diff);
            }
          }
          finally {
            repository.completeChangeSet();
          }
        }
      }
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
