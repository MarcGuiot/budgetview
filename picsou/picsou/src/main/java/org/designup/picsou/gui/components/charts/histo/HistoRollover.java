package org.designup.picsou.gui.components.charts.histo;

import org.globsframework.model.Key;

public interface HistoRollover {
  boolean isOnColumn(int columnIndex);

  boolean isOnObject(Key key);

  Integer getColumnIndex();

  Key getObjectKey();

  boolean isActive();
}
