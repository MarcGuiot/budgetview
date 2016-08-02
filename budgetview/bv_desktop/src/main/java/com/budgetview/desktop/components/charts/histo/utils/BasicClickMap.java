package com.budgetview.desktop.components.charts.histo.utils;

import org.globsframework.model.Key;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class BasicClickMap {
  private Map<Rectangle, Key> areas = new HashMap<Rectangle, Key>();

  public void add(Rectangle rectangle, Key key) {
    areas.put(rectangle, key);
  }

  public Key getKey(int x, int y) {
    for (Rectangle rectangle : areas.keySet()) {
      if (rectangle.contains(x, y)) {
        return areas.get(rectangle);
      }
    }
    return null;
  }

  public void reset() {
    areas.clear();
  }
}
