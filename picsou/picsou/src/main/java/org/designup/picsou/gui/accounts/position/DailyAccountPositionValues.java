package org.designup.picsou.gui.accounts.position;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class DailyAccountPositionValues {

  private SortedMap<Integer, MonthData> monthData = new TreeMap<Integer, MonthData>();

  public interface Functor {
    public void processPositions(int monthId, Double[] minValues, boolean monthSelected, boolean[] daysSelected);
  }

  public void apply(Functor functor) {
    for (Map.Entry<Integer, MonthData> entry : monthData.entrySet()) {
      MonthData data = entry.getValue();
      functor.processPositions(entry.getKey(), data.minValues, data.monthSelected, data.daysSelected);
    }
  }

  private class MonthData {
    Double[] minValues;
    boolean monthSelected;
    boolean[] daysSelected;

    private MonthData(Double[] minValues, boolean monthSelected, boolean[] daysSelected) {
      this.minValues = minValues;
      this.monthSelected = monthSelected;
      this.daysSelected = daysSelected;
    }
  }

  void add(int monthId, Double[] minValues, boolean monthSelected, boolean[] daysSelected) {
    monthData.put(monthId, new MonthData(minValues, monthSelected, daysSelected));
  }
}
