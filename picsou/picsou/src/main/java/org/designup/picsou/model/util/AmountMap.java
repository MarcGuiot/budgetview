package org.designup.picsou.model.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AmountMap {
  private Map<Integer, Double> amounts = new HashMap<Integer, Double>();

  public void add(int id, Double amount) {
    if (amount == null) {
      return;
    }
    Double previousAmount = amounts.get(id);
    if (previousAmount == null) {
      previousAmount = 0.00;
    }
    amounts.put(id, previousAmount + amount);
  }

  public Set<Integer> keySet() {
    return amounts.keySet();
  }

  public Double get(Integer id) {
    return amounts.get(id);
  }

  public double get(Integer id, Double defaultValue) {
    Double value = get(id);
    return value != null ? amounts.get(id) : defaultValue;
  }

  public Set<Map.Entry<Integer, Double>> entrySet() {
    return amounts.entrySet();
  }

  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("[");
    boolean first = true;
    for (Map.Entry<Integer, Double> entry : amounts.entrySet()) {
      if (!first) {
        builder.append(", ");
      }
      builder.append(entry.getKey())
        .append(":")
        .append(entry.getValue());
      first = false;
    }
    builder.append("]");
    return builder.toString();
  }

  public boolean isEmpty() {
    return amounts.isEmpty();
  }
}
