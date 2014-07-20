package org.designup.picsou.gui.series;

import org.designup.picsou.gui.model.PeriodBudgetAreaStat;
import org.designup.picsou.gui.model.PeriodSeriesStat;
import org.designup.picsou.gui.model.SeriesType;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.model.util.AmountMap;
import org.globsframework.gui.SelectionService;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.utils.directory.Directory;

import java.util.Set;

import static org.globsframework.model.utils.GlobMatchers.fieldEquals;

public class PeriodBudgetAreaStatUpdater implements ChangeSetListener {

  private GlobRepository repository;

  public static void init(GlobRepository repository) {
    repository.addTrigger(new PeriodBudgetAreaStatUpdater(repository));
  }

  public PeriodBudgetAreaStatUpdater(GlobRepository repository) {
    this.repository = repository;
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (changeSet.containsChanges(Transaction.TYPE)) {
      recomputeAll();
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(Transaction.TYPE)) {
      recomputeAll();
    }
  }

  private void recomputeAll() {
    AmountMap maxValues = new AmountMap();

    for (Glob stat : repository.getAll(PeriodSeriesStat.TYPE,
                                       fieldEquals(PeriodSeriesStat.TARGET_TYPE, SeriesType.SERIES.getId()))) {
      BudgetArea budgetArea = PeriodSeriesStat.getBudgetArea(stat, repository);
      maxValues.setMax(budgetArea.getId(), stat.get(PeriodSeriesStat.ABS_SUM_AMOUNT));
    }

    repository.startChangeSet();
    try {
      for (Integer budgetAreaId : maxValues.keySet()) {
        Glob budgetAreaStat = repository.findOrCreate(Key.create(PeriodBudgetAreaStat.TYPE, budgetAreaId));
        repository.update(budgetAreaStat.getKey(), PeriodBudgetAreaStat.ABS_SUM_AMOUNT, maxValues.get(budgetAreaId));
      }
    }
    finally {
      repository.completeChangeSet();
    }
  }
}