package com.budgetview.triggers;

import org.globsframework.model.*;
import org.globsframework.metamodel.GlobType;
import org.globsframework.utils.Utils;
import com.budgetview.desktop.model.SeriesStat;
import com.budgetview.model.CurrentMonth;
import com.budgetview.model.Series;
import com.budgetview.model.BudgetArea;
import com.budgetview.shared.utils.Amounts;

import java.util.Set;

public class SeriesStatSummaryTrigger implements ChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    if (!changeSet.containsChanges(SeriesStat.TYPE)) {
      return;
    }

    final Integer referenceMonth = findReferenceMonth(repository);
    if (referenceMonth == null){
      return;
    }
    changeSet.safeVisit(SeriesStat.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        update(repository, key, values, referenceMonth);
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        update(repository, key, values, referenceMonth);
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
      }
    });
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(Series.TYPE)) {
      Integer referenceMonth = findReferenceMonth(repository);
      if (referenceMonth == null){
        return;
      }
      for (Glob stat : repository.getAll(SeriesStat.TYPE)) {
        update(repository, stat.getKey(), stat, referenceMonth);
      }
    }
  }

  private void update(GlobRepository repository, Key statKey, FieldValues values, int referenceMonth) {
    Integer month = statKey.get(SeriesStat.MONTH);

    Double observed;
    Double planned;
    if (values.contains(SeriesStat.ACTUAL_AMOUNT) && values.contains(SeriesStat.PLANNED_AMOUNT)) {
      observed = values.get(SeriesStat.ACTUAL_AMOUNT);
      planned = values.get(SeriesStat.PLANNED_AMOUNT);
    }
    else {
      Glob stat = repository.get(statKey);
      observed = stat.get(SeriesStat.ACTUAL_AMOUNT);
      planned = stat.get(SeriesStat.PLANNED_AMOUNT);      
    }
    Double value;
    if (month > referenceMonth) {
      value = planned;
    }
    else if (month == referenceMonth) {
      BudgetArea budgetArea = SeriesStat.getBudgetArea(statKey, repository);
      value = Amounts.max(Utils.zeroIfNull(observed), planned, budgetArea.isIncome());
    }
    else {
      value = observed;
    }
    repository.update(statKey, SeriesStat.SUMMARY_AMOUNT, value);
  }

  private Integer findReferenceMonth(GlobRepository repository) {
    Glob glob = repository.find(CurrentMonth.KEY);
    if (glob == null){
      return null;
    }
    return glob.get(CurrentMonth.LAST_TRANSACTION_MONTH);
  }
}
