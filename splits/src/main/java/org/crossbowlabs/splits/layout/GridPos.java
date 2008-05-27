package org.crossbowlabs.splits.layout;

public class GridPos {
  private Integer x;
  private Integer y;
  private Integer w;
  private Integer h;

  public GridPos(Integer x, Integer y, Integer w, Integer h) {
    this.x = x;
    this.y = y;
    this.w = w;
    this.h = h;
  }

  public Integer getX() {
    return x;
  }

  public Integer getY() {
    return y;
  }

  public Integer getW() {
    return w;
  }

  public Integer getH() {
    return h;
  }

  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final GridPos gridPos = (GridPos)o;

    if (!x.equals(gridPos.x)) {
      return false;
    }
    if (!y.equals(gridPos.y)) {
      return false;
    }
    if (!h.equals(gridPos.h)) {
      return false;
    }
    if (!w.equals(gridPos.w)) {
      return false;
    }

    return true;
  }

  public int hashCode() {
    int result;
    result = x.hashCode();
    result = 29 * result + y.hashCode();
    result = 29 * result + w.hashCode();
    result = 29 * result + h.hashCode();
    return result;
  }

  public String toString() {
    return "(" + x + "," + y + "," + w + "," + h + ")";
  }
}
