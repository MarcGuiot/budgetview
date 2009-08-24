package org.designup.picsou.triggers;

import org.globsframework.model.*;
import org.globsframework.metamodel.GlobType;
import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.model.CurrentMonth;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.util.Amounts;

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

  private void update(GlobRepository repository, Key key, FieldValues values, int referenceMonth) {
    Integer month = key.get(SeriesStat.MONTH);

    Double observed;
    Double planned;
    if (values.contains(SeriesStat.AMOUNT) && values.contains(SeriesStat.PLANNED_AMOUNT)) {
      observed = values.get(SeriesStat.AMOUNT);
      planned = values.get(SeriesStat.PLANNED_AMOUNT);
    }
    else {
      Glob stat = repository.get(key);
      observed = stat.get(SeriesStat.AMOUNT);
      planned = stat.get(SeriesStat.PLANNED_AMOUNT);      
    }
    Double value;
    if (month > referenceMonth) {
      value = planned;
    }
    else if (month == referenceMonth) {
      BudgetArea budgetArea = SeriesStat.getBudgetArea(key, repository);
      value = Amounts.max(observed, planned, budgetArea.isIncome());
    }
    else {
      value = observed;
    }
    repository.update(key, SeriesStat.SUMMARY_AMOUNT, value);
  }

  private Integer findReferenceMonth(GlobRepository repository) {
    Glob glob = repository.find(CurrentMonth.KEY);
    if (glob == null){
      return null;
    }
    return glob.get(CurrentMonth.LAST_TRANSACTION_MONTH);
  }
}
