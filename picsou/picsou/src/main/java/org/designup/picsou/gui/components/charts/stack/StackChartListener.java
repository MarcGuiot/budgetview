package org.designup.picsou.gui.components.charts.stack;

import org.globsframework.model.Key;

public interface StackChartListener {
  void processClick(Key selectedKey, boolean expandSelection);

  void rolloverUpdated(Key key);
}
