package org.designup.picsou.gui.time.selectable;

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
