package org.designup.picsou.triggers;

import org.designup.picsou.model.CurrentMonth;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.SeriesBudget;
import com.budgetview.shared.utils.Amounts;
import org.globsframework.model.*;
import org.globsframework.utils.Utils;

import static org.globsframework.model.FieldValue.value;

public class AutomaticSeriesBudgetTrigger extends AbstractChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {

    changeSet.safeVisit(Series.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        if (values.contains(Series.IS_AUTOMATIC) && values.isTrue(Series.IS_AUTOMATIC)) {
          updateSeriesBudget(key, repository);
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
      }
    });
  }

  public static void updateSeriesBudget(Key seriesKey, GlobRepository repository) {
    final Glob currentMonth = repository.get(CurrentMonth.KEY);
    Integer seriesId = seriesKey.get(Series.ID);
    GlobList seriesBudgets =
      repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, seriesId)
        .getGlobs().sort(SeriesBudget.MONTH);
    Double previousAmount = 0.00;
    boolean firstUpdate = true;
    for (Glob seriesBudget : seriesBudgets) {
      if (!seriesBudget.isTrue(SeriesBudget.ACTIVE)) {
        repository.update(seriesBudget.getKey(),
                          value(SeriesBudget.PLANNED_AMOUNT, 0.00));
      }
      else {
        repository.update(seriesBudget.getKey(),
                          value(SeriesBudget.PLANNED_AMOUNT, Utils.zeroIfNull(previousAmount)));
        Double currentAmount = previousAmount;
        if (seriesBudget.get(SeriesBudget.MONTH) <= currentMonth.get(CurrentMonth.LAST_TRANSACTION_MONTH)) {
          previousAmount = seriesBudget.get(SeriesBudget.ACTUAL_AMOUNT);
          if (previousAmount == null) {
            previousAmount = 0.00;
          }
        }
        if (seriesBudget.get(SeriesBudget.MONTH).equals(currentMonth.get(CurrentMonth.LAST_TRANSACTION_MONTH))) {
          if (currentAmount > 0) {
            if (currentAmount > previousAmount) {
              previousAmount = currentAmount;
            }
          }
          else if (currentAmount < 0) {
            if (currentAmount < previousAmount) {
              previousAmount = currentAmount;
            }
          }
        }
        if (firstUpdate && Amounts.isNotZero(previousAmount)) {
          repository.update(seriesBudget.getKey(),
                            value(SeriesBudget.PLANNED_AMOUNT, Utils.zeroIfNull(previousAmount)));
          firstUpdate = false;
        }
      }
    }
  }
}
