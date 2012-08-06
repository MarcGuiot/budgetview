package org.designup.picsou.model.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AmountMap {
  private Map<Integer, Double> amounts = new HashMap<Integer, Double>();

  public void setMax(Integer id, Double newValue) {
    Double previousValue = amounts.get(id);
    if ((previousValue == null) || (previousValue < newValue)) {
      amounts.put(id, newValue);
    }
  }

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

  public Set<Map.Entry<Integer,Double>> entrySet() {
    return amounts.entrySet();
  }
}
