package org.designup.picsou.gui.components.charts.histo;

import org.designup.picsou.gui.components.charts.histo.utils.DefaultHistoSelection;
import org.globsframework.model.Key;
import org.globsframework.utils.Utils;

import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeSet;

public class HistoSelectionManager {

  private java.util.List<HistoChartListener> listeners;

  private HistoDataset dataset;

  private Integer columnSelectionMinIndex;
  private Integer columnSelectionMaxIndex;

  private InnerHistoRollover rollover = new InnerHistoRollover();

  public HistoSelectionManager() {
  }

  public void addListener(HistoChartListener listener) {
    if (listeners == null) {
      listeners = new ArrayList<HistoChartListener>();
    }
    this.listeners.add(listener);
  }

  public void resetRollover(HistoDataset dataset) {
    this.dataset = dataset;
    this.rollover.columnIndex = null;
    this.rollover.objectKey = null;
    for (HistoChartListener listener : listeners) {
      listener.rolloverUpdated(getRollover());
    }
  }

  public void updateRollover(Integer columnIndex, Key objectKey, boolean dragging) {
    boolean rolloverChanged = false;
    boolean expandSelection = false;

    if (!Utils.equal(columnIndex, rollover.columnIndex)) {
      this.rollover.columnIndex = columnIndex;
      rolloverChanged = true;
      expandSelection = dragging;
    }

    if (!Utils.equal(objectKey, rollover.objectKey)) {
      this.rollover.objectKey = objectKey;
      rolloverChanged = true;
    }

    if (rolloverChanged) {
      for (HistoChartListener listener : listeners) {
        listener.rolloverUpdated(getRollover());
      }
    }
    if (expandSelection) {
      addRolloverColumnToSelection();
    }
  }

  public void startClick() {
    columnSelectionMinIndex = null;
    columnSelectionMaxIndex = null;

    if ((rollover.columnIndex >=0) && (rollover.columnIndex < dataset.size())) {
      addRolloverColumnToSelection();
    }
    else {
      notifyClick();
    }
  }

  private void notifyClick() {
    if ((columnSelectionMinIndex == null) || (columnSelectionMaxIndex == null)) {
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
      listener.processClick(selection, rollover.getObjectKey());
    }
  }

  public boolean canSelect() {
    return (rollover.columnIndex != null) && (listeners != null);
  }

  public void notifyDoubleClick() {
    for (HistoChartListener listener : listeners) {
      listener.processDoubleClick(rollover.columnIndex, rollover.objectKey);
    }
  }

  public void addRolloverColumnToSelection() {
    boolean selectionChanged = false;
    if ((columnSelectionMinIndex == null) || (rollover.columnIndex < columnSelectionMinIndex)) {
      columnSelectionMinIndex = rollover.columnIndex;
      selectionChanged = true;
    }
    if ((columnSelectionMaxIndex == null) || (rollover.columnIndex > columnSelectionMaxIndex)) {
      columnSelectionMaxIndex = rollover.columnIndex;
      selectionChanged = true;
    }
    if (selectionChanged) {
      notifyClick();
    }
  }

  public void notifyScroll(int wheelRotation) {
    for (HistoChartListener listener : listeners) {
      listener.scroll(wheelRotation);
    }
  }

  public HistoRollover getRollover() {
    return rollover;
  }

  private class InnerHistoRollover implements HistoRollover {
    private Integer columnIndex;
    private Key objectKey;

    public boolean isOnColumn(int columnIndex) {
      return Utils.equal(this.columnIndex, columnIndex);
    }

    public boolean isOnObject(Key key) {
      return Utils.equal(this.objectKey, key);
    }

    public Integer getColumnIndex() {
      return columnIndex;
    }

    public Key getObjectKey() {
      return objectKey;
    }

    public boolean isActive() {
      return (columnIndex != null) || (objectKey != null);
    }
  }
}
