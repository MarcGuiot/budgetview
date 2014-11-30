package org.designup.picsou.triggers.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

public class SeriesAndMonths {
  private Map<Integer, TreeSet<Integer>> elements;

  public interface Functor {
    void apply(Integer seriesId, Integer monthId);
  }

  public void add(Integer seriesId, Integer monthId) {
    TreeSet<Integer> months = getElements().get(seriesId);
    if (months == null) {
      months = new TreeSet<Integer>();
      elements.put(seriesId, months);
    }
    months.add(monthId);
  }

  public boolean isEmpty() {
    return elements == null || elements.isEmpty();
  }

  public void apply(Functor functor) {
    for (Map.Entry<Integer, TreeSet<Integer>> entry : elements.entrySet()) {
      Integer seriesId = entry.getKey();
      for (Integer monthId : entry.getValue()) {
        functor.apply(seriesId, monthId);
      }
    }
  }

  private Map<Integer, TreeSet<Integer>> getElements() {
    if (elements == null) {
      elements = new HashMap<Integer, TreeSet<Integer>>();
    }
    return elements;
  }
}
