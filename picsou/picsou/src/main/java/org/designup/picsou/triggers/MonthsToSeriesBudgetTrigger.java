package org.designup.picsou.triggers;

import org.designup.picsou.model.Month;
import org.designup.picsou.model.ProfileType;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.SeriesBudget;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.utils.GlobMatchers;

import java.util.ListIterator;
import java.util.Set;

public class MonthsToSeriesBudgetTrigger implements ChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    changeSet.safeVisit(Month.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        Integer monthId = values.get(Month.ID);
        GlobList seriesList = repository.getAll(Series.TYPE,
                                                GlobMatchers.or(
                                                  GlobMatchers.fieldIsNull(Series.LAST_MONTH),
                                                  GlobMatchers.fieldGreaterOrEqual(Series.LAST_MONTH, monthId)
                                                ));
        for (Glob series : seriesList) {
          GlobList existingSeriesBudget;
          if (ProfileType.UNKNOWN.getId().equals(series.get(Series.PROFILE_TYPE))) {
            existingSeriesBudget = GlobList.EMPTY;
          }
          else {
            existingSeriesBudget = repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, series.get(Series.ID))
              .getGlobs().sort(SeriesBudget.MONTH);
          }

          Glob budget = repository.create(SeriesBudget.TYPE,
                                          value(SeriesBudget.ACTIVE, series.get(Series.getField(monthId))),
                                          value(SeriesBudget.SERIES, series.get(Series.ID)),
                                          value(SeriesBudget.DAY, series.get(Series.DAY)),
                                          value(SeriesBudget.MONTH, monthId));
          Double seriesAmount = series.get(Series.INITIAL_AMOUNT);

          if (existingSeriesBudget.size() != 0) {
            ListIterator<Glob> iterator = existingSeriesBudget.listIterator(existingSeriesBudget.size() - 1);
            while (iterator.hasPrevious()) {
              Glob budgets = iterator.previous();
              seriesAmount = budgets.get(SeriesBudget.AMOUNT);
              if (budgets.get(SeriesBudget.MONTH) < monthId) {
                break;
              }
            }
          }
          if (seriesAmount != null) {
            repository.update(budget.getKey(), SeriesBudget.AMOUNT, seriesAmount);
          }
        }
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        Integer deletedMonth = previousValues.get(Month.ID);
        repository.getAll(SeriesBudget.TYPE, GlobMatchers.fieldEquals(SeriesBudget.MONTH, deletedMonth));
      }
    });
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
  }
}
