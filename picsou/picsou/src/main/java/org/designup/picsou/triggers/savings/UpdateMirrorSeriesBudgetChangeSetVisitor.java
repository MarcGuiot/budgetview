package org.designup.picsou.triggers.savings;

import org.designup.picsou.model.Series;
import org.designup.picsou.model.SeriesBudget;
import org.designup.picsou.model.BudgetArea;
import org.globsframework.metamodel.Field;
import org.globsframework.model.*;
import org.globsframework.model.repository.LocalGlobRepository;
import org.globsframework.utils.Log;

public class UpdateMirrorSeriesBudgetChangeSetVisitor implements ChangeSetVisitor {
  private LocalGlobRepository localRepository;

  public UpdateMirrorSeriesBudgetChangeSetVisitor(LocalGlobRepository localRepository) {
    this.localRepository = localRepository;
  }

  public void visitCreation(Key key, FieldValues values) throws Exception {
    updateMirror(key, values);
  }

  public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
    updateMirror(key, values);
  }

  private void updateMirror(Key key, FieldValues values) {
    Glob budget = localRepository.get(key);
    final Glob series = localRepository.find(Key.create(Series.TYPE, budget.get(SeriesBudget.SERIES)));
    if (!series.get(Series.BUDGET_AREA).equals(BudgetArea.SAVINGS.getId())){
      return;
    }
    Glob fromAccount = localRepository.findLinkTarget(series, Series.FROM_ACCOUNT);
    Glob toAccount = localRepository.findLinkTarget(series, Series.TO_ACCOUNT);
//    if (Account.areBothImported(fromAccount, toAccount))
    {
//      if (series.isTrue(Series.IS_MIRROR)) {
//        return;
//      }
      Integer mirrorSeries = series.get(Series.MIRROR_SERIES);
      final Glob mirrorBudget =
        localRepository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, mirrorSeries)
          .findByIndex(SeriesBudget.MONTH, budget.get(SeriesBudget.MONTH)).getGlobs().getFirst();
      if (mirrorBudget == null){
        Log.write("missing mirror budget for " + mirrorSeries);
        return ;
      }
      values.safeApply(new FieldValues.Functor() {
        public void process(Field field, Object value) throws Exception {
          if (field.equals(SeriesBudget.ACTUAL_AMOUNT) || field.equals(SeriesBudget.SERIES)) {
            return;
          }
          if (field.equals(SeriesBudget.PLANNED_AMOUNT)) {
            localRepository.update(mirrorBudget.getKey(), SeriesBudget.PLANNED_AMOUNT, value == null ? null : -((Double)value));
          }
          else {
            localRepository.update(mirrorBudget.getKey(), field, value);
          }
        }
      });
    }
  }

  public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
  }
}
