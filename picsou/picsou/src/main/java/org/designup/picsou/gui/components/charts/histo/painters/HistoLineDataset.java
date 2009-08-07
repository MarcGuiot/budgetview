package org.designup.picsou.gui.components.charts.histo.painters;

import org.designup.picsou.gui.components.charts.histo.HistoDataset;

import java.util.ArrayList;
import java.util.List;

public class HistoLineDataset implements HistoDataset {

  private double maxPositive = 0;
  private double maxNegative = 0;

  private List<String> labels = new ArrayList<String>();
  private List<Double> values = new ArrayList<Double>();

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

  public void add(double value, String label) {
    this.values.add(value);
    this.labels.add(label);

    updateMax(value);
  }

  private void updateMax(double value) {
    if ((value > 0) && (value > maxPositive)) {
      maxPositive = value;
    }
    else if ((value < 0) && (-value > maxNegative)) {
      maxNegative = -value;
    }
  }

  public Double getValue(int index) {
    Double result = values.get(index);
    if (result == null) {
      return 0.0;
    }
    return result;
  }

  public boolean isSelected(int index) {
    return false;
  }

  public String toString() {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < labels.size(); i++) {
      builder.append(labels.get(i) + ": " + values.get(i) + "\n");
    }
    return builder.toString();
  }
}