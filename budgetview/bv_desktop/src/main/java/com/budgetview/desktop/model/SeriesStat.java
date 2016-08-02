package com.budgetview.desktop.model;

import com.budgetview.model.*;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.DefaultDouble;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.index.NotUniqueIndex;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.FieldValues;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Utils;
import org.globsframework.utils.exceptions.InvalidParameter;
import org.globsframework.utils.exceptions.UnexpectedValue;

import java.util.HashSet;
import java.util.Set;

import static org.globsframework.model.utils.GlobMatchers.*;
import static org.globsframework.utils.Utils.equal;

public class SeriesStat {
  public static GlobType TYPE;

  @Key
  public static IntegerField TARGET;

  @Key
  @Target(SeriesType.class)
  public static LinkField TARGET_TYPE;

  @Key
  @Target(Account.class)
  public static LinkField ACCOUNT;

  @Key
  @Target(Month.class)
  public static LinkField MONTH;

  @DefaultDouble(0.0)
  public static DoubleField ACTUAL_AMOUNT;

  @DefaultDouble(0.0)
  public static DoubleField PLANNED_AMOUNT;

  @DefaultDouble(0.0)
  public static DoubleField REMAINING_AMOUNT;

  @DefaultDouble(0.0)
  public static DoubleField OVERRUN_AMOUNT;

  public static DoubleField SUMMARY_AMOUNT;

  public static BooleanField ACTIVE;

  public static NotUniqueIndex MONTH_INDEX;
  public static NotUniqueIndex SERIES_INDEX;

  static {
    GlobTypeLoader loader = GlobTypeLoader.init(SeriesStat.class);
    loader.defineNonUniqueIndex(MONTH_INDEX, MONTH);
    loader.defineNonUniqueIndex(SERIES_INDEX, TARGET);
  }

  public static BudgetArea getBudgetArea(org.globsframework.model.Key seriesStatKey, GlobRepository repository) {
    Integer targetType = seriesStatKey.get(TARGET_TYPE);
    if (SeriesType.SERIES.getId().equals(targetType)) {
      Glob series = repository.get(org.globsframework.model.Key.create(Series.TYPE, seriesStatKey.get(TARGET)));
      return BudgetArea.get(series.get(Series.BUDGET_AREA));
    }
    if (SeriesType.SERIES_GROUP.getId().equals(targetType)) {
      Glob series = repository.get(org.globsframework.model.Key.create(SeriesGroup.TYPE, seriesStatKey.get(TARGET)));
      return BudgetArea.get(series.get(SeriesGroup.BUDGET_AREA));
    }
    throw new UnexpectedValue("Unexpected seriesStat type " + targetType + " for: " + seriesStatKey);
  }

  public static org.globsframework.model.Key createKeyForSeries(Integer accountId, Integer seriesId, Integer monthId) {
    return org.globsframework.model.Key.create(ACCOUNT, accountId,
                                               TARGET, seriesId,
                                               TARGET_TYPE, SeriesType.SERIES.getId(),
                                               MONTH, monthId);
  }

  public static org.globsframework.model.Key createKeyForSeries(Integer seriesId, Integer monthId) {
    return org.globsframework.model.Key.create(ACCOUNT, Account.ALL_SUMMARY_ACCOUNT_ID,
                                               TARGET, seriesId,
                                               TARGET_TYPE, SeriesType.SERIES.getId(),
                                               MONTH, monthId);
  }

  public static Glob findOrCreateForSeries(Integer seriesId, Integer monthId, GlobRepository repository) {
    org.globsframework.model.Key key = createKeyForSeries(seriesId, monthId);
    Glob stat = repository.find(key);
    return stat != null ? stat : repository.create(key);
  }

  public static boolean isSummaryForSeries(FieldValues seriesStat) {
    return isSummary(seriesStat) && SeriesType.SERIES.getId().equals(seriesStat.get(TARGET_TYPE));
  }

  public static boolean isSummaryForGroup(FieldValues seriesStat) {
    return isSummary(seriesStat) && SeriesType.SERIES_GROUP.getId().equals(seriesStat.get(TARGET_TYPE));
  }

  public static org.globsframework.model.Key keyForGroupSummary(Integer groupId, Integer monthId) {
    return org.globsframework.model.Key.create(ACCOUNT, Account.ALL_SUMMARY_ACCOUNT_ID,
                                               TARGET, groupId,
                                               TARGET_TYPE, SeriesType.SERIES_GROUP.getId(),
                                               MONTH, monthId);
  }

  public static Glob findSeries(FieldValues seriesStatValues, GlobRepository repository) {
    if (!SeriesType.SERIES.getId().equals(seriesStatValues.get(TARGET_TYPE))) {
      throw new InvalidParameter("Unexpected type for: " + seriesStatValues);
    }
    return repository.find(org.globsframework.model.Key.create(Series.TYPE, seriesStatValues.get(TARGET)));
  }

