package org.designup.picsou.gui.time;

import org.designup.picsou.gui.time.selectable.ChainedSelectableElement;
import org.designup.picsou.gui.time.selectable.Selectable;

public abstract class DefaultCompositeComponent extends AbstractComponent implements Selectable, Comparable<YearGraph> {
  protected ChainedSelectableElement innerElements;
  private AbstractComponent[] components;

  public DefaultCompositeComponent(ChainedSelectableElement innerElements, ChainedSelectableElement yearElement) {
    super(yearElement);
    this.innerElements = innerElements;
  }

  public void add(AbstractComponent[] components) {
    this.components = components;
  }

  public Selectable getSelectable(int x, int y) {
    Selectable selectable;
    if (clickableAreaTop != null && clickableAreaTop.contains(x, y)) {
      for (Selectable month : components) {
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
      return components[index - 1];
    }

    public Selectable getRight() {
      if (index == components.length - 1) {
        return innerElements.getRight();
      }
      return components[index + 1];
    }
  }
}
