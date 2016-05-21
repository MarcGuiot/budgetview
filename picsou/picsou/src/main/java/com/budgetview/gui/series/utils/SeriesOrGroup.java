package com.budgetview.gui.series.utils;

import com.budgetview.gui.model.SeriesType;
import com.budgetview.model.BudgetArea;
import com.budgetview.gui.model.SeriesStat;
import com.budgetview.gui.series.view.SeriesWrapper;
import com.budgetview.model.Account;
import com.budgetview.model.Series;
import com.budgetview.model.SeriesGroup;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.exceptions.InvalidParameter;
import org.globsframework.utils.exceptions.UnexpectedValue;

import java.util.HashSet;
import java.util.Set;

public class SeriesOrGroup {
  public final Integer id;
  public final SeriesType type;

  public SeriesOrGroup(Glob target) {
    this(getId(target), SeriesType.get(target));
  }

  private static Integer getId(Glob target) {
    if (Series.TYPE.equals(target.getType())) {
      return target.get(Series.ID);
    }
    if (SeriesGroup.TYPE.equals(target.getType())) {
      return target.get(SeriesGroup.ID);
    }
    throw new InvalidParameter("Unexpected type: " + target);
  }

  public SeriesOrGroup(Integer id, SeriesType type) {
    this.id = id;
    this.type = type;
  }

  public Integer getId() {
    return id;
  }

  public String getName(GlobRepository repository) {
    switch (type) {
      case SERIES:
        Glob series = repository.get(Key.create(Series.TYPE, id));
        return series.get(Series.NAME);
      case SERIES_GROUP:
        Glob group = repository.get(Key.create(SeriesGroup.TYPE, id));
        return group.get(SeriesGroup.NAME);
    }
    throw new UnexpectedValue("Unexpected type " + type);
  }

  public BudgetArea getBudgetArea(GlobRepository repository) {
    switch (type) {
      case SERIES:
        Glob series = repository.get(Key.create(Series.TYPE, id));
        return BudgetArea.get(series.get(Series.BUDGET_AREA));
      case SERIES_GROUP:
        Glob group = repository.get(Key.create(SeriesGroup.TYPE, id));
        return BudgetArea.get(group.get(SeriesGroup.BUDGET_AREA));
    }
    throw new UnexpectedValue("Unexpected type " + type);
  }

  public boolean shouldCreateWrapper(GlobRepository repository) {
    switch (type) {
      case SERIES:
        Glob series = repository.get(Key.create(Series.TYPE, id));
        return SeriesWrapper.shouldCreateWrapperForSeries(series);
      case SERIES_GROUP:
        return true;
    }
    throw new UnexpectedValue("Unexpected type " + type);
  }

  public Key createSeriesStatKey(Integer monthId) {
    return Key.create(SeriesStat.ACCOUNT, Account.ALL_SUMMARY_ACCOUNT_ID,
                      SeriesStat.TARGET, id,
                      SeriesStat.TARGET_TYPE, type.getId(),
                      SeriesStat.MONTH, monthId);
  }

  public GlobList getStatsForAllMonths(GlobRepository repository) {
    return SeriesStat.getAllSummaryMonths(id, type, repository);
  }

  public static SeriesOrGroup getFromStat(Glob seriesStat) {
    return new SeriesOrGroup(seriesStat.get(SeriesStat.TARGET), SeriesStat.getTargetType(seriesStat));
  }

  public static Set<SeriesOrGroup> getAllFromSeriesStat(GlobList seriesStatList) {
    Set<SeriesOrGroup> result = new HashSet<SeriesOrGroup>();
    for (Glob seriesStat : seriesStatList) {
      result.add(getFromStat(seriesStat));
    }
    return result;
  }

  public Key getKey() {
    switch (type) {
      case SERIES:
        return Key.create(Series.TYPE, id);
      case SERIES_GROUP:
        return Key.create(SeriesGroup.TYPE, id);
    }
    throw new UnexpectedValue("Unexpected type " + type);
  }

  public static Set<Key> createKeys(Set<SeriesOrGroup> seriesOrGroups) {
    Set<Key> result = new HashSet<Key>();
    for (SeriesOrGroup seriesOrGroup : seriesOrGroups) {
      result.add(seriesOrGroup.getKey());
    }
    return result;
  }

  public int hashCode() {
    int result = id.hashCode();
    result = 31 * result + type.hashCode();
    return result;
  }

  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    SeriesOrGroup that = (SeriesOrGroup)o;

    if (!id.equals(that.id)) {
      return false;
    }
    if (type != that.type) {
      return false;
    }

    return true;
  }

  public boolean isGroup() {
    return SeriesType.SERIES_GROUP.equals(type);
  }

  public boolean isSeries() {
    return SeriesType.SERIES.equals(type);
  }

  public boolean isInGroup(GlobRepository repository) {
    if (!type.equals(SeriesType.SERIES)) {
      return false;
    }

    Glob series = repository.get(Key.create(Series.TYPE, id));
    return series.get(Series.GROUP) != null;
  }

  public SeriesOrGroup getContainingGroup(GlobRepository repository) {
    if (!isSeries()) {
      return null;
    }
    Glob series = repository.get(Key.create(Series.TYPE, id));
    if (series.get(Series.GROUP) == null) {
      return null;
    }
    return new SeriesOrGroup(series.get(Series.GROUP), SeriesType.SERIES_GROUP);
  }
}
