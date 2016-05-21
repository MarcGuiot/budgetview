package com.budgetview.gui.components.charts.histo.diff;

import com.budgetview.shared.gui.histochart.utils.AbstractHistoDataset;
import com.budgetview.gui.description.Formatting;
import com.budgetview.utils.Lang;
import org.globsframework.model.Key;

import java.util.Set;

public class HistoDiffDataset extends AbstractHistoDataset<HistoDiffElement> {

  private int multiplier = 1;
  private Set<Key> keys;

  public HistoDiffDataset(String tooltipKey) {
    super(tooltipKey);
  }

  public void add(int id, double reference, double actual, String label, String tooltipLabel, String section, boolean isCurrent, boolean isSelected, boolean isFuture) {
    add(new HistoDiffElement(id, label, tooltipLabel, section, reference, actual, isCurrent, isSelected, isFuture));
    updateMax(reference);
    updateMax(actual);
  }

  public String getTooltip(int index, Set<Key> objectKey) {
    if ((index < 0) || (index >= size())) {
      return "";
    }
    return Lang.get(getTooltipKey(),
                    getElement(index).tooltip,
                    Formatting.toString(getReferenceValue(index)),
                    Formatting.toString(getActualValue(index)));
  }

  public double getReferenceValue(int index) {
    Double result = getElement(index).referenceValue;
    if (result == null) {
      return 0.0;
    }
    return result * multiplier;
  }

  public double getActualValue(int index) {
    Double result = getElement(index).actualValue;
    if (result == null) {
      return 0.0;
    }
    return result * multiplier;
  }

  public double getMaxPositiveValue() {
    if (multiplier > 0) {
      return super.getMaxPositiveValue();
    }
    return super.getMaxNegativeValue();
  }

  public double getMaxNegativeValue() {
    if (multiplier > 0) {
      return super.getMaxNegativeValue();
    }
    return super.getMaxPositiveValue();
  }

  public boolean isFuture(int index) {
    return getElement(index).future;
  }

  public boolean isCurrent(int index) {
    return getElement(index).current;
  }

  public void setInverted() {
    this.multiplier = -1;
  }

  public boolean isInverted() {
    return multiplier < 0;
  }

  public void setKeys(Set<Key> keys) {
    this.keys = keys;
  }

  public Set<Key> getKeys() {
    return keys;
  }
}