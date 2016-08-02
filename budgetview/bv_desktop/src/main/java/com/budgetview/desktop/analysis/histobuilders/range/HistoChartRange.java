package com.budgetview.desktop.analysis.histobuilders.range;

import com.budgetview.desktop.analysis.histobuilders.HistoChartRangeListener;
import org.globsframework.gui.splits.utils.Disposable;

import java.util.List;

public interface HistoChartRange extends Disposable {
  void scroll(int offset);

  void addListener(HistoChartRangeListener listener);

  void removeListener(HistoChartRangeListener listener);

  void reset();

  List<Integer> getMonthIds(Integer selectedMonthId);
}
