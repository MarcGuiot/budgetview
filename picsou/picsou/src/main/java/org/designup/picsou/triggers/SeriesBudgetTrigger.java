package org.designup.picsou.triggers;

import org.designup.picsou.model.*;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.model.*;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.utils.Utils;

import java.util.*;

public class SeriesBudgetTrigger implements ChangeSetListener {
  private GlobRepository parentRepository;

  public SeriesBudgetTrigger(GlobRepository parentRepository) {
    this.parentRepository = parentRepository;
  }

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
        GlobList seriesToBudget =
          repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, key.get(Series.ID)).getGlobs();
        repository.delete(seriesToBudget);
      }
    });

    changeSet.safeVisit(SeriesBudget.TYPE, new ChangeSetVisitor() {
      Set<Key> updatedSeries = new HashSet<Key>();

      public void visitCreation(Key key, FieldValues values) throws Exception {
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        if (values.contains(SeriesBudget.ACTIVE)) {
          if (!values.isTrue(SeriesBudget.ACTIVE)) {
            repository.update(key, SeriesBudget.AMOUNT, 0.0);
          }
        }
        Glob seriesBudget = repository.get(key);
        Glob series = repository.findLinkTarget(seriesBudget, SeriesBudget.SERIES);
        if (series == null) {
          return;
        }
        if (series.isTrue(Series.IS_AUTOMATIC) && !series.get(Series.PROFILE_TYPE).equals(ProfileType.IRREGULAR.getId())) {
          if (!updatedSeries.contains(series.getKey())) {
            AutomaticSeriesBudgetTrigger.updateSeriesBudget(series.getKey(), repository);
            updatedSeries.add(series.getKey());
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
      Calendar calendar = Calendar.getInstance();
      for (int i = fromIndex; i <= toIndex; i++) {
        int monthId = monthIds[i];
        boolean active = series.isTrue(Series.getMonthField(monthId));
        Glob seriesBudget = monthWithBudget.remove(monthId);
        if (seriesBudget == null) {
          Glob existingSeriesBudget =
            parentRepository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, seriesId)
              .findByIndex(SeriesBudget.MONTH, monthId).getGlobs().getFirst();

          FieldValue[] values = new FieldValue[]{
            value(SeriesBudget.SERIES, seriesId),
            value(SeriesBudget.AMOUNT, getInitialAmount(series, active)),
            value(SeriesBudget.MONTH, monthId),
            value(SeriesBudget.DAY, Month.getDay(series.get(Series.DAY), monthId, calendar)),
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
            if (seriesBudget.get(SeriesBudget.OBSERVED_AMOUNT) != null
                && seriesBudget.get(SeriesBudget.MONTH) <= currentMonth.get(CurrentMonth.LAST_TRANSACTION_MONTH)) {
              active = true;
            }
          }
          repository.update(seriesBudget.getKey(),
                            value(SeriesBudget.DAY, Month.getDay(series.get(Series.DAY), monthId, calendar)),
                            value(SeriesBudget.ACTIVE, active));
          if ((series.get(Series.PROFILE_TYPE).equals(ProfileType.IRREGULAR.getId()) &&
               seriesBudget.get(SeriesBudget.MONTH) > currentMonth.get(CurrentMonth.LAST_TRANSACTION_MONTH))) {
            repository.update(seriesBudget.getKey(), SeriesBudget.AMOUNT, 0.0);
          }
        }
      }
    }
    for (Glob seriesBudget : monthWithBudget.values()) {
      repository.delete(seriesBudget.getKey());
    }

    if (series.isTrue(Series.IS_AUTOMATIC)) {
      AutomaticSeriesBudgetTrigger.updateSeriesBudget(series.getKey(), repository);
    }
  }

  private double getInitialAmount(Glob series, Boolean active) {
    if (active && !series.get(Series.PROFILE_TYPE).equals(ProfileType.IRREGULAR.getId())) {
      return Utils.zeroIfNull(series.get(Series.INITIAL_AMOUNT));
    }
    else {
      return 0.0;
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
