package org.designup.picsou.triggers.savings;

import org.globsframework.model.utils.DefaultChangeSetListener;
import org.globsframework.model.*;
import org.globsframework.metamodel.Field;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.SeriesBudget;
import org.designup.picsou.model.BudgetArea;

public class SavingsUpdateSeriesMirrorTrigger extends DefaultChangeSetListener {

  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {

    changeSet.safeVisit(Series.TYPE, new UpdateMirrorSeriesChangeSetVisitor(repository));

    changeSet.safeVisit(SeriesBudget.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        Integer seriesId = values.get(SeriesBudget.SERIES);
        Glob series = repository.get(Key.create(Series.TYPE, seriesId));
        if (!series.get(Series.BUDGET_AREA).equals(BudgetArea.SAVINGS.getId())){
          return;
        }
        Integer mirrorSeriesId = series.get(Series.MIRROR_SERIES);
        Glob budget = repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, mirrorSeriesId)
          .findByIndex(SeriesBudget.MONTH, values.get(SeriesBudget.MONTH)).getGlobs().getFirst();
        if (budget != null){
          return;
        }
        int sign = series.get(Series.FROM_ACCOUNT).equals(series.get(Series.TARGET_ACCOUNT)) ? -1 : 1;
        if (mirrorSeriesId != null) {
          FieldValue[] fieldValues = values.toArray();
          for (int i = 0; i < fieldValues.length; i++) {
            FieldValue value = fieldValues[i];
            if (value.getField().equals(SeriesBudget.SERIES)) {
              fieldValues[i] = new FieldValue(SeriesBudget.SERIES, mirrorSeriesId);
            }
            else if (value.getField().equals(SeriesBudget.AMOUNT)) {
              fieldValues[i] = new FieldValue(SeriesBudget.AMOUNT,
                                              values.get(SeriesBudget.AMOUNT) == null ? null :
                                              -sign * Math.abs(values.get(SeriesBudget.AMOUNT)));
              repository.update(key, SeriesBudget.AMOUNT,
                                values.get(SeriesBudget.AMOUNT) == null ? null :
                                sign * Math.abs(values.get(SeriesBudget.AMOUNT)));
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
        if (!series.get(Series.BUDGET_AREA).equals(BudgetArea.SAVINGS.getId())){
          return;
        }
        Integer mirrorSeriesId = series.get(Series.MIRROR_SERIES);
        if (mirrorSeriesId != null) {
          final Glob mirrorBudget = repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, mirrorSeriesId)
            .findByIndex(SeriesBudget.MONTH, budget.get(SeriesBudget.MONTH)).getGlobs().getFirst();
          values.safeApply(new FieldValues.Functor() {
            public void process(Field field, Object value) throws Exception {
              if (field.equals(SeriesBudget.AMOUNT)) {
                repository.update(mirrorBudget.getKey(), field, value == null ? null : -((Double)value));
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
        if (!series.get(Series.BUDGET_AREA).equals(BudgetArea.SAVINGS.getId())){
          return;
        }
        Integer mirrorSeriesId = series.get(Series.MIRROR_SERIES);
        if (mirrorSeriesId != null) {
          Glob budget = repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, mirrorSeriesId)
            .findByIndex(SeriesBudget.MONTH, previousValues.get(SeriesBudget.MONTH)).getGlobs().getFirst();
          if (budget != null) {
            repository.delete(budget.getKey());
          }
        }
      }
    });
  }
}
