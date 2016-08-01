package com.budgetview.gui.time.mousestates;

import com.budgetview.gui.time.selectable.SelectableContainer;
import com.budgetview.gui.time.selectable.Selectable;

public class PressedMouseState extends AbstractPressedState {

  public PressedMouseState(SelectableContainer container, Selectable selectable, int x, int y) {
    super(container);
    firstSelected = selectable;
    currentSelectable.add(selectable);
  }

}
