package org.designup.picsou.gui.components.charts.histo.painters;

import org.designup.picsou.gui.components.charts.histo.HistoDataset;
import org.designup.picsou.gui.description.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.text.NumberFormat;
import java.text.DecimalFormat;

public class HistoDiffDataset implements HistoDataset {

  private double maxPositive = 0;
  private double maxNegative = 0;

  private List<String> labels = new ArrayList<String>();
  private List<Double> referenceValues = new ArrayList<Double>();
  private List<Double> actualValues = new ArrayList<Double>();
  private List<Boolean> selected = new ArrayList<Boolean>();
  private List<Boolean> future = new ArrayList<Boolean>();

  public int getSize() {
    return labels.size();
  }

  public double getMaxPositiveValue() {
    return maxPositive;
  }

  public double getMaxNegativeValue() {
    return maxNegative;
  }

  public String getLabel(int index) {
    return labels.get(index);
  }

  public void add(double reference, double actual, String label, boolean isSelected, boolean isFuture) {
    this.referenceValues.add(reference);
    this.actualValues.add(actual);
    this.labels.add(label);
    this.selected.add(isSelected);
    this.future.add(isFuture);

    updateMax(reference);
    updateMax(actual);
  }

  private void updateMax(double value) {
    if ((value > 0) && (value > maxPositive)) {
      maxPositive = value;
    }
    else if ((value < 0) && (-value > maxNegative)) {
      maxNegative = -value;
    }
  }

  public Double getReferenceValue(int index) {
    Double result = referenceValues.get(index);
    if (result == null) {
      return 0.0;
    }
    return result;
  }

  public Double getActualValue(int index) {
    Double result = actualValues.get(index);
    if (result == null) {
      return 0.0;
    }
    return result;
  }

  public boolean isFuture(int index) {
    return future.get(index);
  }

  public boolean isSelected(int index) {
    return selected.get(index);
  }

  public String toString() {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < labels.size(); i++) {
      builder
        .append(Formatting.TWO_DIGIT_INTEGER_FORMAT.format(i))
        .append(": ")
        .append(labels.get(i))
        .append(" - ")
        .append(referenceValues.get(i))
        .append(" / ")
        .append(actualValues.get(i));
      if (isSelected(i)) {
        builder.append(" - selected");
      }
      if (isFuture(i)) {
        builder.append(" - future");
      }
      builder.append("\n");
    }
    return builder.toString();
  }
}