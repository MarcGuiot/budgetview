package com.budgetview.gui.time.mousestates;

import com.budgetview.gui.time.selectable.Selectable;
import com.budgetview.gui.time.selectable.SelectableContainer;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.*;

public class AbstractPressedState extends AbstractMouseState {
  protected Selectable firstSelected;
  protected java.util.List<Selectable> currentSelectable = new ArrayList<Selectable>();

  public AbstractPressedState(SelectableContainer container) {
    super(container);
  }

  private Selectable updateSelectable(MouseEvent e, java.util.List<Selectable> selected) {
    Selectable selectable = getSelectable(e.getX(), e.getY());
    return findNearestSelectable(container.getLastSelected(), selectable, selected);
  }

  static private Selectable findNearestSelectable(Selectable currentlySelected, Selectable selectable, List<Selectable> selected) {
    if (selectable == null || currentlySelected == null) {
      return null;
    }
    List<Selectable> leftPossibleSelection = search(currentlySelected, selectable, new Next() {
      public Selectable next(Selectable selectable) {
        return selectable.getLeft();
      }
    });
    List<Selectable> rightPosibleSelection = search(currentlySelected, selectable, new Next() {
      public Selectable next(Selectable selectable) {
        return selectable.getRight();
      }
    });
    if (rightPosibleSelection.isEmpty() && leftPossibleSelection.isEmpty()) {
      return null;
    }
    if (rightPosibleSelection.size() > leftPossibleSelection.size()) {
      selected.addAll(rightPosibleSelection);
    }
    else {
      selected.addAll(leftPossibleSelection);
    }
    return selectable;
  }

  interface Next {
    Selectable next(Selectable selectable);
  }

  static private List<Selectable> search(Selectable currentlySelected, Selectable selectable, Next nextFunctor) {
    List<Selectable> posibleSelection = new ArrayList<Selectable>();
    Selectable next = nextFunctor.next(currentlySelected);
    posibleSelection.add(next);
    while (next != null && next != selectable) {
      posibleSelection.add(next);
      next = nextFunctor.next(next);
    }
    if (next == null) {
      posibleSelection.clear();
    }
    else {
      posibleSelection.add(selectable);
    }
    return posibleSelection;
  }

  public MouseState mouseMoved(MouseEvent e) {
    java.util.List<Selectable> selected = new ArrayList<Selectable>();
    Selectable lastSelected = updateSelectable(e, selected);
    if (lastSelected == null) {
      return this;
    }
    container.setLastSelected(lastSelected);

    int removeCount = 0;
    Iterator<Selectable> listIterator = selected.iterator();
    listIterator.next();
    ListIterator<Selectable> reverseIterator = currentSelectable.listIterator(currentSelectable.size());
    reverseIterator.previous();
    while (listIterator.hasNext() && reverseIterator.hasPrevious()) {
      Selectable newSelected = listIterator.next();
      if (newSelected.equals(firstSelected)) {
        removeCount++;
        break;
      }
      if (newSelected.equals(reverseIterator.previous())) {
        removeCount++;
      }
      else {
        break;
      }
    }
    if (removeCount >= 1) {
      ListIterator<Selectable> listIterator2 = currentSelectable.listIterator(currentSelectable.size());
      while (removeCount != 0) {
        listIterator2.previous().unSelect();
        listIterator2.remove();
        selected.remove(0);
        removeCount--;
      }
    }
    selected.remove(0);
    currentSelectable.addAll(selected);

    for (Selectable selectable : currentSelectable) {
      selectable.select();
    }
    return this;
  }

  public MouseState mouseReleased(MouseEvent e) {
    getCurrentlySelected().addAll(currentSelectable);
    return new ReleasedMouseState(container);
  }

  public MouseState keyPressed(KeyEvent e) {
    if (e.getKeyCode() == KeyEvent.VK_LEFT) {
      Selectable selectable = container.getLastSelected();
      if (selectable != null) {
        Selectable newSelectable = selectable.getLeft();
        if (newSelectable != null) {
          Set<Selectable> selectables = getCurrentlySelected();
          for (Selectable previouslySelected : selectables) {
            previouslySelected.unSelect();
          }
          selectables.clear();
          newSelectable.select();
          selectables.add(newSelectable);
        }
      }
    }
    return this;
  }
}
