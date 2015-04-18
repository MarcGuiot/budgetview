package org.designup.picsou.gui.analysis.histobuilders.range;

import org.designup.picsou.model.CurrentMonth;
import org.designup.picsou.model.Month;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.TypeChangeSetListener;

import java.util.List;
import java.util.SortedSet;

public class ScrollableHistoChartRange extends AbstractHistoChartRange {

  private int monthsBack;
  private int monthsLater;
  private boolean centerOnSelection;

  private int scrollOffset = 0;

  private Integer rangeStart;
  private Integer rangeEnd;
  private int firstMonth;
  private int lastMonth;
  private final TypeChangeSetListener monthListener;

  public ScrollableHistoChartRange(int monthsBack, int monthsLater, boolean centerOnSelection, GlobRepository repository) {
    super(repository);
    this.monthsBack = monthsBack;
    this.monthsLater = monthsLater;
    this.centerOnSelection = centerOnSelection;
    this.monthListener = new TypeChangeSetListener(Month.TYPE) {
      public void update(GlobRepository repository) {
        updateBounds();
      }
    };
    repository.addChangeListener(monthListener);
    updateBounds();
  }

  public void dispose() {
    repository.removeChangeListener(monthListener);
  }

  private void updateBounds() {
    SortedSet<Integer> monthIds = repository.getAll(Month.TYPE).getSortedSet(Month.ID);
    if (monthIds.isEmpty()) {
      return;
    }
    firstMonth = monthIds.first();
    lastMonth = monthIds.last();
  }

  public void scroll(int offset) {
    scrollOffset += offset;
    notifyListeners();
  }

  public void reset() {
    if (scrollOffset != 0) {
      scrollOffset = 0;
      notifyListeners();
    }
  }

  public List<Integer> getMonthIds(Integer selectedMonthId) {
    if (centerOnSelection) {
      centerOnSelectedMonth(selectedMonthId);
    }
    else {
      centerOnCurrentMonth(selectedMonthId);
    }

    return getMonths(rangeStart, rangeEnd);
  }

  private void centerOnSelectedMonth(Integer selectedMonthId) {

    rangeStart = Month.previous(selectedMonthId, monthsBack);
    rangeEnd = Month.next(selectedMonthId, monthsLater);

    if (rangeStart < firstMonth) {
      rangeStart = firstMonth;
    }
    if (rangeEnd > lastMonth) {
      rangeEnd = lastMonth;
    }

    int actualScroll = 0;
    if (scrollOffset < 0) {
      while (actualScroll > scrollOffset) {
        if ((rangeEnd > selectedMonthId) && shiftLeft()) {
          actualScroll--;
        }
        else {
          break;
        }
      }
    }
    else if (scrollOffset > 0) {
      while (actualScroll < scrollOffset) {
        if ((rangeStart < selectedMonthId) && shiftRight()) {
          actualScroll++;
        }
        else {
          break;
        }
      }
    }

    scrollOffset = actualScroll;
  }

  private void centerOnCurrentMonth(Integer selectedMonthId) {
    Integer currentMonth = CurrentMonth.findCurrentMonth(repository);
    if (currentMonth == null) {
      return;
    }

    center(currentMonth);

    if (selectedMonthId < rangeStart) {
      alignLeft(selectedMonthId);
    }
    if (selectedMonthId > rangeEnd) {
      alignRight(selectedMonthId);
    }

    int actualScroll = 0;
    if (scrollOffset < 0) {
      while (actualScroll > scrollOffset) {
        if (shiftLeft()) {
          actualScroll--;
        }
        else {
          break;
        }
      }
    }
    else if (scrollOffset > 0) {
      while (actualScroll < scrollOffset) {
        if (shiftRight()) {
          actualScroll++;
        }
        else {
          break;
        }
      }
    }

    scrollOffset = actualScroll;
  }

  private boolean shiftLeft() {
    if (rangeStart <= firstMonth) {
      return false;
    }
    rangeStart = Month.previous(rangeStart);
    rangeEnd = Month.previous(rangeEnd);
    return true;
  }

  private boolean shiftRight() {
    if (rangeEnd >= lastMonth) {
      return false;
    }
    rangeStart = Month.next(rangeStart);
    rangeEnd = Month.next(rangeEnd);
    return true;
  }

  private void alignLeft(Integer month) {
    rangeStart = month;
    rangeEnd = Math.min(lastMonth, Month.next(rangeStart, getRange()));
  }

  private int getRange() {
    return monthsBack + monthsLater;
  }

  private void alignRight(int month) {
    rangeEnd = month;
    rangeStart = Math.max(firstMonth, Month.previous(rangeEnd, getRange()));
  }

  private void center(Integer monthId) {
    rangeStart = Month.previous(monthId, monthsBack);
    rangeEnd = Month.next(monthId, monthsLater);

    if (rangeStart < firstMonth) {
      alignLeft(firstMonth);
    }
    if (rangeEnd > lastMonth) {
      alignRight(lastMonth);
    }
  }

  public String toString() {
    return "scrollableRange(" + monthsBack + ", " + monthsLater + ") ==> "  + firstMonth + "-" + lastMonth;
  }
}
