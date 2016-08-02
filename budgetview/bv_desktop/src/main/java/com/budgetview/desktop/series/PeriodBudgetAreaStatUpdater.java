package com.budgetview.desktop.series;

import com.budgetview.desktop.model.PeriodBudgetAreaStat;
import com.budgetview.desktop.model.PeriodSeriesStat;
import com.budgetview.desktop.model.SeriesType;
import com.budgetview.model.BudgetArea;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;

import java.util.Set;

import static org.globsframework.model.FieldValue.value;
import static org.globsframework.model.utils.GlobMatchers.*;

public class PeriodBudgetAreaStatUpdater implements ChangeSetListener {

  private GlobRepository repository;

  public static void init(GlobRepository repository) {
    repository.addTrigger(new PeriodBudgetAreaStatUpdater(repository));
  }

  public PeriodBudgetAreaStatUpdater(GlobRepository repository) {
    this.repository = repository;
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (changeSet.containsChanges(PeriodSeriesStat.TYPE)) {
      recomputeAll();
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(PeriodSeriesStat.TYPE)) {
      recomputeAll();
    }
  }

  private void recomputeAll() {

    repository.startChangeSet();
    try {
      for (BudgetArea area : BudgetArea.values()) {

        double absSumAmount = 0;
        double amount = 0;
        double plannedAmount = 0;
        double pastRemaining = 0;
        double futureRemaining = 0;
        double pastOverrun = 0;
        double futureOverrun = 0;

        for (Glob stat : repository.getAll(PeriodSeriesStat.TYPE,
                                           and(isTrue(PeriodSeriesStat.ACTIVE),
                                               fieldEquals(PeriodSeriesStat.BUDGET_AREA, area.getId()),
                                               fieldEquals(PeriodSeriesStat.TARGET_TYPE, SeriesType.SERIES.getId())))) {
          absSumAmount += stat.get(PeriodSeriesStat.ABS_SUM_AMOUNT, 0.00);
          amount += stat.get(PeriodSeriesStat.AMOUNT, 0.00);
          plannedAmount += stat.get(PeriodSeriesStat.PLANNED_AMOUNT, 0.00);
          pastRemaining += stat.get(PeriodSeriesStat.PAST_REMAINING, 0.00);
          futureRemaining += stat.get(PeriodSeriesStat.FUTURE_REMAINING, 0.00);
          pastOverrun += stat.get(PeriodSeriesStat.PAST_OVERRUN, 0.00);
          futureOverrun += stat.get(PeriodSeriesStat.FUTURE_OVERRUN, 0.00);
        }

        Glob budgetAreaStat = repository.findOrCreate(Key.create(PeriodBudgetAreaStat.TYPE, area.getId()));
        repository.update(budgetAreaStat.getKey(),
                          value(PeriodBudgetAreaStat.ABS_SUM_AMOUNT, absSumAmount),
                          value(PeriodBudgetAreaStat.AMOUNT, amount),
                          value(PeriodBudgetAreaStat.PLANNED_AMOUNT, plannedAmount),
                          value(PeriodBudgetAreaStat.PAST_REMAINING, pastRemaining),
                          value(PeriodBudgetAreaStat.FUTURE_REMAINING, futureRemaining),
                          value(PeriodBudgetAreaStat.PAST_OVERRUN, pastOverrun),
                          value(PeriodBudgetAreaStat.FUTURE_OVERRUN, futureOverrun));
      }
    }
    finally {
      repository.completeChangeSet();
    }
  }
}
