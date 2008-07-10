package org.designup.picsou.triggers;

import org.designup.picsou.gui.TimeService;
import org.designup.picsou.model.*;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.model.*;
import static org.globsframework.model.FieldValue.value;
import static org.globsframework.model.utils.GlobMatchers.fieldEquals;
import org.globsframework.utils.MultiMap;
import org.globsframework.utils.directory.Directory;

import java.util.*;

public class SeriesUpdateTrigger implements ChangeSetListener {
  private TimeService time;

  public SeriesUpdateTrigger(Directory directory) {
    time = directory.get(TimeService.class);
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    repository.enterBulkDispatchingMode();
    try {
      if (changeSet.containsCreationsOrDeletions(Series.TYPE)) {
        Set<Key> createdSeries = changeSet.getCreated(Series.TYPE);
        for (Key series : createdSeries) {
          updateBudget(repository.get(series), repository);
        }
        Set<Key> deletedSeries = changeSet.getDeleted(Series.TYPE);
        for (Key series : deletedSeries) {
          updateSeriesDependanciesOnDelete(series, repository);
        }
      }
      if (changeSet.containsChanges(UserPreferences.TYPE)) {
        GlobList seriesList = repository.getAll(Series.TYPE);
        for (Glob series : seriesList) {
          updateBudget(series, repository);
        }
      }
    }
    finally {
      repository.completeBulkDispatchingMode();
    }
  }

  private void updateSeriesDependanciesOnDelete(Key series, GlobRepository repository) {
    Integer seriesId = series.get(Series.ID);
    GlobList seriesBudget = repository.findByIndex(SeriesBudget.SERIES_INDEX, seriesId);
    repository.delete(seriesBudget);
    GlobList transactions = repository.getAll(Transaction.TYPE, fieldEquals(Transaction.SERIES, seriesId));
    for (Glob transaction : transactions) {
      if (transaction.get(Transaction.PLANNED)) {
        repository.delete(transaction.getKey());
      }
      else {
        repository.update(transaction.getKey(), Transaction.SERIES, null);
      }
    }
  }

  private void updateBudget(Glob series, GlobRepository repository) {
    Glob userPreferences = repository.get(Key.create(UserPreferences.TYPE, UserPreferences.SINGLETON_ID));
    int currentMonthId = time.getCurrentMonthId();
    Map<Integer, Glob> monthWithBudget =
      toMap(repository.findByIndex(SeriesBudget.SERIES_INDEX, series.get(Series.ID)), SeriesBudget.MONTH);

    MultiMap<Integer, Glob> monthWithTransactions =
      toMultiMap(repository.findByIndex(SeriesBudget.SERIES_INDEX, series.get(Series.ID)), SeriesBudget.MONTH);

    int monthIds[] = Month.createMonthsWithFirst(currentMonthId, userPreferences.get(UserPreferences.FUTURE_MONTH_COUNT));
    Calendar calendar = Calendar.getInstance();
    for (int monthId : monthIds) {
      BooleanField monthField = Series.getField(monthId);
      if (series.get(monthField)) {
        if (!monthWithBudget.containsKey(monthId)) {
          repository.create(SeriesBudget.TYPE,
                            value(SeriesBudget.SERIES, series.get(Series.ID)),
                            value(SeriesBudget.MONTH, monthId));
        }
        if (!monthWithTransactions.containsKey(monthId)) {
          createPlannedTransaction(series, repository, monthId, getDays(series, monthId, calendar));
        }
      }
    }
    int lastMonth = monthIds[monthIds.length - 1];
    deleteFutureBudgets(repository, monthWithBudget, lastMonth);
    deleteFutureTransactions(series, repository, lastMonth);
  }

  private void createPlannedTransaction(Glob series, GlobRepository repository, int monthId, Integer day) {
    repository.create(Transaction.TYPE,
                      value(Transaction.ACCOUNT, Account.SUMMARY_ACCOUNT_ID),
                      value(Transaction.AMOUNT, series.get(Series.AMOUNT)),
                      value(Transaction.SERIES, series.get(Series.ID)),
                      value(Transaction.BANK_MONTH, monthId),
                      value(Transaction.BANK_DAY, day),
                      value(Transaction.MONTH, monthId),
                      value(Transaction.DAY, day),
                      value(Transaction.LABEL, series.get(Series.LABEL)),
                      value(Transaction.PLANNED, true),
                      value(Transaction.CATEGORY, series.get(Series.DEFAULT_CATEGORY)));
  }

  private Integer getDays(Glob series, int monthId, Calendar calendar) {
    Integer day = series.get(Series.DAY);
    calendar.setTime(Month.toDate(monthId, 1));
    int lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    if (day == null || day < 0 || day > lastDay) {
      return lastDay;
    }
    return day;
  }

  private void deleteFutureBudgets(GlobRepository repository, Map<Integer, Glob> monthWithBudget, int lastMonth) {
    for (Glob budget : monthWithBudget.values()) {
      if (budget.get(SeriesBudget.MONTH) > lastMonth) {
        repository.delete(budget.getKey());
      }
    }
  }

  private void deleteFutureTransactions(Glob series, GlobRepository repository, int lastMonth) {
    GlobList transactions =
      repository.getAll(Transaction.TYPE, fieldEquals(Transaction.SERIES, series.get(Series.ID)))
        .sort(Transaction.MONTH);
    for (Glob transaction : transactions) {
      if (transaction.get(Transaction.MONTH) > lastMonth && transaction.get(Transaction.PLANNED)) {
        repository.delete(transaction.getKey());
      }
    }
  }

  private Map<Integer, Glob> toMap(GlobList globs, IntegerField field) {
    Map<Integer, Glob> globMap = new HashMap<Integer, Glob>();
    for (Glob glob : globs) {
      globMap.put(glob.get(field), glob);
    }
    return globMap;
  }

  private MultiMap<Integer, Glob> toMultiMap(GlobList globs, IntegerField field) {
    MultiMap<Integer, Glob> globMap = new MultiMap<Integer, Glob>();
    for (Glob glob : globs) {
      globMap.put(glob.get(field), glob);
    }
    return globMap;
  }


  public void globsReset(GlobRepository repository, List<GlobType> changedTypes) {
  }
}
