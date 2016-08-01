package com.budgetview.gui.analysis.histobuilders.range;

import com.budgetview.model.util.ClosedMonthRange;
import org.globsframework.model.GlobRepository;

import java.util.List;

public class FixedHistoChartRange extends AbstractHistoChartRange {

  private ClosedMonthRange range;
  
  public FixedHistoChartRange(ClosedMonthRange range, GlobRepository repository) {
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
