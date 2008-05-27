package org.designup.picsou.gui.time.selectable;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.*;

public class AbstractPressedState extends AbstractMouseState {
  protected Selectable firstSelected;
  protected int x;
  protected int y;
  protected java.util.List<Selectable> currentSelectable = new ArrayList<Selectable>();

  public AbstractPressedState(SelectableContainer container) {
    super(container);
  }

  private Selectable updateSelectable(MouseEvent e, java.util.List<Selectable> selected) {
    Selectable selectable = getSelectable(e.getX(), e.getY());
    return findNearestSelectable(container.getLastSelected(), selectable, selected);
  }

  static public Selectable findNearestSelectable(Selectable currentlySelected, Selectable selectable, List<Selectable> selected) {
    if (selectable == null || currentlySelected == null) {
      return null;
    }
    List<Selectable> leftPosibleSelection = search(currentlySelected, selectable, new Next() {
      public Selectable next(Selectable selectable) {
        return selectable.getLeft();
      }
    });
    List<Selectable> rightPosibleSelection = search(currentlySelected, selectable, new Next() {
      public Selectable next(Selectable selectable) {
        return selectable.getRight();
      }
    });
    if (rightPosibleSelection.isEmpty() && leftPosibleSelection.isEmpty()) {
      return null;
    }
    if (rightPosibleSelection.size() > leftPosibleSelection.size()) {
      selected.addAll(rightPosibleSelection);
    }
    else {
      selected.addAll(leftPosibleSelection);
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
    container.setLastSeletected(lastSelected);

    int removeCount = 0;
    Iterator<Selectable> listIterator = selected.iterator();
    listIterator.next();
    ListIterator<Selectable> listIterator1 = currentSelectable.listIterator(currentSelectable.size());
    listIterator1.previous();
    while (listIterator.hasNext() && listIterator1.hasPrevious()) {
      Selectable newSelected = listIterator.next();
      if (newSelected.equals(firstSelected)) {
        removeCount++;
        break;
      }
      if (newSelected.equals(listIterator1.previous())) {
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

    for (Selectable tmp : currentSelectable) {
      tmp.select();
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
        Selectable selectable1 = selectable.getLeft();
        if (selectable1 != null) {
          Set<Selectable> selectables = getCurrentlySelected();
          for (Selectable selectable2 : selectables) {
            selectable2.unSelect();
          }
          selectables.clear();
          selectable1.select();
          selectables.add(selectable1);
        }
      }
    }
    return this;
  }
}
