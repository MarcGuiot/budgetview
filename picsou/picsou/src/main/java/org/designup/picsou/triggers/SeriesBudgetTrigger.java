package org.designup.picsou.triggers;

import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.SeriesBudget;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;

import java.util.List;

public class SeriesBudgetTrigger implements ChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    changeSet.safeVisit(SeriesBudget.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        Glob series = repository.get(Key.create(Series.TYPE, values.get(SeriesBudget.SERIES)));
        Double amount = values.get(SeriesBudget.AMOUNT);
        update(values, series, amount, repository);
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        if (values.contains(SeriesBudget.AMOUNT)) {
          Glob seriesBudget = repository.get(key);
          Glob series = repository.get(Key.create(Series.TYPE, seriesBudget.get(SeriesBudget.SERIES)));
          Double amount = values.getPrevious(SeriesBudget.AMOUNT) - values.get(SeriesBudget.AMOUNT);
          update(values, series, amount, repository);
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        Glob series = repository.get(Key.create(Series.TYPE, previousValues.get(SeriesBudget.SERIES)));
        Double amount = previousValues.get(SeriesBudget.AMOUNT);
        update(previousValues, series, -amount, repository);
      }
    });
  }

  private void update(FieldValues values, Glob series, Double amount, GlobRepository repository) {
    if (BudgetArea.OCCASIONAL_EXPENSES.getId().equals(series.get(Series.BUDGET_AREA))) {
      return;
    }
    if (BudgetArea.INCOME.getId().equals(series.get(Series.BUDGET_AREA))) {
      updateOccasionalSeriesBudget(amount, repository, values.get(SeriesBudget.MONTH));
    }
    else {
      updateOccasionalSeriesBudget(-amount, repository, values.get(SeriesBudget.MONTH));
    }
  }

  private void updateOccasionalSeriesBudget(Double amount, GlobRepository repository, Integer monthId) {
    GlobList seriesBudgets =
      repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, Series.OCCASIONAL_SERIES_ID)
        .findByIndex(SeriesBudget.MONTH, monthId).getGlobs();
    if (seriesBudgets.isEmpty()) {
      repository.create(SeriesBudget.TYPE,
                        FieldValue.value(SeriesBudget.AMOUNT, amount),
                        FieldValue.value(SeriesBudget.MONTH, monthId),
                        FieldValue.value(SeriesBudget.SERIES, Series.OCCASIONAL_SERIES_ID));
    }
    else {
      Glob budgetSerieToUpdate = seriesBudgets.get(0);
      repository.update(budgetSerieToUpdate.getKey(), SeriesBudget.AMOUNT,
                        budgetSerieToUpdate.get(SeriesBudget.AMOUNT) + amount);
    }
  }

  public void globsReset(GlobRepository repository, List<GlobType> changedTypes) {
  }
}