  public static Glob getSeries(FieldValues seriesStatValues, GlobRepository repository) {
    if (!SeriesType.SERIES.getId().equals(seriesStatValues.get(TARGET_TYPE))) {
      throw new InvalidParameter("Unexpected type for: " + seriesStatValues);
    }
    return repository.get(org.globsframework.model.Key.create(Series.TYPE, seriesStatValues.get(TARGET)));
  }

  public static Glob findSummaryForSeries(Integer seriesId, Integer monthId, GlobRepository repository) {
    return repository.find(org.globsframework.model.Key.create(ACCOUNT, Account.ALL_SUMMARY_ACCOUNT_ID,
                                                               TARGET, seriesId,
                                                               TARGET_TYPE, SeriesType.SERIES.getId(),
                                                               MONTH, monthId));
  }

  public static GlobMatcher isSummary() {
    return new GlobMatcher() {
      public boolean matches(Glob seriesStat, GlobRepository repository) {
        return seriesStat != null && isSummary(seriesStat);
      }
    };
  }

  public static GlobMatcher isForAccount(final int accountId) {
    return new GlobMatcher() {
      public boolean matches(Glob seriesStat, GlobRepository repository) {
        return seriesStat != null && equal(accountId, seriesStat.get(SeriesStat.ACCOUNT));
      }
    };
  }

  public static GlobMatcher isSeriesForAccount(final int accountId) {
    return new GlobMatcher() {
      public boolean matches(Glob seriesStat, GlobRepository repository) {
        return seriesStat != null && equal(accountId, seriesStat.get(SeriesStat.ACCOUNT)) && SeriesType.SERIES.getId().equals(seriesStat.get(TARGET_TYPE));
      }
    };
  }

  public static GlobMatcher isSummaryForSeries() {
    return new GlobMatcher() {
      public boolean matches(Glob seriesStat, GlobRepository repository) {
        return seriesStat != null && isSummary(seriesStat) && SeriesType.SERIES.getId().equals(seriesStat.get(TARGET_TYPE));
      }
    };
  }

  public static GlobMatcher isSummaryForGroup() {
    return new GlobMatcher() {
      public boolean matches(Glob seriesStat, GlobRepository repository) {
        return seriesStat != null && isSummary(seriesStat) && SeriesType.SERIES_GROUP.getId().equals(seriesStat.get(TARGET_TYPE));
      }
    };
  }

  public static GlobMatcher isSeriesInGroup(final Integer groupId) {
    return new GlobMatcher() {
      public boolean matches(Glob seriesStat, GlobRepository repository) {
        if ((seriesStat == null) || !SeriesType.SERIES.getId().equals(seriesStat.get(TARGET_TYPE))) {
          return false;
        }
        Glob series = repository.find(org.globsframework.model.Key.create(Series.TYPE, seriesStat.get(SeriesStat.TARGET)));
        return series != null && Utils.equal(groupId, series.get(Series.GROUP));
      }
    };
  }

  public static GlobMatcher isRoot() {
    return new GlobMatcher() {
      public boolean matches(Glob seriesStat, GlobRepository repository) {
        if (seriesStat == null || !isSummary(seriesStat)) {
          return false;
        }
        if (SeriesType.SERIES_GROUP.getId().equals(seriesStat.get(TARGET_TYPE))) {
          return true;
        }
        Glob series = repository.find(org.globsframework.model.Key.create(Series.TYPE, seriesStat.get(TARGET)));
        return series != null  && series.get(Series.GROUP) == null;
      }
    };
  }

  public static boolean isSummary(FieldValues seriesStat) {
    return equal(Account.ALL_SUMMARY_ACCOUNT_ID, seriesStat.get(SeriesStat.ACCOUNT));
  }

  public static GlobList getAllSummarySeriesForMonth(Integer monthId, GlobRepository repository) {
    return repository.findByIndex(SeriesStat.MONTH_INDEX, monthId)
      .filter(GlobMatchers.and(fieldEquals(SeriesStat.ACCOUNT, Account.ALL_SUMMARY_ACCOUNT_ID),
                               fieldEquals(SeriesStat.TARGET_TYPE, SeriesType.SERIES.getId())),
              repository);
  }

  public static GlobMatcher linkedToSeries(final org.globsframework.model.Key seriesKey) {
    return new GlobMatcher() {
      public boolean matches(Glob seriesStat, GlobRepository repository) {
        return (seriesStat != null) &&
               equal(seriesStat.get(SeriesStat.TARGET_TYPE), SeriesType.SERIES.getId()) &&
               equal(seriesStat.get(SeriesStat.TARGET), seriesKey.get(Series.ID));
      }
    };
  }

