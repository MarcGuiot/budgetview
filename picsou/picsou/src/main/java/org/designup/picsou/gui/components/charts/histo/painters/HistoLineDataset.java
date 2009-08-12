package org.designup.picsou.gui.components.charts.histo.painters;

import org.designup.picsou.gui.components.charts.histo.HistoDataset;

import java.util.ArrayList;
import java.util.List;

public class HistoLineDataset implements HistoDataset {

  private double maxPositive = 0;
  private double maxNegative = 0;

  private List<Element> elements = new ArrayList<Element>();

  public int size() {
    return elements.size();
  }

  public double getMaxPositiveValue() {
    return maxPositive;
  }

  public double getMaxNegativeValue() {
    return maxNegative;
  }

  public String getLabel(int index) {
    return elements.get(index).label;
  }

  public void add(int id, double value, String label) {
    this.elements.add(new Element(id, label, value));

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

  public int getId(int index) {
    return elements.get(index).id;
  }

  public Double getValue(int index) {
    Double result = elements.get(index).value;
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
    for (Element element : elements) {
      builder
        .append("[")
        .append(element.id)
        .append("] ")
        .append(element.label)
        .append(": ")
        .append(element.value)
        .append("\n");
    }
    return builder.toString();
  }

  private class Element {
    final int id;
    final String label;
    final double value;

    private Element(int id, String label, double value) {
      this.id = id;
      this.label = label;
      this.value = value;
    }
  }
}