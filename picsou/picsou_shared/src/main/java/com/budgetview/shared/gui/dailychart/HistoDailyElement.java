package com.budgetview.shared.gui.dailychart;

import com.budgetview.shared.gui.histochart.HistoDatasetElement;
import org.globsframework.utils.exceptions.InvalidParameter;

import java.util.Arrays;

public class HistoDailyElement extends HistoDatasetElement {

  public final Double[] values;
  public final int minDay;
  public final boolean[] daySelections;

  public HistoDailyElement(int id, Double[] values, String label, String section, String tooltip, int minDay, boolean current, boolean future, boolean selected, boolean[] daySelections) {
    super(id, label, tooltip, section, current, future, selected);

    if (values.length != daySelections.length) {
      throw new InvalidParameter("All arrays should have the same length");
    }

    this.values = values;
    this.minDay = minDay;
    this.daySelections = daySelections;
  }

  public Double getLastValue() {
    return values[values.length - 1];
  }

  public String toString() {
    return id + " " + super.toString() + "==>" + Arrays.toString(values);
  }
}
