package org.designup.picsou.gui.components.charts.histo.utils;

import org.designup.picsou.gui.components.charts.histo.HistoChartListener;
import org.designup.picsou.gui.components.charts.histo.HistoRollover;
import org.designup.picsou.gui.components.charts.histo.HistoSelection;
import org.globsframework.model.Key;

import java.util.Set;

public class HistoChartListenerAdapter implements HistoChartListener {
  public void processClick(HistoSelection selection, Set<Key> objectKeys) {
  }

  public void processDoubleClick(Integer columnIndex, Set<Key> objectKeys) {
  }

  public void processRightClick(HistoSelection selection, Set<Key> objectKeys) {
  }

  public void scroll(int count) {
  }

  public void rolloverUpdated(HistoRollover rollover) {
  }
}
