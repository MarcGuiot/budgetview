package org.designup.picsou.gui.series.analysis.histobuilders.range;

import org.designup.picsou.model.util.MonthRange;
import org.globsframework.model.GlobRepository;

import java.util.List;

public class FixedHistoChartRange extends AbstractHistoChartRange {

  private MonthRange range;
  
  public FixedHistoChartRange(MonthRange range, GlobRepository repository) {
    super(repository);
    this.range = range;
  }

  public void scroll(int offset) {
  }

  public void reset() {
  }

  public List<Integer> getMonthIds(Integer selectedMonthId) {
    return range.asList();
  }
}
