package org.designup.picsou.gui.analysis.histobuilders.range;

import org.designup.picsou.gui.analysis.histobuilders.HistoChartRangeListener;

import java.util.List;

public interface HistoChartRange {
  void scroll(int offset);

  void addListener(HistoChartRangeListener listener);

  void removeListener(HistoChartRangeListener listener);

  void reset();

  List<Integer> getMonthIds(Integer selectedMonthId);
}
