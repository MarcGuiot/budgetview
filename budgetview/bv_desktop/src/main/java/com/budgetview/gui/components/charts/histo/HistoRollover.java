package com.budgetview.gui.components.charts.histo;

import org.globsframework.model.Key;

import java.util.Set;

public interface HistoRollover {
  boolean isOnColumn(int columnIndex);

  boolean isOnObject(Key key);

  Integer getColumnIndex();

  Set<Key> getObjectKeys();

  boolean isActive();
}
