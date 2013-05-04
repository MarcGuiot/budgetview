package org.designup.picsou.gui.components.charts.histo;

import com.budgetview.shared.gui.histochart.HistoDataset;
import org.designup.picsou.gui.components.charts.histo.utils.DefaultHistoSelection;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.model.Key;
import org.globsframework.utils.Utils;
import org.globsframework.utils.exceptions.InvalidParameter;

import java.util.*;

public class HistoSelectionManager implements Disposable {

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

  public void dispose() {
    listeners.clear();
    dataset = null;
  }

  public void resetRollover(HistoDataset dataset) {
    this.dataset = dataset;
    this.rollover.columnIndex = null;
    this.rollover.setObjectKeys(new HashSet<Key>());
    for (HistoChartListener listener : listeners) {
      listener.rolloverUpdated(getRollover());
    }
  }

  public void updateRollover(Integer columnIndex, Set<Key> objectKeys, boolean dragging, boolean rightClick) {
    boolean rolloverChanged = false;
    boolean expandSelection = false;

    if (!Utils.equal(columnIndex, rollover.columnIndex)) {
      this.rollover.columnIndex = columnIndex;
      rolloverChanged = true;
      expandSelection = dragging;
    }

    if (!Utils.equal(objectKeys, rollover.objectKeys)) {
      this.rollover.setObjectKeys(objectKeys);
      rolloverChanged = true;
    }

    if (rolloverChanged) {
      for (HistoChartListener listener : listeners) {
        listener.rolloverUpdated(getRollover());
      }
    }
    if (expandSelection) {
      addRolloverColumnToSelection(rightClick);
    }
  }

  public void startClick(boolean rightClick) {
    columnSelectionMinIndex = null;
    columnSelectionMaxIndex = null;

    if ((rollover.columnIndex >= 0) && (rollover.columnIndex < dataset.size())) {
      addRolloverColumnToSelection(rightClick);
    }
    else {
      notifyClick(rightClick);
    }
  }

  private void notifyClick(boolean isRightClick) {
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
    Set<Key> objectKeys = rollover.getObjectKeys();
    for (HistoChartListener listener : listeners) {
      if (isRightClick) {
        listener.processRightClick(selection, objectKeys);
      }
      else {
        listener.processClick(selection, objectKeys);
      }
    }
  }

  public boolean canSelect() {
    return (rollover.columnIndex != null) && (listeners != null);
  }

  public void notifyDoubleClick() {
    for (HistoChartListener listener : listeners) {
      listener.processDoubleClick(rollover.columnIndex, rollover.objectKeys);
    }
  }

  public void addRolloverColumnToSelection(boolean rightClick) {
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
      notifyClick(rightClick);
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
    private Set<Key> objectKeys = new HashSet<Key>();

    public boolean isOnColumn(int columnIndex) {
      return Utils.equal(this.columnIndex, columnIndex);
    }

    public boolean isOnObject(Key key) {
      return objectKeys.contains(key);
    }

    public Integer getColumnIndex() {
      return columnIndex;
    }

    public Set<Key> getObjectKeys() {
      return objectKeys;
    }

    public boolean isActive() {
      return (columnIndex != null) || !objectKeys.isEmpty();
    }

    public void setObjectKeys(Set<Key> objectKeys) {
      if (objectKeys == null) {
        throw new InvalidParameter("Use Collections.emptySet");
      }
      this.objectKeys = objectKeys;
    }
  }
}
