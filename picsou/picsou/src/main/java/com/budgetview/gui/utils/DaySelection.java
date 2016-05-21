package com.budgetview.gui.utils;

import org.globsframework.utils.exceptions.OperationDenied;

import java.util.HashMap;
import java.util.Map;

public class DaySelection {

  public static final DaySelection EMPTY = new DaySelection() {
    public void add(int monthId, int day) {
      throw new OperationDenied("Cannot modify constant value");
    }
  };

  private Map<Integer, boolean[]> selection = new HashMap<Integer, boolean[]>();

  public void add(int monthId, int day) {
    boolean[] values = selection.get(monthId);
    if (values == null) {
      values = new boolean[32];
    }
    values[day - 1] = true;
    selection.put(monthId, values);
  }

  public boolean[] getValues(int monthId, int maxDays) {
    boolean[] values = selection.get(monthId);
    boolean[] result = new boolean[maxDays];
    if (values != null) {
      System.arraycopy(values, 0, result, 0, maxDays);
    }
    return result;
  }

}
