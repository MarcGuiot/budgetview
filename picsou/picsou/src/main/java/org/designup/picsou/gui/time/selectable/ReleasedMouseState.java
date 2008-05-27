package org.designup.picsou.gui.time.selectable;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.Set;

public class ReleasedMouseState extends AbstractMouseState {
  int lastx;
  int lasty;

  public ReleasedMouseState(SelectableContainer container) {
    super(container);
  }

  public MouseState mousePressed(MouseEvent e) {
    Selectable selectable = getSelectable(e.getX(), e.getY());
    if (selectable == null) {
      return this;
    }
    Set<Selectable> currentlySelected = getCurrentlySelected();
    int onCtrlMask = InputEvent.CTRL_DOWN_MASK | InputEvent.BUTTON1_DOWN_MASK;
    int onShiftMask = InputEvent.SHIFT_DOWN_MASK | InputEvent.BUTTON1_DOWN_MASK;
    if ((e.getModifiersEx() & onCtrlMask) == onCtrlMask) {
      if (currentlySelected.contains(selectable)) {
        if (currentlySelected.size() > 1) {
          selectable.unSelect();
          container.setLastSeletected(null);
          currentlySelected.remove(selectable);
          return this;
        }
      }
    }
    else if ((e.getModifiersEx() & onShiftMask) == onShiftMask) {
      ShiftPressedMouseState mouseState = new ShiftPressedMouseState(container);
      return mouseState.mouseMoved(e);
    }
    else {
      for (Selectable selected : currentlySelected) {
        selected.unSelect();
      }
      currentlySelected.clear();
    }
    selectable.select();
    container.setLastSeletected(selectable);
    return new PressedMouseState(container, selectable, e.getX(), e.getY());
  }

}
