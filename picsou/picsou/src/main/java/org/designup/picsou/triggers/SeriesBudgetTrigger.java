package org.designup.picsou.triggers;

import org.designup.picsou.model.Month;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.SeriesBudget;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.model.*;
import static org.globsframework.model.FieldValue.value;

import java.util.*;

public class SeriesBudgetTrigger implements ChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    changeSet.safeVisit(Series.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        if (values.get(Series.INITIAL_AMOUNT) != null) {
          updateSeriesBudget(repository.get(key), repository);
        }
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        Glob series = repository.get(key);
        if (series.get(Series.INITIAL_AMOUNT) != null) {
          updateSeriesBudget(series, repository);
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        GlobList seriesToBudget = repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, key.get(Series.ID)).getGlobs();
        repository.delete(seriesToBudget);
      }
    });
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
  }

  void updateSeriesBudget(Glob series, GlobRepository repository) {
    Integer seriesId = series.get(Series.ID);
    Map<Integer, Glob> monthWithBudget =
      toMap(repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, seriesId).getGlobs(),
            SeriesBudget.MONTH);

    Integer[] monthIds = repository.getAll(Month.TYPE).getSortedArray(Month.ID);
    if (monthIds.length == 0) {
      return;
    }
    int fromIndex = 0;
    Integer firstMonth = series.get(Series.FIRST_MONTH);
    Integer fromDate = firstMonth == null ? monthIds[0] : Math.max(firstMonth, monthIds[0]);
    if (fromDate != null) {
      fromIndex = Arrays.binarySearch(monthIds, fromDate);
    }
    int toIndex = monthIds.length - 1;
    Integer lastMonth = series.get(Series.LAST_MONTH);
    Integer toDate = lastMonth == null ? monthIds[monthIds.length - 1] : Math.min(lastMonth, monthIds[monthIds.length - 1]);
    if (toDate == null) {
      toIndex = Arrays.binarySearch(monthIds, toDate);
    }

    Calendar calendar = Calendar.getInstance();
    for (int i = fromIndex; i <= toIndex; i++) {
      int monthId = monthIds[i];
      BooleanField monthField = Series.getField(monthId);
      Boolean active = series.get(monthField);
      Glob seriesBudget = monthWithBudget.remove(monthId);
      if (seriesBudget == null) {
        FieldValue fieldValues[] = {value(SeriesBudget.SERIES, seriesId),
                                    value(SeriesBudget.AMOUNT, series.get(Series.INITIAL_AMOUNT)),
                                    value(SeriesBudget.MONTH, monthId),
                                    value(SeriesBudget.DAY, Month.getDay(series.get(Series.DAY), monthId, calendar)),
                                    value(SeriesBudget.ACTIVE, active)};
        repository.create(SeriesBudget.TYPE, fieldValues);
      }
      else {
        FieldValue fieldValues[] = {value(SeriesBudget.DAY, Month.getDay(series.get(Series.DAY), monthId, calendar)),
                                    value(SeriesBudget.ACTIVE, active)};
        repository.update(seriesBudget.getKey(), fieldValues);
      }
    }
    for (Glob seriesBudget : monthWithBudget.values()) {
      repository.delete(seriesBudget.getKey());
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
