package org.designup.picsou.gui.components.charts.histo.painters;

import org.designup.picsou.gui.components.charts.histo.HistoDataset;
import org.globsframework.utils.Strings;

import java.util.ArrayList;
import java.util.List;

public class HistoLineDataset implements HistoDataset {

  private double maxPositive = 0;
  private double maxNegative = 0;

  private List<Element> elements = new ArrayList<Element>();
  private boolean containsSections;

  public void add(int id, double value, String label, String section, boolean selected) {
    this.elements.add(new Element(id, label, section, value, selected));
    this.containsSections |= Strings.isNotEmpty(section);
    updateMax(value);
  }

  public int size() {
    return elements.size();
  }

  public boolean isEmpty() {
    return elements.isEmpty();
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

  public String getSection(int index) {
    return elements.get(index).section;
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
    return elements.get(index).selected;
  }

  public boolean containsSections() {
    return containsSections;
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
        .append(element.value);
      if (element.selected) {
        builder.append(" - selected");
      }
      builder.append("\n");
    }
    return builder.toString();
  }

  private class Element {
    final int id;
    final String label;
    final String section;
    final double value;
    private boolean selected;

    private Element(int id, String label, String section, double value, boolean selected) {
      this.id = id;
      this.label = label;
      this.section = section;
      this.value = value;
      this.selected = selected;
    }
  }
}