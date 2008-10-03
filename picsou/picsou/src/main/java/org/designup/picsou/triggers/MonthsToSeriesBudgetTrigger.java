package org.designup.picsou.triggers;

import org.designup.picsou.gui.TimeService;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.ProfileType;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.SeriesBudget;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;

import java.util.*;

public class MonthsToSeriesBudgetTrigger implements ChangeSetListener {
  private Directory directory;

  public MonthsToSeriesBudgetTrigger(Directory directory) {
    this.directory = directory;
  }

  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    Set<Key> deleted = changeSet.getDeleted(Month.TYPE);
    if (!deleted.isEmpty()) {
      Set<Integer> deletedId = new HashSet<Integer>();
      for (Key key : deleted) {
        deletedId.add(key.get(Month.ID));
      }
      GlobList seriesBudgets = repository.getAll(SeriesBudget.TYPE,
                                                 GlobMatchers.fieldIn(SeriesBudget.MONTH, deletedId));
      repository.delete(seriesBudgets);
    }
    Set<Key> createdMonth = changeSet.getCreated(Month.TYPE);
    SortedSet<Key> rightMonth = new TreeSet<Key>(new Comparator<Key>() {
      public int compare(Key o1, Key o2) {
        return o1.get(Month.ID).compareTo(o2.get(Month.ID));
      }
    });
    SortedSet<Key> leftMonth = new TreeSet<Key>(new Comparator<Key>() {
      public int compare(Key o1, Key o2) {
        return o2.get(Month.ID).compareTo(o1.get(Month.ID));
      }
    });

    int currentMonth = directory.get(TimeService.class).getCurrentMonthId();
    for (Key key : createdMonth) {
      if (key.get(Month.ID) > currentMonth) {
        rightMonth.add(key);
      }
      else {
        leftMonth.add(key);
      }
    }
    for (Key key : leftMonth) {
      addMonth(repository, key.get(Month.ID));
    }
    for (Key key : rightMonth) {
      addMonth(repository, key.get(Month.ID));
    }
  }

  private void addMonth(GlobRepository repository, final Integer monthId) {
    GlobList seriesList = repository.getAll(Series.TYPE,
                                            GlobMatchers.and(
                                              GlobMatchers.or(
                                                GlobMatchers.fieldIsNull(Series.LAST_MONTH),
                                                GlobMatchers.fieldGreaterOrEqual(Series.LAST_MONTH, monthId)
                                              ),
                                              GlobMatchers.or(
                                                GlobMatchers.fieldIsNull(Series.FIRST_MONTH),
                                                GlobMatchers.fieldLesserOrEqual(Series.FIRST_MONTH, monthId)
                                              )));
    for (Glob series : seriesList) {
      Glob existingSeriesBudget[];
      if (ProfileType.IRREGULAR.getId().equals(series.get(Series.PROFILE_TYPE))) {
        existingSeriesBudget = new Glob[0];
      }
      else {
        GlobList globs = repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, series.get(Series.ID))
          .getGlobs();
        existingSeriesBudget = globs.sort(SeriesBudget.MONTH).toArray(new Glob[globs.size()]);
      }

      Glob budget = repository.create(SeriesBudget.TYPE,
                                      value(SeriesBudget.ACTIVE, series.get(Series.getField(monthId))),
                                      value(SeriesBudget.SERIES, series.get(Series.ID)),
                                      value(SeriesBudget.DAY, series.get(Series.DAY)),
                                      value(SeriesBudget.MONTH, monthId));
      if (series.get(Series.IS_AUTOMATIC)) {
        Double amount = 0.;
        int nextMonth = Month.next(monthId);
        int previousMonth = Month.previous(monthId);
        for (Glob glob : existingSeriesBudget) {
          if (glob.get(SeriesBudget.MONTH) == previousMonth && glob.get(SeriesBudget.ACTIVE)) {
            amount = glob.get(SeriesBudget.AMOUNT);
          }
          if (glob.get(SeriesBudget.MONTH) == nextMonth) {
            repository.update(glob.getKey(), SeriesBudget.AMOUNT, 0.);
            break;
          }
        }
        repository.update(budget.getKey(), SeriesBudget.AMOUNT, amount);
        continue;
      }
      Double seriesAmount = series.get(Series.INITIAL_AMOUNT);

      if (existingSeriesBudget.length != 0) {
        int pos = existingSeriesBudget.length - 1;
        while (pos >= 0) {
          Glob budgets = existingSeriesBudget[pos];
          seriesAmount = budgets.get(SeriesBudget.AMOUNT);
          if (budgets.get(SeriesBudget.MONTH) < monthId) {
            break;
          }
          pos--;
        }
      }
      if (seriesAmount != null) {
        repository.update(budget.getKey(), SeriesBudget.AMOUNT, seriesAmount);
      }
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
  }
}
