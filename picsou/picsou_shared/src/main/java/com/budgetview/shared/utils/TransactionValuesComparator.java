package com.budgetview.shared.utils;

import com.budgetview.shared.model.SeriesValues;
import org.globsframework.model.Glob;

import java.util.Comparator;

public class TransactionValuesComparator implements Comparator<Glob> {
  public int compare(Glob transaction1, Glob transaction2) {
    if (transaction1 == null && transaction2 == null) {
      return 0;
    }
    if (transaction1 == null) {
      return -1;
    }
    if (transaction2 == null) {
      return 1;
    }


    double g1Value = Math.max(Math.abs(transaction1.get(SeriesValues.PLANNED_AMOUNT, 0.00)),
                              Math.abs(transaction1.get(SeriesValues.AMOUNT, 0.00)));
    double g2Value = Math.max(Math.abs(transaction2.get(SeriesValues.PLANNED_AMOUNT, 0.00)),
                              Math.abs(transaction2.get(SeriesValues.AMOUNT, 0.00)));
    int valueDiff = Double.compare(g1Value, g2Value) * -1;
    if (valueDiff != 0) {
      return valueDiff;
    }

    return transaction1.get(SeriesValues.NAME).toLowerCase().compareTo(
      transaction2.get(SeriesValues.NAME).toLowerCase()) * -1;
  }
}
