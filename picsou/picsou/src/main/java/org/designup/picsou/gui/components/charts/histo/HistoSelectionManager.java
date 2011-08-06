package org.designup.picsou.gui.components.charts.histo;

import org.designup.picsou.gui.components.charts.histo.utils.DefaultHistoSelection;
import org.globsframework.utils.Utils;

import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeSet;

public class HistoSelectionManager {

  private Integer columnSelectionMinIndex;
  private Integer columnSelectionMaxIndex;

  private Integer rolloverColumnIndex;

  private java.util.List<HistoChartListener> listeners;

  public void startClick(HistoDataset dataset) {
    resetSelection();

    if (rolloverColumnIndex < dataset.size()) {
      addRolloverColumnToSelection(dataset);
    }
    else {
      notifyColumnSelection(dataset);
    }
  }

  public void addRolloverColumnToSelection(HistoDataset dataset) {
    boolean selectionChanged = false;
    if ((columnSelectionMinIndex == null) || (rolloverColumnIndex < columnSelectionMinIndex)) {
      columnSelectionMinIndex = rolloverColumnIndex;
      selectionChanged = true;
    }
    if ((columnSelectionMaxIndex == null) || (rolloverColumnIndex > columnSelectionMaxIndex)) {
      columnSelectionMaxIndex = rolloverColumnIndex;
      selectionChanged = true;
    }
    if (selectionChanged) {
      notifyColumnSelection(dataset);
    }
  }

  private void notifyColumnSelection(HistoDataset dataset) {
    if (!hasSelection()) {
      return;
    }

    SortedSet<Integer> columnIds = new TreeSet<Integer>();
    if (dataset != null) {
      for (Integer columnIndex : Utils.range(columnSelectionMinIndex, columnSelectionMaxIndex)) {
        int id = dataset.getId(columnIndex);
        if (id >= 0) {
          columnIds.add(id);
        }
      }
    }
    HistoSelection selection = new DefaultHistoSelection(columnIds);
    for (HistoChartListener listener : listeners) {
      listener.columnsClicked(selection);
    }
  }

  public void resetSelection() {
    columnSelectionMinIndex = null;
    columnSelectionMaxIndex = null;
  }

  public boolean hasSelection() {
    return (columnSelectionMinIndex != null) && (columnSelectionMaxIndex != null);
  }

  public void addListener(HistoChartListener listener) {
    if (listeners == null) {
      listeners = new ArrayList<HistoChartListener>();
    }
    this.listeners.add(listener);
  }

  public boolean canSelect() {
    return (rolloverColumnIndex != null) && (listeners != null);
  }

  public void notifyDoubleClick() {
    for (HistoChartListener listener : listeners) {
      listener.doubleClick();
    }
  }

  public void notifyScroll(int wheelRotation) {
    for (HistoChartListener listener : listeners) {
      listener.scroll(wheelRotation);
    }
  }

  public Integer getRolloverColumnIndex() {
    return rolloverColumnIndex;
  }

  public boolean isCurrentRolloverColumn(Integer columnIndex) {
    return (Utils.equal(columnIndex, rolloverColumnIndex));
  }

  public void setRolloverColumn(Integer columnIndex) {
    this.rolloverColumnIndex = columnIndex;
  }

  public boolean isRolloverColumn(int columnIndex) {
    return (rolloverColumnIndex != null) && (rolloverColumnIndex == columnIndex);
  }

  public void resetRollover() {
    this.rolloverColumnIndex = null;
  }
}
