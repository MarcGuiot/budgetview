package com.budgetview.triggers.savings;

import com.budgetview.shared.model.BudgetArea;
import com.budgetview.model.Series;
import com.budgetview.model.SeriesBudget;
import org.globsframework.metamodel.Field;
import org.globsframework.model.*;
import org.globsframework.model.utils.DefaultChangeSetListener;
import org.globsframework.utils.Utils;

public class SavingsUpdateSeriesMirrorTrigger extends DefaultChangeSetListener {

  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {

    changeSet.safeVisit(Series.TYPE, new UpdateMirrorSeriesChangeSetVisitor(repository));

    changeSet.safeVisit(SeriesBudget.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        Integer seriesId = values.get(SeriesBudget.SERIES);
        Glob series = repository.get(Key.create(Series.TYPE, seriesId));
        if (!series.get(Series.BUDGET_AREA).equals(BudgetArea.TRANSFER.getId())) {
          return;
        }
        Integer mirrorSeriesId = series.get(Series.MIRROR_SERIES);
        Glob budgetForMirror = repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, mirrorSeriesId)
          .findByIndex(SeriesBudget.MONTH, values.get(SeriesBudget.MONTH)).getGlobs().getFirst();
        if (budgetForMirror != null) {
          return;
        }
        int sign = Utils.equal(series.get(Series.FROM_ACCOUNT), series.get(Series.TARGET_ACCOUNT)) ? -1 : 1;
        if (mirrorSeriesId != null) {
          FieldValue[] fieldValues = values.toArray();
          for (int i = 0; i < fieldValues.length; i++) {
            FieldValue value = fieldValues[i];
            if (value.getField().equals(SeriesBudget.SERIES)) {
              fieldValues[i] = new FieldValue(SeriesBudget.SERIES, mirrorSeriesId);
            }
            else if (value.getField().equals(SeriesBudget.PLANNED_AMOUNT)) {
              fieldValues[i] = new FieldValue(SeriesBudget.PLANNED_AMOUNT,
                                              values.get(SeriesBudget.PLANNED_AMOUNT) == null ? null :
                                                -sign * Math.abs(values.get(SeriesBudget.PLANNED_AMOUNT)));
              repository.update(key, SeriesBudget.PLANNED_AMOUNT,
                                values.get(SeriesBudget.PLANNED_AMOUNT) == null ? null :
                                  sign * Math.abs(values.get(SeriesBudget.PLANNED_AMOUNT)));
            }
          }
          repository.create(Key.create(SeriesBudget.TYPE, repository.getIdGenerator()
            .getNextId(SeriesBudget.ID, 1)), fieldValues);
        }
      }

      public void visitUpdate(Key key, final FieldValuesWithPrevious values) throws Exception {
        Glob budget = repository.get(key);
        Integer seriesId = budget.get(SeriesBudget.SERIES);
        Glob series = repository.get(Key.create(Series.TYPE, seriesId));
        if (!series.get(Series.BUDGET_AREA).equals(BudgetArea.TRANSFER.getId())) {
          return;
        }
        Integer mirrorSeriesId = series.get(Series.MIRROR_SERIES);
        if (mirrorSeriesId != null) {
          final Glob mirrorBudget = repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, mirrorSeriesId)
            .findByIndex(SeriesBudget.MONTH, budget.get(SeriesBudget.MONTH)).getGlobs().getFirst();
          values.safeApply(new FieldValues.Functor() {
            public void process(Field field, Object value) throws Exception {
              if (field.equals(SeriesBudget.PLANNED_AMOUNT)) {
                repository.update(mirrorBudget.getKey(), field, value == null ? null : -((Double) value));
              }
              else {
                repository.update(mirrorBudget.getKey(), field, value);
              }
            }
          });
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        Integer seriesId = previousValues.get(SeriesBudget.SERIES);
        Glob series = repository.find(Key.create(Series.TYPE, seriesId));
        if (series == null) {
          return;
        }
        if (!series.get(Series.BUDGET_AREA).equals(BudgetArea.TRANSFER.getId())) {
          return;
        }
        Integer mirrorSeriesId = series.get(Series.MIRROR_SERIES);
        if (mirrorSeriesId != null) {
          Glob budget = repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, mirrorSeriesId)
            .findByIndex(SeriesBudget.MONTH, previousValues.get(SeriesBudget.MONTH)).getGlobs().getFirst();
          if (budget != null) {
            repository.delete(budget);
          }
        }
      }
    });
  }
}
