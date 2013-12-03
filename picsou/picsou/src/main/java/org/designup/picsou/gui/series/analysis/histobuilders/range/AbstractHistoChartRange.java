package org.designup.picsou.gui.series.analysis.histobuilders.range;

import org.designup.picsou.gui.series.analysis.histobuilders.HistoChartRangeListener;
import org.designup.picsou.model.Month;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractHistoChartRange implements HistoChartRange {

  protected GlobRepository repository;
  private List<HistoChartRangeListener> listeners = new ArrayList<HistoChartRangeListener>();

  public AbstractHistoChartRange(GlobRepository repository) {
    this.repository = repository;
  }

  protected void notifyListeners() {
    for (HistoChartRangeListener listener : listeners) {
      listener.rangeUpdated();
    }
  }

  public void addListener(HistoChartRangeListener listener) {
    listeners.add(listener);
  }

  public void removeListener(HistoChartRangeListener listener) {
    listeners.remove(listener);
  }

  protected List<Integer> getMonths(Integer start, Integer end) {
    if ((start == null) || (end == null)) {
      return new ArrayList<Integer>();
    }

    List<Integer> result = new ArrayList<Integer>();
    for (Integer monthId : Month.range(start, end)) {
      Key monthKey = Key.create(Month.TYPE, monthId);
      if (repository.contains(monthKey)) {
        result.add(monthId);
      }
    }
    return result;
  }
}
