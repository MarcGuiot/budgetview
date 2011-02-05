package org.designup.picsou.gui.components.charts.histo.utils;

import java.util.ArrayList;
import java.util.List;

public class ScrollGroup {
  private List<Scrollable> elements = new ArrayList<Scrollable>();

  public void add(Scrollable element) {
    elements.add(element);
  }

  public void scroll(int units) {
    for (Scrollable element : elements) {
      element.scroll(units);
    }
  }
}
