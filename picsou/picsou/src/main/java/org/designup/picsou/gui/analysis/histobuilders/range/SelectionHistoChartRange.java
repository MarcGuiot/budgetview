package org.designup.picsou.gui.analysis.histobuilders.range;

import org.designup.picsou.model.Month;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import java.util.List;
import java.util.SortedSet;

public class SelectionHistoChartRange extends AbstractHistoChartRange implements GlobSelectionListener {

  private Integer start;
  private Integer end;

  public SelectionHistoChartRange(GlobRepository repository, Directory directory) {
    super(repository);
    directory.get(SelectionService.class).addListener(this, Month.TYPE);
  }

  public void selectionUpdated(GlobSelection selection) {
    SortedSet<Integer> months = selection.getAll(Month.TYPE).getSortedSet(Month.ID);
    if (!months.isEmpty()) {
      start = months.first();
      end = months.last();
    }
    else {
      start = null;
      end = null;
    }
  }

  public void scroll(int offset) {
  }

  public void reset() {
  }

  public List<Integer> getMonthIds(Integer selectedMonthId) {
    return getMonths(start, end);
  }
}
