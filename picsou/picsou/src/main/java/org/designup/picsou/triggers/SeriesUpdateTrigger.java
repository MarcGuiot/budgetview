package org.designup.picsou.triggers;

import org.designup.picsou.gui.TimeService;
import org.designup.picsou.model.*;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.model.*;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.utils.GlobMatchers;
import static org.globsframework.model.utils.GlobMatchers.fieldEquals;
import org.globsframework.utils.MultiMap;
import org.globsframework.utils.directory.Directory;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SeriesUpdateTrigger implements ChangeSetListener {
  private TimeService time;

  public SeriesUpdateTrigger(Directory directory) {
    time = directory.get(TimeService.class);
  }

  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    repository.enterBulkDispatchingMode();
    try {
      changeSet.safeVisit(Series.TYPE, new ChangeSetVisitor() {
        public void visitCreation(Key key, FieldValues values) throws Exception {
          updateBudget(repository.get(key), repository);
        }

        public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
          updateBudget(repository.get(key), repository);
        }

        public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
          updateSeriesDependanciesOnDelete(key, repository);
        }
      });
      if (changeSet.containsCreationsOrDeletions(Month.TYPE)) {
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
    int currentMonthId = time.getCurrentMonthId();
    Integer seriesId = series.get(Series.ID);
    Map<Integer, Glob> monthWithBudget =
      toMap(repository.findByIndex(SeriesBudget.SERIES_INDEX, seriesId), SeriesBudget.MONTH);

    Integer[] monthIds = repository.getAll(Month.TYPE, GlobMatchers.fieldGreaterOrEqual(Month.ID, currentMonthId))
      .getSortedArray(Month.ID);

    if (series.get(Series.AMOUNT) != null) {
      Calendar calendar = Calendar.getInstance();
      for (int monthId : monthIds) {
        BooleanField monthField = Series.getField(monthId);
        if (series.get(monthField)) {
          if (!monthWithBudget.containsKey(monthId)) {
            repository.create(SeriesBudget.TYPE,
                              value(SeriesBudget.SERIES, seriesId),
                              value(SeriesBudget.AMOUNT, series.get(Series.AMOUNT)),
                              value(SeriesBudget.MONTH, monthId));
          }
          GlobList transactions = repository.findByIndex(Transaction.MONTH_INDEX, monthId)
            .filterSelf(GlobMatchers.and(
              fieldEquals(Transaction.PLANNED, true),
              fieldEquals(Transaction.SERIES, seriesId)),
                        repository);
          if (transactions.isEmpty()) {
            createPlannedTransaction(series, repository, monthId, getDays(series, monthId, calendar));
          }
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
                      value(Transaction.AMOUNT, -series.get(Series.AMOUNT)),
                      value(Transaction.SERIES, series.get(Series.ID)),
                      value(Transaction.BANK_MONTH, monthId),
                      value(Transaction.BANK_DAY, day),
                      value(Transaction.MONTH, monthId),
                      value(Transaction.DAY, day),
                      value(Transaction.LABEL, series.get(Series.LABEL)),
                      value(Transaction.PLANNED, true),
                      value(Transaction.TRANSACTION_TYPE, TransactionType.PLANNED.getId()),
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
    GlobList seriesList = repository.getAll(Series.TYPE);
    for (Glob series : seriesList) {
      updateBudget(series, repository);
    }
  }
}
