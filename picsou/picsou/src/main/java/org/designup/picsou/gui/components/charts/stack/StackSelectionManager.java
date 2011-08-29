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

  public void startClick(boolean expandSelection) {
    if (currentRollover != null) {
      addRolloverToSelection(expandSelection);
    }
    else {
      notifyClick(expandSelection);
    }
  }

  public void updateRollover(Key key, boolean expandSelection) {
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
      addRolloverToSelection(expandSelection);
    }
  }

  private void addRolloverToSelection(boolean expandSelection) {
    if (currentRollover == null) {
      return;
    }
    notifyClick(expandSelection);
  }

  public boolean isRollover(Key key) {
    return key != null && key.equals(currentRollover);
  }

  private void notifyClick(boolean expandSelection) {
    for (StackChartListener listener : listeners) {
      listener.processClick(currentRollover, expandSelection);
    }
  }
}
