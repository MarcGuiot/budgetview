package org.designup.picsou.triggers;

import org.designup.picsou.gui.model.BudgetStat;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.SeriesBudget;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobUtils;

import java.util.List;

public class BudgetStatComputer implements ChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    changeSet.safeVisit(SeriesBudget.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        Integer seriesId = values.get(SeriesBudget.SERIES);
        Integer monthId = values.get(SeriesBudget.MONTH);
        Double amount = values.get(SeriesBudget.AMOUNT);
        update(seriesId, monthId, amount, repository);
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        if (values.contains(SeriesBudget.AMOUNT)) {
          Glob seriesBudget = repository.get(key);
          Integer seriesId = seriesBudget.get(SeriesBudget.SERIES);
          Integer monthId = seriesBudget.get(SeriesBudget.MONTH);
          update(seriesId, monthId, values.get(SeriesBudget.AMOUNT) -
                                    values.getPrevious(SeriesBudget.AMOUNT), repository);
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        Integer seriesId = previousValues.get(SeriesBudget.SERIES);
        Integer monthId = previousValues.get(SeriesBudget.MONTH);
        Double amount = previousValues.get(SeriesBudget.AMOUNT);
        update(seriesId, monthId, -amount, repository);
      }
    });
  }

  private void update(Integer seriesId, Integer monthId, Double amount, GlobRepository repository) {
    if (seriesId != null) {
      Glob series = repository.get(Key.create(Series.TYPE, seriesId));
      Integer budgetAreaId = series.get(Series.BUDGET_AREA);
      if (budgetAreaId != null) {
        Key budgetStatId = Key.create(BudgetStat.TYPE)
          .set(BudgetStat.BUDGET_AREA, budgetAreaId).set(BudgetStat.MONTH, monthId).get();
        Glob bugdetStat = repository.findOrCreate(budgetStatId);
        GlobUtils.add(budgetStatId, bugdetStat, BudgetStat.AMOUNT, amount, repository);
      }
    }
  }

  public void globsReset(GlobRepository repository, List<GlobType> changedTypes) {
  }
}
