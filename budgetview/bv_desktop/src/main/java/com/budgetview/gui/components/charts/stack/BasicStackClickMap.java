package com.budgetview.gui.components.charts.stack;

import org.globsframework.model.Key;
import org.globsframework.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class BasicStackClickMap {

  private Map<Rectangle, Item> clickAreas = new HashMap<Rectangle, Item>();

  public void clear() {
    clickAreas.clear();
  }

  public void put(Rectangle rectangle, Key key, String label) {
    clickAreas.put(rectangle, new Item(key, label));
  }

  public Rectangle getArea(String label) {
    for (Map.Entry<Rectangle, Item> entry : clickAreas.entrySet()) {
      if (Utils.equal(label, entry.getValue().label)) {
        return entry.getKey();
      }
    }
    return null;
  }

  public Key getKeyAt(int x, int y) {
    for (Map.Entry<Rectangle, Item> entry : clickAreas.entrySet()) {
      if (entry.getKey().contains(x, y)) {
        return entry.getValue().key;
      }
    }
    return null;
  }

  public List<String> getAllLabels() {
    List<String> result = new ArrayList<String>();
    for (Map.Entry<Rectangle, Item> entry : clickAreas.entrySet()) {
      result.add(entry.getValue().label);
    }
    return result;
  }

  private class Item {
    final Key key;
    final String label;

    private Item(Key key, String label) {
      this.key = key;
      this.label = label;
    }

    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      Item item = (Item)o;

      if (key != null ? !key.equals(item.key) : item.key != null) {
        return false;
      }
      if (label != null ? !label.equals(item.label) : item.label != null) {
        return false;
      }

      return true;
    }

    public int hashCode() {
      int result = key != null ? key.hashCode() : 0;
      result = 31 * result + (label != null ? label.hashCode() : 0);
      return result;
    }
  }
}
