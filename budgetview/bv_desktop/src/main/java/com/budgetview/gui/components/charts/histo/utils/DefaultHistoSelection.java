package com.budgetview.gui.components.charts.histo.utils;

import com.budgetview.gui.components.charts.histo.HistoSelection;

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

  public String toString() {
    return columnIds.toString();
  }
}
