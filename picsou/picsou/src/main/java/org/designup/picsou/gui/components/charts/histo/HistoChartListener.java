package org.designup.picsou.gui.components.charts.histo;

import org.globsframework.model.Key;

public interface HistoChartListener {
  void columnsClicked(HistoSelection selection);

  void doubleClick(Integer columnIndex, Key objectKey);

  void scroll(int count);

  void rolloverUpdated(HistoRollover rollover);
}
