package com.budgetview.desktop.time.mousestates;

import com.budgetview.desktop.time.selectable.Selectable;
import com.budgetview.desktop.time.selectable.SelectableContainer;

public class PressedMouseState extends AbstractPressedState {

  public PressedMouseState(SelectableContainer container, Selectable selectable, int x, int y) {
    super(container);
    firstSelected = selectable;
    currentSelectable.add(selectable);
  }

}
