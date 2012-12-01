package org.designup.picsou.gui.components.charts.stack;

import org.designup.picsou.gui.description.Formatting;
import org.globsframework.model.Key;
import org.globsframework.utils.exceptions.InvalidParameter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StackChartDataset {

  private List<Element> elements = new ArrayList<Element>();
  private double total = 0.0;
  private String longestLabel = "";
  private boolean containsSelection;

  public void add(String label, Double value) {
    add(label, value, null, false);
  }

  public void add(String label, Double value, Key key) {
    add(label, value, key, false);
  }

  public void add(String label, Double value, Key key, boolean selected) {
    if ((value == null) || Math.abs(value) < 0.01) {
      return;
    }
    if (value < 0) {
      throw new InvalidParameter("Invalid negative value " + value + " for " + label);
    }

    Element element = new Element(label, value, key, selected);
    int index = Collections.binarySearch(elements, element);
    elements.add(index < 0 ? -index - 1 : index, element);
    total += value;
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

  public Key getKey(int index) {
    return elements.get(index).key;
  }

  public String getTooltipText(int index) {
    return Formatting.toString(elements.get(index).value);
  }

  private static class Element implements Comparable<Element> {
    String label;
    double value;
    private Key key;
    boolean selected;

    public Element(String label, double value, Key key, boolean selected) {
      this.label = label;
      this.value = value;
      this.key = key;
      this.selected = selected;
    }

    public int compareTo(Element other) {
      int cmp = Double.compare(other.value, value);
      if (cmp == 0) {
        return other.label.compareTo(label);
      }
      return cmp;
    }


  }
}
