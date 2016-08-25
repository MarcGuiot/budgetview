package com.budgetview.shared.utils;

import com.budgetview.shared.mobile.model.SeriesValues;
import org.globsframework.model.Glob;

import java.util.Comparator;

public class SeriesValuesComparator implements Comparator<Glob> {
  public int compare(Glob seriesValues1, Glob seriesValues2) {
    if (seriesValues1 == null && seriesValues2 == null) {
      return 0;
    }
    if (seriesValues1 == null) {
      return -1;
    }
    if (seriesValues2 == null) {
      return 1;
    }
    double g1Value = Math.max(Math.abs(seriesValues1.get(SeriesValues.PLANNED_AMOUNT, 0.00)),
                              Math.abs(seriesValues1.get(SeriesValues.AMOUNT, 0.00)));
    double g2Value = Math.max(Math.abs(seriesValues2.get(SeriesValues.PLANNED_AMOUNT, 0.00)),
                              Math.abs(seriesValues2.get(SeriesValues.AMOUNT, 0.00)));
    int valueDiff = Double.compare(g1Value, g2Value) * -1;
    if (valueDiff != 0) {
      return valueDiff;
    }

    return seriesValues1.get(SeriesValues.SERIES_ENTITY).compareTo(
      seriesValues2.get(SeriesValues.SERIES_ENTITY));
  }
}
