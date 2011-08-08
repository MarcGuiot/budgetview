package org.designup.picsou.gui.components.charts.histo.utils;

import org.designup.picsou.gui.components.charts.histo.HistoSelection;

import java.util.Collections;
import java.util.SortedSet;

public class DefaultHistoSelection implements HistoSelection {

  private SortedSet<Integer> columnIds;

  public DefaultHistoSelection(SortedSet<Integer> columnIds) {
    this.columnIds = columnIds;
  }

  public SortedSet<Integer> getColumnIds() {
    return Collections.unmodifiableSortedSet(columnIds);
  }
}