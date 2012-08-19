package org.designup.picsou.gui.components.charts.stack;

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

  public void startClick(boolean expandSelection, boolean rightClick) {
    if (currentRollover != null) {
      addRolloverToSelection(expandSelection, rightClick);
    }
    else {
      notifyClick(expandSelection, rightClick);
    }
  }

  public void updateRollover(Key key, boolean expandSelection, boolean rightClick) {
    boolean rolloverChanged = false;
    boolean doExpandSelection = false;

    if (!Utils.equal(key, currentRollover)) {
      this.currentRollover = key;
      rolloverChanged = true;
      doExpandSelection = expandSelection;
    }

    if (rolloverChanged) {
      for (StackChartListener listener : listeners) {
        listener.rolloverUpdated(currentRollover);
      }
    }

    if (doExpandSelection) {
      addRolloverToSelection(expandSelection, rightClick);
    }
  }

  private void addRolloverToSelection(boolean expandSelection, boolean rightClick) {
    if (currentRollover == null) {
      return;
    }
    notifyClick(expandSelection, rightClick);
  }

  public boolean isRollover(Key key) {
    return key != null && key.equals(currentRollover);
  }

  private void notifyClick(boolean expandSelection, boolean rightClick) {
    for (StackChartListener listener : listeners) {
      if (rightClick) {
        listener.processRightClick(currentRollover, expandSelection);
      }
      else {
        listener.processClick(currentRollover, expandSelection);
      }
    }
  }
}
