package com.budgetview.triggers;

import com.budgetview.model.*;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.model.*;
import org.globsframework.utils.collections.Pair;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.globsframework.model.FieldValue.value;

public class SeriesBudgetTrigger extends AbstractChangeSetListener {
  private GlobRepository parentRepository;

  public SeriesBudgetTrigger(GlobRepository parentRepository) {
    this.parentRepository = parentRepository;
  }

  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    changeSet.safeVisit(Series.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        try {
          repository.startChangeSet();
          updateSeriesBudget(repository.get(key), repository);
        }
        finally {
          repository.completeChangeSet();
        }
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        Glob series = repository.get(key);
        try {
          repository.startChangeSet();
          updateSeriesBudget(series, repository);
        }
        finally {
          repository.completeChangeSet();
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        GlobList seriesToBudget =
          repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, key.get(Series.ID)).getGlobs();
        repository.delete(seriesToBudget);
      }
    });
  }

  public void updateSeriesBudget(Glob series, GlobRepository repository) {
    Integer seriesId = series.get(Series.ID);
    Map<Integer, Glob> monthWithBudget =
      toMap(repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, seriesId).getGlobs(),
            SeriesBudget.MONTH);

    Integer[] monthIds = repository.getAll(Month.TYPE).getSortedArray(Month.ID);
    if (monthIds.length == 0) {
      return;
    }

    Pair<Integer, Integer> startEndValidMonth = Account.getValidMonth(series, repository);
    int fromIndex;
    Integer firstMonth = series.get(Series.FIRST_MONTH);
    Integer fromDate = firstMonth == null ? monthIds[0] : Math.max(firstMonth, monthIds[0]);
    fromIndex = Arrays.binarySearch(monthIds, fromDate);
    int toIndex;
    Integer lastMonth = series.get(Series.LAST_MONTH);
    Integer toDate = lastMonth == null ?
                     monthIds[monthIds.length - 1] :
                     Math.min(lastMonth, monthIds[monthIds.length - 1]);
    toIndex = Arrays.binarySearch(monthIds, toDate);
    Glob currentMonth = repository.get(CurrentMonth.KEY);
    if (fromIndex >= 0 && toIndex >= 0) {
      for (int i = fromIndex; i <= toIndex; i++) {
        int monthId = monthIds[i];
        boolean active = series.isTrue(Series.ACTIVE) &&
                         series.isTrue(Series.getMonthField(monthId)) &&
                         (monthId >= startEndValidMonth.getFirst() &&
                          monthId <= startEndValidMonth.getSecond());
        Glob seriesBudget = monthWithBudget.remove(monthId);
        if (seriesBudget == null) {
          Glob existingSeriesBudget =
            parentRepository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, seriesId)
              .findByIndex(SeriesBudget.MONTH, monthId).getGlobs().getFirst();

          FieldValue[] values = new FieldValue[]{
            value(SeriesBudget.SERIES, seriesId),
            value(SeriesBudget.PLANNED_AMOUNT, getInitialAmount(series, active)),
            value(SeriesBudget.MONTH, monthId),
            value(SeriesBudget.DAY, Month.getDay(series.get(Series.DAY), monthId)),
            value(SeriesBudget.ACTIVE, active)};
          if (existingSeriesBudget != null) {
            repository.create(existingSeriesBudget.getKey(), values);
          }
          else {
            repository.create(SeriesBudget.TYPE, values);
          }
        }
        else {
          if (!active) {
            if (seriesBudget.get(SeriesBudget.ACTUAL_AMOUNT) != null
                && seriesBudget.get(SeriesBudget.MONTH) <= currentMonth.get(CurrentMonth.LAST_TRANSACTION_MONTH)) {
              active = true;
            }
          }
          repository.update(seriesBudget.getKey(),
                            value(SeriesBudget.DAY, Month.getDay(series.get(Series.DAY), monthId)),
                            value(SeriesBudget.ACTIVE, active));
        }
      }
    }
    for (Glob seriesBudget : monthWithBudget.values()) {
      repository.delete(seriesBudget);
    }

    if (series.isTrue(Series.IS_AUTOMATIC)) {
      AutomaticSeriesBudgetTrigger.updateSeriesBudget(series.getKey(), repository);
    }
  }

  private Double getInitialAmount(Glob series, Boolean active) {
    if (active && !series.get(Series.PROFILE_TYPE).equals(ProfileType.IRREGULAR.getId())) {
      return series.get(Series.INITIAL_AMOUNT);
    }
    else {
      return null;
    }
  }

  private Map<Integer, Glob> toMap(GlobList globs, IntegerField field) {
    Map<Integer, Glob> globMap = new HashMap<Integer, Glob>();
    for (Glob glob : globs) {
      globMap.put(glob.get(field), glob);
    }
    return globMap;
  }
}
