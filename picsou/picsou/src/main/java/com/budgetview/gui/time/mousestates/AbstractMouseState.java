package com.budgetview.gui.time.mousestates;

import com.budgetview.gui.time.selectable.Selectable;
import com.budgetview.gui.time.selectable.SelectableContainer;

import java.awt.event.MouseEvent;
import java.util.Set;

class AbstractMouseState implements MouseState {
  protected SelectableContainer container;

  public AbstractMouseState(SelectableContainer container) {
    this.container = container;
  }

  public MouseState mousePressed(MouseEvent e) {
    return this;
  }

  public MouseState mouseReleased(MouseEvent e) {
    return this;
  }

  public MouseState mouseMoved(MouseEvent e) {
    return this;
  }

  protected Set<Selectable> getCurrentlySelected() {
    return container.getCurrentlySelectedToUpdate();
  }

  protected Selectable getSelectable(int x, int y) {
    return container.getSelectable(x, y);
  }
}
