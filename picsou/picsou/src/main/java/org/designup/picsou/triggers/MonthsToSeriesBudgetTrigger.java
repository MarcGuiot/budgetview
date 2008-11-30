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
      if (ProfileType.IRREGULAR.getId().equals(series.get(Series.PROFILE_TYPE))) {
        repository.create(SeriesBudget.TYPE,
                          value(SeriesBudget.AMOUNT, 0.),
                          value(SeriesBudget.ACTIVE, true),
                          value(SeriesBudget.SERIES, series.get(Series.ID)),
                          value(SeriesBudget.DAY, series.get(Series.DAY)),
                          value(SeriesBudget.MONTH, monthId));
      }
      else {
        GlobList globs = repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, series.get(Series.ID))
          .getGlobs();
        Glob[] existingSeriesBudget = globs.sort(SeriesBudget.MONTH).toArray(new Glob[globs.size()]);

        Glob budget = repository.create(SeriesBudget.TYPE,
                                        value(SeriesBudget.ACTIVE, series.get(Series.getMonthField(monthId))),
                                        value(SeriesBudget.SERIES, series.get(Series.ID)),
                                        value(SeriesBudget.DAY, series.get(Series.DAY)),
                                        value(SeriesBudget.MONTH, monthId));

        // attention les creation de mois arrive dans un ordre aleatoire
        // attention aussi a la creation de mois en debut de periode et non en fin
        if (series.get(Series.IS_AUTOMATIC)) {
          Double amount = 0.;
          int lastmonthId = 0;
          int nextMonth = Month.next(monthId);
          for (Glob glob : existingSeriesBudget) {
            if (glob.get(SeriesBudget.MONTH) < monthId
                && glob.get(SeriesBudget.ACTIVE)
                && glob.get(SeriesBudget.MONTH) > lastmonthId) {
              amount = glob.get(SeriesBudget.AMOUNT);
              lastmonthId = glob.get(SeriesBudget.MONTH);
            }
            if (glob.get(SeriesBudget.MONTH) == nextMonth) {
//              ???
//              repository.update(glob.getKey(), SeriesBudget.AMOUNT, 0.);
              break;
            }
          }
          repository.update(budget.getKey(), SeriesBudget.AMOUNT, amount);
        }
        else {
          Double seriesAmount = null; // pour les tests de triggers

          if (existingSeriesBudget.length != 0) {
            int pos = existingSeriesBudget.length - 1;
            while (pos >= 0) {
              Glob budgets = existingSeriesBudget[pos];
              pos--;
              if (budgets.get(SeriesBudget.MONTH).equals(monthId)) {
                break;
              }
            }
            while (pos >= 0) {
              Glob budgets = existingSeriesBudget[pos];
              if (budgets.get(SeriesBudget.ACTIVE) && !budgets.get(SeriesBudget.MONTH).equals(monthId)) {
                seriesAmount = budgets.get(SeriesBudget.AMOUNT);
                break;
              }
            }
            if (seriesAmount == null) {
              pos = pos < 0 ? 0 : pos;
              while (pos < existingSeriesBudget.length - 1) {
                Glob budgets = existingSeriesBudget[pos];
                if (budgets.get(SeriesBudget.ACTIVE) && !budgets.get(SeriesBudget.MONTH).equals(monthId)) {
                  seriesAmount = budgets.get(SeriesBudget.AMOUNT);
                  break;
                }
                pos++;
              }
            }
          }
          if (seriesAmount == null) {
            seriesAmount = series.get(Series.INITIAL_AMOUNT);
          }
          if (seriesAmount != null) {
            repository.update(budget.getKey(), SeriesBudget.AMOUNT, seriesAmount);
          }
        }
      }
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
  }
}
