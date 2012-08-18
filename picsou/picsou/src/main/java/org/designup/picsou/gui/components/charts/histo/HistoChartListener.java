package org.designup.picsou.gui.components.charts.histo;

import org.globsframework.model.Key;

import java.util.Set;

public interface HistoChartListener {
  void processClick(HistoSelection selection, Set<Key> objectKeys);

  void processDoubleClick(Integer columnIndex, Set<Key> objectKeys);

  void processRightClick(HistoSelection selection, Set<Key> objectKeys);

  void scroll(int count);

  void rolloverUpdated(HistoRollover rollover);
}
