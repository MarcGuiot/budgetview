package org.designup.picsou.gui.components;

public final class StackChartElement implements Comparable<StackChartElement> {
  private String label;
  private double value;
  private boolean selected;

  public StackChartElement(String label, Double value, boolean selected) {
    this.label = label;
    this.value = value != null ? value : 0;
    this.selected = selected;
  }

  public String getLabel() {
    return label;
  }

  public boolean isSelected() {
    return selected;
  }

  public double getValue() {
    return value;
  }

  // Reversed
  public int compareTo(StackChartElement other) {
    return Double.compare(other.value, value);
  }

  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    StackChartElement that = (StackChartElement)o;

    if (selected != that.selected) {
      return false;
    }
    if (Double.compare(that.value, value) != 0) {
      return false;
    }
    if (label != null ? !label.equals(that.label) : that.label != null) {
      return false;
    }

    return true;
  }

  public int hashCode() {
    int result;
    long temp;
    result = (label != null ? label.hashCode() : 0);
    temp = value != +0.0d ? Double.doubleToLongBits(value) : 0L;
    result = 31 * result + (int)(temp ^ (temp >>> 32));
    result = 31 * result + (selected ? 1 : 0);
    return result;
  }
}
