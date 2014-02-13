package org.designup.picsou.gui.model;

import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.SeriesGroup;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.DefaultBoolean;
import org.globsframework.metamodel.annotations.DefaultDouble;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.utils.exceptions.InvalidParameter;

import static org.globsframework.model.FieldValue.value;

public class PeriodSeriesStat {
  public static GlobType TYPE;

  @Key
  public static IntegerField TARGET;

  @Key
  @Target(SeriesType.class)
  public static LinkField TARGET_TYPE;

  @DefaultDouble(0.0)
  public static DoubleField AMOUNT;

  public static DoubleField PLANNED_AMOUNT;

  @DefaultDouble(0.0)
  public static DoubleField PAST_REMAINING;

  @DefaultDouble(0.0)
  public static DoubleField FUTURE_REMAINING;

  @DefaultDouble(0.0)
  public static DoubleField PAST_OVERRUN;

  @DefaultDouble(0.0)
  public static DoubleField FUTURE_OVERRUN;

  @DefaultDouble(0.0)
  public static DoubleField ABS_SUM_AMOUNT;

  @DefaultDouble(0.0)
  public static DoubleField PREVIOUS_SUMMARY_AMOUNT;

  @DefaultDouble(0.0)
  public static DoubleField NEW_SUMMARY_AMOUNT;

  public static IntegerField PREVIOUS_SUMMARY_MONTH;
  public static IntegerField NEW_SUMMARY_MONTH;

  @DefaultBoolean(false)
  public static BooleanField ACTIVE;

  @DefaultBoolean(true)
  public static BooleanField VISIBLE;

  @DefaultBoolean(false)
  public static BooleanField TO_SET;

  static {
    GlobTypeLoader.init(PeriodSeriesStat.class);
  }

  public static boolean isForSeries(Glob periodSeriesStat) {
    return SeriesType.SERIES.equals(getSeriesType(periodSeriesStat));
  }

  public static boolean isForGroup(Glob periodSeriesStat) {
    return SeriesType.SERIES_GROUP.equals(getSeriesType(periodSeriesStat));
  }

  public static Glob findTarget(Glob periodStat, GlobRepository repository) {
    if (periodStat == null) {
      return null;
    }
    SeriesType statType = getSeriesType(periodStat);
    return repository.find(org.globsframework.model.Key.create(statType.getTargetType(), periodStat.get(TARGET)));
  }

  public static BudgetArea getBudgetArea(Glob periodStat, GlobRepository repository) {
    Glob target = findTarget(periodStat, repository);
    if (target == null) {
      return null;
    }
    switch (getSeriesType(periodStat)) {
      case SERIES:
        return Series.getBudgetArea(target);
      case SERIES_GROUP:
        return SeriesGroup.getBudgetArea(target);
    }
    throw new InvalidParameter("Unexpected type for " + periodStat);
  }

  public static String getName(Glob periodStat, GlobRepository repository) {
    Glob target = findTarget(periodStat, repository);
    if (target == null) {
      return null;
    }
    switch (getSeriesType(periodStat)) {
      case SERIES:
        return target.get(Series.NAME);
      case SERIES_GROUP:
        return target.get(SeriesGroup.NAME);
    }
    throw new InvalidParameter("Unexpected type for " + periodStat);
  }

  public static Glob findOrCreateForSeries(Integer seriesId, GlobRepository repository) {
    return findOrCreate(seriesId, SeriesType.SERIES, repository);
  }

  public static Glob findOrCreate(Integer targetId, SeriesType type, GlobRepository repository) {
    Glob existing = findUnique(targetId, type, repository);
    if (existing != null) {
      return existing;
    }
    return repository.create(TYPE,
                             value(TARGET, targetId),
                             value(TARGET_TYPE, type.getId()));
  }

  public static Glob findUnique(Integer targetId, SeriesType targetType, GlobRepository repository) {
    if (targetId == null) {
      return null;
    }
    return repository.find(org.globsframework.model.Key.create(TARGET, targetId,
                                                               TARGET_TYPE, targetType.getId()));
  }

  public static Glob findParentStat(Glob periodSeriesStat, GlobRepository repository) {
    if (!isForSeries(periodSeriesStat)) {
      return null;
    }
    Glob series = findTarget(periodSeriesStat, repository);
    return findUnique(series.get(Series.GROUP), SeriesType.SERIES_GROUP, repository);
  }

  public static GlobMatcher seriesMatcher() {
    return new GlobMatcher() {
      public boolean matches(Glob stat, GlobRepository repository) {
        return stat != null &&
               SeriesType.SERIES.getId().equals(stat.get(PeriodSeriesStat.TARGET_TYPE));
      }
    };
  }

  public static GlobMatcher groupsMatcher() {
    return new GlobMatcher() {
      public boolean matches(Glob stat, GlobRepository repository) {
        return stat != null &&
               SeriesType.SERIES_GROUP.getId().equals(stat.get(PeriodSeriesStat.TARGET_TYPE));
      }
    };
  }

  public static SeriesType getSeriesType(Glob periodSeriesStat) {
    return SeriesType.get(periodSeriesStat.get(TARGET_TYPE));
  }

  public static boolean isSeriesInGroup(Glob periodStat, GlobRepository repository) {
    Glob target = findTarget(periodStat, repository);
    if (target == null) {
      return false;
    }
    switch (getSeriesType(periodStat)) {
      case SERIES:
        return target.get(Series.GROUP) != null;
      case SERIES_GROUP:
        return false;
    }
    throw new InvalidParameter("Unexpected type for " + periodStat);
  }
}
