package org.designup.picsou.gui.series.utils;

import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.gui.model.SeriesType;
import org.designup.picsou.gui.series.view.SeriesWrapper;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.SeriesGroup;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.exceptions.UnexpectedValue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SeriesOrGroup {
  public final Integer id;
  public final SeriesType type;

  public SeriesOrGroup(Integer id, SeriesType type) {
    this.id = id;
    this.type = type;
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
    return Key.create(SeriesStat.TARGET, id,
                      SeriesStat.TARGET_TYPE, type.getId(),
                      SeriesStat.MONTH, monthId);
  }

  public GlobList getStatsForAllMonths(GlobRepository repository) {
    return SeriesStat.getAllMonths(id, type, repository);
  }

  public static Set<SeriesOrGroup> getAllFromSeriesStat(GlobList seriesStatList) {
    Set<SeriesOrGroup> result = new HashSet<SeriesOrGroup>();
    for (Glob seriesStat : seriesStatList) {
      result.add(new SeriesOrGroup(seriesStat.get(SeriesStat.TARGET), SeriesStat.getTargetType(seriesStat)));
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

  public boolean isSeries() {
    return SeriesType.SERIES.equals(type);
  }
}
