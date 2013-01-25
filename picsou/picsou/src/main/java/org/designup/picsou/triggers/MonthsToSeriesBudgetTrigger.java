package org.designup.picsou.triggers;

import org.designup.picsou.model.*;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.collections.Pair;
import org.globsframework.utils.directory.Directory;

import java.util.*;

import static org.globsframework.model.FieldValue.value;
import static org.globsframework.model.utils.GlobMatchers.fieldIn;

public class MonthsToSeriesBudgetTrigger extends AbstractChangeSetListener {
  private Directory directory;

  public MonthsToSeriesBudgetTrigger(Directory directory) {
    this.directory = directory;
  }

  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    if (!changeSet.containsChanges(Month.TYPE) && !changeSet.containsChanges(CurrentMonth.TYPE)) {
      return;
    }
    Set<Key> deleted = changeSet.getDeleted(Month.TYPE);
    if (!deleted.isEmpty()) {
      Set<Integer> deletedId = new HashSet<Integer>();
      for (Key key : deleted) {
        deletedId.add(key.get(Month.ID));
      }
      repository.delete(SeriesBudget.TYPE, fieldIn(SeriesBudget.MONTH, deletedId));
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

    int currentMonth = repository.get(CurrentMonth.KEY).get(CurrentMonth.CURRENT_MONTH);
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

  public static void addMonth(GlobRepository repository, final Integer monthId) {
    GlobList seriesList = repository.getAll(Series.TYPE,
                                            GlobMatchers.and(
                                              GlobMatchers.or(
                                                GlobMatchers.isNull(Series.LAST_MONTH),
                                                GlobMatchers.fieldGreaterOrEqual(Series.LAST_MONTH, monthId)
                                              ),
                                              GlobMatchers.or(
                                                GlobMatchers.isNull(Series.FIRST_MONTH),
                                                GlobMatchers.fieldLessOrEqual(Series.FIRST_MONTH, monthId)
                                              )));
    for (Glob series : seriesList) {
      addMonthForSeries(repository, monthId, series);
    }
  }

  public static void addMonthForSeries(GlobRepository repository, Integer monthId, Glob series) {
    if (ProfileType.IRREGULAR.getId().equals(series.get(Series.PROFILE_TYPE))) {
      repository.create(SeriesBudget.TYPE,
                        value(SeriesBudget.ACTIVE, true),
                        value(SeriesBudget.SERIES, series.get(Series.ID)),
                        value(SeriesBudget.DAY, Month.getDay(series.get(Series.DAY), monthId)),
                        value(SeriesBudget.MONTH, monthId));
      return;
    }

    ReadOnlyGlobRepository.MultiFieldIndexed index = repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, series.get(Series.ID));
    // cas des SB crees par le trigger de creation de comptes a debit differé ==> c'est le trigger
    // qui cree les SB en parcourant les mois. il ne faut pas recreer le SB.
    if (!index.findByIndex(SeriesBudget.MONTH, monthId).getGlobs().isEmpty()) {
      return;
    }
    GlobList globs = index.getGlobs();
    Glob[] existingSeriesBudget = globs.sort(SeriesBudget.MONTH).toArray();

    Pair<Integer, Integer> startEndMonth = Account.getValidMonth(series, repository);

    Glob budget = repository.create(SeriesBudget.TYPE,
                                    value(SeriesBudget.ACTIVE, series.get(Series.getMonthField(monthId)) &&
                                                               monthId >= startEndMonth.getFirst() && monthId <= startEndMonth.getSecond()),
                                    value(SeriesBudget.SERIES, series.get(Series.ID)),
                                    value(SeriesBudget.DAY, Month.getDay(series.get(Series.DAY), monthId)),
                                    value(SeriesBudget.MONTH, monthId));

    // attention les creation de mois arrive dans un ordre aleatoire
    // attention aussi a la creation de mois en debut de periode et non en fin
    if (series.isTrue(Series.IS_AUTOMATIC)) {
      Double amount = null;
      int lastmonthId = 0;
      int nextMonth = Month.next(monthId);
      for (Glob seriesBudget : existingSeriesBudget) {
        if (seriesBudget.get(SeriesBudget.MONTH) < monthId
            && seriesBudget.isTrue(SeriesBudget.ACTIVE)
            && seriesBudget.get(SeriesBudget.MONTH) > lastmonthId) {
          amount = seriesBudget.get(SeriesBudget.PLANNED_AMOUNT);
          lastmonthId = seriesBudget.get(SeriesBudget.MONTH);
        }
        if (seriesBudget.get(SeriesBudget.MONTH) == nextMonth) {
          break;
        }
      }
      repository.update(budget.getKey(), SeriesBudget.PLANNED_AMOUNT, amount);
    }
    else {
      Double seriesAmount = null; // pour les tests de triggers

      if (existingSeriesBudget.length != 0) {
        int pos = existingSeriesBudget.length - 1;
        while (pos >= 0) {
          Glob budgets = existingSeriesBudget[pos];
          if (budgets.get(SeriesBudget.MONTH) < monthId) {
            break;
          }
          pos--;
        }
        while (pos >= 0) {
          Glob budgets = existingSeriesBudget[pos];
          if (budgets.isTrue(SeriesBudget.ACTIVE) && !budgets.get(SeriesBudget.MONTH).equals(monthId)) {
            seriesAmount = budgets.get(SeriesBudget.PLANNED_AMOUNT);
            break;
          }
          pos--;
        }
        if (seriesAmount == null) {
          pos = pos < 0 ? 0 : pos;
          while (pos < existingSeriesBudget.length - 1) {
            Glob budgets = existingSeriesBudget[pos];
            if (budgets.isTrue(SeriesBudget.ACTIVE) && !budgets.get(SeriesBudget.MONTH).equals(monthId)) {
              seriesAmount = budgets.get(SeriesBudget.PLANNED_AMOUNT);
              break;
            }
            pos++;
          }
        }
      }
      if (seriesAmount == null) {
        seriesAmount = series.get(Series.INITIAL_AMOUNT);
      }
      repository.update(budget.getKey(), SeriesBudget.PLANNED_AMOUNT, seriesAmount);
    }
  }
}
