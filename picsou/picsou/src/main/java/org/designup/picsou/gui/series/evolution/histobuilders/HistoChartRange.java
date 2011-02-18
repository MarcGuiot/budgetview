package org.designup.picsou.gui.series.evolution.histobuilders;

import org.designup.picsou.model.CurrentMonth;
import org.designup.picsou.model.Month;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

public class HistoChartRange {

  private GlobRepository repository;
  private int monthsBack;
  private int monthsLater;
  private boolean centerOnSelection;

  private List<HistoChartRangeListener> listeners = new ArrayList<HistoChartRangeListener>();
  private int scrollMonth = 0;

  public HistoChartRange(int monthsBack, int monthsLater, boolean centerOnSelection, GlobRepository repository) {
    this.repository = repository;
    this.monthsBack = monthsBack;
    this.monthsLater = monthsLater;
    this.centerOnSelection = centerOnSelection;
  }

  public void scroll(int offset) {
    scrollMonth += offset;
    notifyListeners();
  }

  private void notifyListeners() {
    for (HistoChartRangeListener listener : listeners) {
      listener.rangeUpdated();
    }
  }

  public void addListener(HistoChartRangeListener listener) {
    listeners.add(listener);
  }

  public void reset() {
    if (scrollMonth != 0) {
      scrollMonth = 0;
      notifyListeners();
    }
  }

  public List<Integer> getMonthIds(Integer selectedMonthId) {
    SortedSet<Integer> monthIds = repository.getAll(Month.TYPE).getSortedSet(Month.ID);
    int firstMonth = monthIds.first();
    int lastMonth = monthIds.last();

    Integer currentMonth = centerOnSelection ? selectedMonthId : CurrentMonth.getCurrentMonth(repository);
    int offsetCenter = Month.offset(currentMonth, scrollMonth);
    int rangeStart = Month.previous(offsetCenter, monthsBack);
    int rangeEnd = Month.next(offsetCenter, monthsLater);

    if (selectedMonthId < rangeStart) {
      rangeStart = selectedMonthId;
      offsetCenter = Month.next(rangeStart, monthsBack);
      scrollMonth = Month.distance(currentMonth, offsetCenter);
      rangeEnd = Math.min(lastMonth, Month.next(offsetCenter, monthsLater));
    }
    if (selectedMonthId > rangeEnd) {
      rangeEnd = selectedMonthId;
      offsetCenter = Month.previous(rangeEnd, monthsLater);
      scrollMonth = Month.distance(currentMonth, offsetCenter);
      rangeStart = Math.max(firstMonth, Month.previous(offsetCenter, monthsBack));
    }

    if (scrollMonth < 0) {
      while ((scrollMonth != 0) && (rangeStart < firstMonth)) {
        scrollMonth++;
        offsetCenter = Month.offset(currentMonth, scrollMonth);
        rangeStart = Month.previous(offsetCenter, monthsBack);
      }
      rangeEnd = Math.min(lastMonth, Month.next(offsetCenter, monthsLater));
    }
    else if (scrollMonth > 0) {
      while ((scrollMonth != 0) && (rangeEnd > lastMonth)) {
        scrollMonth--;
        offsetCenter = Month.offset(currentMonth, scrollMonth);
        rangeEnd = Month.next(offsetCenter, monthsLater);
      }
      rangeStart = Math.max(firstMonth, Month.previous(offsetCenter, monthsBack));
    }

    List<Integer> result = new ArrayList<Integer>();
    for (Integer monthId : Month.range(rangeStart, rangeEnd)) {
      Key monthKey = Key.create(Month.TYPE, monthId);
      if (repository.contains(monthKey)) {
        result.add(monthId);
      }
    }
    return result;
  }
}
