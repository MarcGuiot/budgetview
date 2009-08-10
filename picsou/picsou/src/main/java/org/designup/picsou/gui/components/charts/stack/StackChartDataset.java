package org.designup.picsou.gui.components.charts.stack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StackChartDataset {

  private List<Element> elements = new ArrayList<Element>();
  private double total = 0.0;
  private String longestLabel = "";

  public void add(String label, double value) {
    Element element = new Element(label, value);
    int index = Collections.binarySearch(elements, element);
    elements.add(index < 0 ? -index - 1 : index, element);
    total += value;
    if (label.length() > longestLabel.length()) {
      longestLabel = label;
    }
  }

  public String getLabel(int index) {
    return elements.get(index).label;
  }

  public double getValue(int index) {
    return elements.get(index).value;
  }

  public double getTotal() {
    return total;
  }

  public int size() {
    return elements.size();
  }

  public boolean isEmpty() {
    return elements.isEmpty();
  }

  public String getLongestLabel() {
    return longestLabel;
  }

  private static class Element implements Comparable<Element> {
    String label;
    double value;

    public Element(String label, double value) {
      this.label = label;
      this.value = value;
    }

    public int compareTo(Element other) {
      return Double.compare(other.value, value);
    }
  }
}
