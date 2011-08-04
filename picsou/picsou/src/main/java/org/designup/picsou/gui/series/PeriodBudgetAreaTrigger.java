package org.designup.picsou.gui.series;

import org.designup.picsou.gui.model.PeriodBudgetAreaStat;
import org.designup.picsou.gui.model.PeriodSeriesStat;
import org.designup.picsou.model.Series;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PeriodBudgetAreaTrigger implements ChangeSetListener {

  private GlobRepository repository;

  public static void init(GlobRepository repository) {
    repository.addTrigger(new PeriodBudgetAreaTrigger(repository));
  }

  public PeriodBudgetAreaTrigger(GlobRepository repository) {
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
    Map<Integer, Double> maxValues = new HashMap<Integer, Double>();

    for (Glob stat : repository.getAll(PeriodSeriesStat.TYPE)) {
      Double absValue = stat.get(PeriodSeriesStat.ABS_SUM_AMOUNT);
      Integer budgetAreaId = repository.findLinkTarget(stat, PeriodSeriesStat.SERIES).get(Series.BUDGET_AREA);
      Double value = maxValues.get(budgetAreaId);
      if ((value == null) || (value < absValue)) {
        maxValues.put(budgetAreaId, absValue);
      }
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
