package org.designup.picsou.gui.time;

import org.designup.picsou.gui.time.selectable.ChainedSelectableElement;
import org.designup.picsou.gui.time.selectable.Selectable;

import java.awt.*;

public abstract class AbstractComponent implements Selectable {
  protected boolean selected = false;
  protected ChainedSelectableElement element;
  protected Rectangle clickableAreaTop = new Rectangle();
  protected Visibility isVisible = Visibility.NOT_VISIBLE;

  public AbstractComponent(ChainedSelectableElement element) {
    this.element = element;
  }

  public Selectable getSelectable(int x, int y) {
    if (clickableAreaTop != null && clickableAreaTop.contains(x, y)) {
      return this;
    }
    return null;
  }

  public void select() {
    selected = true;
  }

  public void unSelect() {
    selected = false;
  }

  public void inverseSelect() {
    selected = !selected;
  }

  public Visibility getVisibility() {
    return isVisible;
  }

  public Selectable getLeft() {
    return element.getLeft();
  }

  public Selectable getRight() {
    return element.getRight();
  }

  public void setNotVisible() {
    isVisible = Visibility.NOT_VISIBLE;
  }

  public void getSelected(java.util.List<Selectable> list) {
    if (selected) {
      list.add(this);
    }
  }

  // for testing
  public Rectangle getClickableArea() {
    return clickableAreaTop;
  }
}
