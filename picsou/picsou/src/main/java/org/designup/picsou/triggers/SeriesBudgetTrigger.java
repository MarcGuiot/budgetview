package org.designup.picsou.triggers;

import org.designup.picsou.model.Month;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.SeriesBudget;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.model.*;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.utils.Utils;

import java.util.*;

public class SeriesBudgetTrigger implements ChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    changeSet.safeVisit(Series.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        updateSeriesBudget(repository.get(key), repository);
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        Glob series = repository.get(key);
        updateSeriesBudget(series, repository);
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        GlobList seriesToBudget = repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, key.get(Series.ID)).getGlobs();
        repository.delete(seriesToBudget);
      }
    });

    changeSet.safeVisit(SeriesBudget.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        if (values.contains(SeriesBudget.ACTIVE)) {
          if (!values.get(SeriesBudget.ACTIVE)) {
            repository.update(key, SeriesBudget.AMOUNT, 0.0);
          }
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
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
    Integer toDate = lastMonth == null ? monthIds[monthIds.length - 1] :
                     Math.min(lastMonth, monthIds[monthIds.length - 1]);
    if (toDate != null) {
      toIndex = Arrays.binarySearch(monthIds, toDate);
    }

    if (fromIndex >= 0 && toIndex >= 0) {
      Calendar calendar = Calendar.getInstance();
      for (int i = fromIndex; i <= toIndex; i++) {
        int monthId = monthIds[i];
        BooleanField monthField = Series.getField(monthId);
        Boolean active = series.get(monthField);
        Glob seriesBudget = monthWithBudget.remove(monthId);
        if (seriesBudget == null) {
          repository.create(SeriesBudget.TYPE,
                            value(SeriesBudget.SERIES, seriesId),
                            value(SeriesBudget.AMOUNT,
                                  active ? Utils.zeroIfNull(series.get(Series.INITIAL_AMOUNT)) : 0.0),
                            value(SeriesBudget.MONTH, monthId),
                            value(SeriesBudget.DAY, Month.getDay(series.get(Series.DAY), monthId, calendar)),
                            value(SeriesBudget.ACTIVE, active));
        }
        else {
          repository.update(seriesBudget.getKey(),
                            value(SeriesBudget.DAY, Month.getDay(series.get(Series.DAY), monthId, calendar)),
                            value(SeriesBudget.ACTIVE, active));
          if (!active) {
            repository.update(seriesBudget.getKey(), SeriesBudget.AMOUNT, 0.0);
          }
        }
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
