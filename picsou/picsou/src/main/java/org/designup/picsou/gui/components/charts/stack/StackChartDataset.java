package org.designup.picsou.gui.components.charts.stack;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StackChartDataset {

  private List<Element> elements = new ArrayList<Element>();
  private double total = 0.0;
  private String longestLabel = "";
  private int multiplier = 1;
  private boolean containsSelection = false;

  public void add(String label, Double value, Action action) {
    add(label, value, action, false);
  }

  public void add(String label, Double value, Action action, boolean selected) {
    if ((value == null) || Math.abs(value) < 0.01) {
      return;
    }

    double adjustedValue = value * multiplier;

    Element element = new Element(label, adjustedValue, action, selected);
    int index = Collections.binarySearch(elements, element);
    elements.add(index < 0 ? -index - 1 : index, element);
    total += adjustedValue;
    if (label.length() > longestLabel.length()) {
      longestLabel = label;
    }

    containsSelection |= selected;
  }

  public String getLabel(int index) {
    return elements.get(index).label;
  }

  public double getValue(int index) {
    return elements.get(index).value;
  }

  public boolean isSelected(int index) {
    return elements.get(index).selected;
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

  public boolean containsSelection() {
    return containsSelection;
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
        .append(element.value);
      if (element.selected) {
        builder.append(" - selected");
      }
      builder
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

  public Action getAction(int index) {
    return elements.get(index).action;
  }

  private static class Element implements Comparable<Element> {
    String label;
    double value;
    private Action action;
    boolean selected;

    public Element(String label, double value, Action action, boolean selected) {
      this.label = label;
      this.value = value;
      this.action = action;
      this.selected = selected;
    }

    public int compareTo(Element other) {
      return Double.compare(other.value, value);
    }
  }
}
