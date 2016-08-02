package com.budgetview.desktop.time.selectable;

import java.util.Set;

public interface SelectableContainer {
  Set<Selectable> getCurrentlySelectedToUpdate();

  Selectable getSelectable(int x, int y);

  void repaint();

  Selectable getLastSelected();

  void setLastSelected(Selectable selectable);

}
