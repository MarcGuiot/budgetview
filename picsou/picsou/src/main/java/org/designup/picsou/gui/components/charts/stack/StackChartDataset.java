package org.designup.picsou.gui.components.charts.stack;

import org.globsframework.utils.exceptions.InvalidParameter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StackChartDataset {

  private List<Element> elements = new ArrayList<Element>();
  private double total = 0.0;
  private String longestLabel = "";
  private int multiplier = 1;

  public void add(String label, Double value) {
    if ((value == null) || Math.abs(value) < 0.01) {
      return;
    }

    double adjustedValue = value * multiplier;

    Element element = new Element(label, adjustedValue);
    int index = Collections.binarySearch(elements, element);
    elements.add(index < 0 ? -index - 1 : index, element);
    total += adjustedValue;
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

  public void setInverted(boolean inverted) {
    multiplier = inverted ? -1 : 1;
  }

  public String toString() {
    StringBuilder builder = new StringBuilder();
    for (Element element : elements) {
       builder
         .append(element.label)
         .append(":")
         .append(element.value)
         .append("\n");   
    }
    return builder.toString();
  }

  public int indexOf(String label) {
    int index = 0;
    for (Element element : elements) {
      if (element.label.equals(label)) {
        return index;
      }
      index++;
    }
    return -1;
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
