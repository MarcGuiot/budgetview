package org.designup.picsou.gui.time.selectable;

public class PressedMouseState extends AbstractPressedState {

  public PressedMouseState(SelectableContainer container, Selectable selectable, int x, int y) {
    super(container);
    firstSelected = selectable;
    currentSelectable.add(selectable);
  }

}
