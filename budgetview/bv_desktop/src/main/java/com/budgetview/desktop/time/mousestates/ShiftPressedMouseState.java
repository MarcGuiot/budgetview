package com.budgetview.desktop.time.mousestates;

import com.budgetview.desktop.time.selectable.Selectable;
import com.budgetview.desktop.time.selectable.SelectableContainer;

public class ShiftPressedMouseState extends AbstractPressedState {

  public ShiftPressedMouseState(SelectableContainer container) {
    super(container);
    Selectable lastSelected = container.getLastSelected();
    this.firstSelected = lastSelected;
    if (lastSelected != null) {
      currentSelectable.add(lastSelected);
    }
  }

}
