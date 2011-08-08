package org.designup.picsou.gui.components.charts.histo.utils;

import org.globsframework.model.Key;
import org.globsframework.utils.exceptions.InvalidState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HorizontalBlocksClickMap {

  private int minY;
  private int maxY;
  private List<Element> elements = new ArrayList<Element>();
  private boolean completed;
  private int maxX;

  public void reset(int minY, int maxY) {
    this.minY = minY;
    this.maxY = maxY;
    elements.clear();
    completed = false;
  }

  public void add(Key key, int x) {
    if (completed) {
      throw new InvalidState("clickMap is complete - no new keys can be added");
    }
    elements.add(new Element(x, key));
  }

  public void complete(int maxX) {
    this.maxX = maxX;
    Collections.sort(elements);
  }

  public Key getKey(int x, int y) {
    if (elements.isEmpty()) {
      return null;
    }

    if ((y < minY) || (y > maxY) || (x < elements.get(0).x) || (x > maxX)) {
      return null;
    }
    int index = Collections.binarySearch(elements, new Element(x, null));
    if (index >= 0) {
      return elements.get(index).key;
    }
    else {
      return elements.get(-index - 2).key;
    }
  }

  private static class Element implements Comparable<Element> {
    public final int x;
    public final Key key;

    private Element(int x, Key key) {
      this.x = x;
      this.key = key;
    }

    public int compareTo(Element other) {
      return x - other.x;
    }

    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      Element element = (Element)o;
      if (x != element.x) {
        return false;
      }

      return true;
    }

    public int hashCode() {
      return x;
    }
  }
}
