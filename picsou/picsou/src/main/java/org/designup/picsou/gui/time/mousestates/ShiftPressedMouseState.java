package org.designup.picsou.gui.time.mousestates;

import org.designup.picsou.gui.time.selectable.SelectableContainer;
import org.designup.picsou.gui.time.selectable.Selectable;

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
