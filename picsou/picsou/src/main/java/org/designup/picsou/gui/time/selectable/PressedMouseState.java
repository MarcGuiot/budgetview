package org.designup.picsou.gui.time.selectable;

public class PressedMouseState extends AbstractPressedState {

  public PressedMouseState(SelectableContainer container, Selectable selectable, int x, int y) {
    super(container);
    this.x = x;
    this.y = y;
    firstSelected = selectable;
    currentSelectable.add(selectable);
  }

}
