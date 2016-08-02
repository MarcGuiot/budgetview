package com.budgetview.desktop.components.charts.histo;

import org.globsframework.model.Key;

import java.awt.*;
import java.util.Set;

public interface HistoChartListener {
  void processClick(HistoSelection selection, Set<Key> objectKeys);

  void processDoubleClick(Integer columnIndex, Set<Key> objectKeys);

  void processRightClick(HistoSelection selection, Set<Key> objectKeys, Point mouseLocation);

  void scroll(int count);

  void rolloverUpdated(HistoRollover rollover);
}
