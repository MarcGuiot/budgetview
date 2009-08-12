package org.designup.picsou.gui.components.charts.histo.painters;

import org.designup.picsou.gui.components.charts.histo.HistoDataset;
import org.designup.picsou.gui.description.Formatting;

import java.util.ArrayList;
import java.util.List;

public class HistoDiffDataset implements HistoDataset {

  private List<Element> elements = new ArrayList<Element>();
  private double maxPositive = 0;
  private double maxNegative = 0;

  public int size() {
    return elements.size();
  }

  public double getMaxPositiveValue() {
    return maxPositive;
  }

  public double getMaxNegativeValue() {
    return maxNegative;
  }

  public int getId(int index) {
    return elements.get(index).id;
  }

  public String getLabel(int index) {
    return elements.get(index).label;
  }

  public void add(int id, double reference, double actual, String label, boolean isSelected, boolean isFuture) {
    this.elements.add(new Element(id, label, reference, actual, isSelected, isFuture));

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
    Double result = elements.get(index).referenceValue;
    if (result == null) {
      return 0.0;
    }
    return result;
  }

  public Double getActualValue(int index) {
    Double result = elements.get(index).actualValue;
    if (result == null) {
      return 0.0;
    }
    return result;
  }

  public boolean isFuture(int index) {
    return elements.get(index).future;
  }

  public boolean isSelected(int index) {
    return elements.get(index).selected;
  }

  public String toString() {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < elements.size(); i++) {
      Element element = elements.get(i);
      builder
        .append(Formatting.TWO_DIGIT_INTEGER_FORMAT.format(i))
        .append(": ")
        .append(element.label)
        .append(" - ")
        .append(element.referenceValue)
        .append(" / ")
        .append(element.actualValue);
      if (element.selected) {
        builder.append(" - selected");
      }
      if (element.future) {
        builder.append(" - future");
      }
      builder.append("\n");
    }
    return builder.toString();
  }

  private class Element {
    private int id;
    final String label;
    final double referenceValue;
    final double actualValue;
    final boolean selected;
    final boolean future;

    private Element(int id, String label, double referenceValue, double actualValue, boolean selected, boolean future) {
      this.id = id;
      this.label = label;
      this.referenceValue = referenceValue;
      this.actualValue = actualValue;
      this.selected = selected;
      this.future = future;
    }
  }
}