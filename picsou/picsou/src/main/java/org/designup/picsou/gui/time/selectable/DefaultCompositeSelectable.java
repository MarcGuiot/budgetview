package org.designup.picsou.gui.time.selectable;

import org.designup.picsou.gui.time.selectable.ChainedSelectableElement;
import org.designup.picsou.gui.time.selectable.Selectable;
import org.designup.picsou.gui.time.YearGraph;

public abstract class DefaultCompositeSelectable extends AbstractSelectable implements Selectable, Comparable<YearGraph> {
  protected ChainedSelectableElement innerElements;
  private AbstractSelectable[] selectables;

  public DefaultCompositeSelectable(ChainedSelectableElement innerElements, ChainedSelectableElement yearElement) {
    super(yearElement);
    this.innerElements = innerElements;
  }

  public void add(AbstractSelectable[] selectables) {
    this.selectables = selectables;
  }

  public Selectable getSelectable(int x, int y) {
    Selectable selectable;
    if (clickableAreaTop != null && clickableAreaTop.contains(x, y)) {
      for (Selectable month : selectables) {
        selectable = month.getSelectable(x, y);
        if (selectable != null) {
          return selectable;
        }
      }
      return this;
    }
    return null;
  }


  protected class DefaultChainedSelectableElement implements ChainedSelectableElement {
    private int index;

    public DefaultChainedSelectableElement(int index) {
      this.index = index;
    }

    public Selectable getLeft() {
      if (index == 0) {
        return innerElements.getLeft();
      }
      return selectables[index - 1];
    }

    public Selectable getRight() {
      if (index == selectables.length - 1) {
        return innerElements.getRight();
      }
      return selectables[index + 1];
    }
  }
}
