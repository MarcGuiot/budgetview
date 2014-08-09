package org.designup.picsou.gui.analysis.utils;

import org.designup.picsou.gui.series.view.SeriesWrapper;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.SubSeries;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;

import java.util.HashSet;
import java.util.Set;

import static org.globsframework.model.utils.GlobMatchers.linkedTo;

public class SeriesWrapperSelection {
  private GlobList wrappers;
  private GlobRepository repository;

  public SeriesWrapperSelection(GlobList wrappers, GlobRepository repository) {
    this.wrappers = wrappers;
    this.repository = repository;
  }

  public boolean isSubSeriesListFromSameGroupSeries() {
    Glob series = findSingleSubListParent();
    return series != null && series.get(Series.GROUP) != null;
  }

  public boolean isSubSeriesListFromSameRootSeries() {
    Glob series = findSingleSubListParent();
    return series != null && series.get(Series.GROUP) == null;
  }

  private Glob findSingleSubListParent() {
    Set<Integer> seriesIds = new HashSet<Integer>();
    for (Glob wrapper : wrappers) {
      if (!SeriesWrapper.isSubSeries(wrapper)) {
        return null;
      }
      seriesIds.add(SeriesWrapper.getSubSeries(wrapper, repository).get(SubSeries.SERIES));
    }
    if (seriesIds.size() != 1) {
      return null;
    }
    return repository.find(Key.create(Series.TYPE, seriesIds.iterator().next()));
  }

  public boolean isSingleSeriesInGroup() {
    if ((wrappers.size() != 1) || !SeriesWrapper.isSeries(wrappers.getFirst())) {
      return false;
    }
    Glob series = SeriesWrapper.getSeries(wrappers.getFirst(), repository);
    return series.get(Series.GROUP) != null;
  }

  public boolean isSingleSeriesWithSubSeries() {
    if (wrappers.size() != 1) {
      return false;
    }
    if (SeriesWrapper.isSeries(wrappers.getFirst())) {
      return repository.contains(SeriesWrapper.TYPE, linkedTo(wrappers.getFirst(), SeriesWrapper.PARENT));
    }
    return SeriesWrapper.isSubSeries(wrappers.getFirst());
  }

  public boolean isSingleGroup() {
    return (wrappers.size() == 1) && SeriesWrapper.isGroup(wrappers.getFirst());
  }

  public boolean isRootSeriesOrGroup() {
    for (Glob wrapper : wrappers) {
      if (!SeriesWrapper.isGroup(wrapper) && !SeriesWrapper.isGroupPart(wrapper, repository)) {
        return false;
      }
    }
    return !wrappers.isEmpty();
  }
}
