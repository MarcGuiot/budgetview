package org.designup.picsou.gui.components.charts.histo.utils;

import org.designup.picsou.gui.components.charts.histo.HistoDataset;
import org.designup.picsou.gui.description.Formatting;
import org.globsframework.utils.Strings;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractHistoDataset<E extends HistoDatasetElement> implements HistoDataset {
  protected List<E> elements = new ArrayList<E>();
  protected double maxPositive = 0;
  protected double maxNegative = 0;
  private boolean containsSections = false;
  private String tooltipKey;

  public AbstractHistoDataset(String tooltipKey) {
    this.tooltipKey = tooltipKey;
  }

  protected void add(E element) {
    elements.add(element);
    this.containsSections |= Strings.isNotEmpty(element.section);
  }

  protected E getElement(int index) {
    return elements.get(index);
  }

  protected String getTooltipKey() {
    return tooltipKey;
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

  public int getId(int monthIndex) {
    if ((monthIndex < 0) || (monthIndex >= elements.size())) {
      return -1;
    }
    return elements.get(monthIndex).id;
  }

  public int getIndex(int id) {
    int index = 0;
    for (E element : elements) {
      if (element.id == id) {
        return index;
      }
      index++;
    }
    return -1;
  }

  public String getLabel(int index) {
    return elements.get(index).label;
  }

  public String getSection(int index) {
    return elements.get(index).section;
  }

  protected void updateMax(double value) {
    if ((value > 0) && (value > maxPositive)) {
      maxPositive = value;
    }
    else if ((value < 0) && (-value > maxNegative)) {
      maxNegative = -value;
    }
  }

  public boolean containsSections() {
    return containsSections;
  }

  public boolean isSelected(int index) {
    return elements.get(index).selected;
  }

  public boolean isCurrent(int index) {
    return elements.get(index).current;
  }

  public boolean isFuture(int index) {
    return elements.get(index).future;
  }

  public String toString() {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < elements.size(); i++) {
      E element = elements.get(i);
      builder
        .append(Formatting.TWO_DIGIT_INTEGER_FORMAT.format(i))
        .append(": ")
        .append(element)
        .append("\n");
    }
    return builder.toString();
  }

}
