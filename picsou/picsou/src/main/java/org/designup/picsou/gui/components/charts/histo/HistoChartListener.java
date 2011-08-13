package org.designup.picsou.gui.components.charts.histo;

import org.globsframework.model.Key;

public interface HistoChartListener {
  void processClick(HistoSelection selection, Key objectKey);

  void processDoubleClick(Integer columnIndex, Key objectKey);

  void scroll(int count);

  void rolloverUpdated(HistoRollover rollover);
}