  public static GlobMatcher summariesLinkedToSeries(final org.globsframework.model.Key seriesKey) {
    return new GlobMatcher() {
      public boolean matches(Glob seriesStat, GlobRepository repository) {
        return (seriesStat != null) &&
               isSummary(seriesStat) &&
               equal(seriesStat.get(SeriesStat.TARGET_TYPE), SeriesType.SERIES.getId()) &&
               equal(seriesStat.get(SeriesStat.TARGET), seriesKey.get(Series.ID));
      }
    };
  }

  public static GlobList getAllSummariesForSeries(Integer[] selectedMonths, GlobRepository sourceRepository) {
    return sourceRepository.getAll(SeriesStat.TYPE,
                                   and(fieldEquals(SeriesStat.ACCOUNT, Account.ALL_SUMMARY_ACCOUNT_ID),
                                       fieldEquals(SeriesStat.TARGET_TYPE, SeriesType.SERIES.getId()),
                                       fieldIn(SeriesStat.MONTH, selectedMonths)));
  }


  public static GlobList getAllSummaryMonthsForSeries(Glob series, GlobRepository repository) {
    return getAllSummaryMonths(series.get(Series.ID), SeriesType.SERIES, repository);
  }

  public static GlobList getAllSummaryMonths(Integer targetId, SeriesType type, GlobRepository repository) {
    return  repository.findByIndex(SeriesStat.SERIES_INDEX, targetId)
      .filterSelf(and(fieldEquals(SeriesStat.ACCOUNT, Account.ALL_SUMMARY_ACCOUNT_ID),
                      fieldEquals(SeriesStat.TARGET_TYPE, type.getId())),
                  repository);
  }

  public static GlobMatcher isSummaryForBudgetArea(final BudgetArea budgetArea) {
    return new GlobMatcher() {
      public boolean matches(Glob seriesStat, GlobRepository repository) {
        if (seriesStat == null || !isSummary(seriesStat)) {
          return false;
        }
        if (SeriesType.SERIES.getId().equals(seriesStat.get(TARGET_TYPE))) {
          Glob series = repository.find(org.globsframework.model.Key.create(Series.TYPE, seriesStat.get(TARGET)));
          return series != null && Utils.equal(series.get(Series.BUDGET_AREA), budgetArea.getId());
        }
        if (SeriesType.SERIES_GROUP.getId().equals(seriesStat.get(TARGET_TYPE))) {
          Glob group = repository.find(org.globsframework.model.Key.create(SeriesGroup.TYPE, seriesStat.get(TARGET)));
          return group != null && Utils.equal(group.get(SeriesGroup.BUDGET_AREA), budgetArea.getId());
        }
        throw new InvalidParameter("Unexpected value: " + seriesStat.get(TARGET_TYPE));
      }
    };
  }

  public static GlobList getTargets(GlobList seriesStatList, GlobRepository repository) {
    Set<Glob> result = new HashSet<Glob>();
    for (Glob seriesStat : seriesStatList) {
      result.add(getTarget(seriesStat, repository));
    }
    return new GlobList(result);
  }

  private static Glob getTarget(Glob seriesStat, GlobRepository repository) {
    Integer targetType = seriesStat.get(TARGET_TYPE);
    if (SeriesType.SERIES.getId().equals(targetType)) {
      return repository.get(org.globsframework.model.Key.create(Series.TYPE, seriesStat.get(TARGET)));
    }
    if (SeriesType.SERIES_GROUP.getId().equals(targetType)) {
      return repository.get(org.globsframework.model.Key.create(SeriesGroup.TYPE, seriesStat.get(TARGET)));
    }
    throw new UnexpectedValue("Unexpected seriesStat type " + targetType + " for: " + seriesStat);
  }

  public static SeriesType getTargetType(Glob seriesStat) {
    return SeriesType.get(seriesStat.get(TARGET_TYPE));
  }

  public static void deleteAllForSeries(Glob series, GlobRepository repository) {
    repository.delete(
      repository.findByIndex(SeriesStat.SERIES_INDEX, series.get(Series.ID))
        .filterSelf(fieldEquals(SeriesStat.TARGET_TYPE, SeriesType.SERIES.getId()), repository));
  }

  public static void deleteAllForSeriesAndMonth(Integer seriesId, Integer monthId, GlobRepository repository) {
    repository.delete(
      repository.findByIndex(SeriesStat.SERIES_INDEX, seriesId)
        .filterSelf(and(fieldEquals(SeriesStat.TARGET_TYPE, SeriesType.SERIES.getId()),
                        fieldEquals(SeriesStat.MONTH, monthId)), repository));
  }
}
