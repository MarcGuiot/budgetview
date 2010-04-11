package org.designup.picsou.gui.components.charts.histo.painters;

import org.designup.picsou.gui.components.charts.histo.HistoDataset;
import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.utils.Lang;
import org.globsframework.utils.Strings;

import java.util.ArrayList;
import java.util.List;

public class HistoDiffDataset implements HistoDataset {

  private List<Element> elements = new ArrayList<Element>();
  private double maxPositive = 0;
  private double maxNegative = 0;
  private boolean containsSections = false;

  private String tooltipKey;

  public HistoDiffDataset(String tooltipKey) {
    this.tooltipKey = tooltipKey;
  }

  public void add(int id, double reference, double actual, String label, String tooltipLabel, String section, boolean isSelected, boolean isFuture) {
    this.elements.add(new Element(id, label, tooltipLabel, section, reference, actual, isSelected, isFuture));
    this.containsSections |= Strings.isNotEmpty(section);
    updateMax(reference);
    updateMax(actual);
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

  public int getId(int index) {
    return elements.get(index).id;
  }

  public String getLabel(int index) {
    return elements.get(index).label;
  }

  public String getTooltip(int index) {
    if ((index < 0) || (index >= elements.size())) {
      return "";
    }
    return Lang.get(tooltipKey,
                    elements.get(index).tooltipLabel,
                    Formatting.toString(getReferenceValue(index)),
                    Formatting.toString(getActualValue(index)));
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

  public boolean containsSections() {
    return containsSections;
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
    final int id;
    final String label;
    final String tooltipLabel;
    final String section;
    final double referenceValue;
    final double actualValue;
    final boolean selected;
    final boolean future;

    private Element(int id,
                    String label, String tooltipLabel, String section,
                    double referenceValue, double actualValue,
                    boolean selected, boolean future) {
      this.id = id;
      this.label = label;
      this.tooltipLabel = tooltipLabel;
      this.section = section;
      this.referenceValue = referenceValue;
      this.actualValue = actualValue;
      this.selected = selected;
      this.future = future;
    }
  }
}