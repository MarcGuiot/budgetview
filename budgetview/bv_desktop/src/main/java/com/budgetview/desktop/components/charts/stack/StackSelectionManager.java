package com.budgetview.desktop.components.charts.stack;

import org.globsframework.model.Key;
import org.globsframework.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class StackSelectionManager {

  private Key currentRollover;
  private List<StackChartListener> listeners = new ArrayList<StackChartListener>();

  public void addListener(StackChartListener listener) {
    this.listeners.add(listener);
  }

  public void clear() {
    currentRollover = null;
  }

  public void startClick(boolean forceExpandSelection, boolean rightClick) {
    if (currentRollover != null) {
      addRolloverToSelection(forceExpandSelection, rightClick);
    }
    else {
      notifyClick(forceExpandSelection, rightClick);
    }
  }

  public void updateRollover(Key key, boolean forceExpandSelection, boolean rightClick) {
    boolean rolloverChanged = false;
    boolean doExpandSelection = false;

    if (!Utils.equal(key, currentRollover)) {
      this.currentRollover = key;
      rolloverChanged = true;
      doExpandSelection = forceExpandSelection;
    }

    if (rolloverChanged) {
      for (StackChartListener listener : listeners) {
        listener.rolloverUpdated(currentRollover);
      }
    }

    if (doExpandSelection) {
      addRolloverToSelection(forceExpandSelection, rightClick);
    }
  }

  private void addRolloverToSelection(boolean forceExpandSelection, boolean rightClick) {
    if (currentRollover == null) {
      return;
    }
    notifyClick(forceExpandSelection, rightClick);
  }

  public boolean isRollover(Key key) {
    return key != null && key.equals(currentRollover);
  }

  private void notifyClick(boolean forceExpandSelection, boolean rightClick) {
    for (StackChartListener listener : listeners) {
      if (rightClick) {
        listener.processRightClick(currentRollover, forceExpandSelection);
      }
      else {
        listener.processClick(currentRollover, forceExpandSelection);
      }
    }
  }
}
