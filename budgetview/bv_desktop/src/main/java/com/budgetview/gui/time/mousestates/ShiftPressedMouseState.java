package com.budgetview.gui.time.mousestates;

import com.budgetview.gui.time.selectable.Selectable;
import com.budgetview.gui.time.selectable.SelectableContainer;

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
