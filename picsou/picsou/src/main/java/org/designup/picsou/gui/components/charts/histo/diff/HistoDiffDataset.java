package org.designup.picsou.gui.components.charts.histo.diff;

import org.designup.picsou.gui.components.charts.histo.utils.AbstractHistoDataset;
import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.Key;

public class HistoDiffDataset extends AbstractHistoDataset<HistoDiffElement> {

  private int multiplier = 1;

  public HistoDiffDataset(String tooltipKey) {
    super(tooltipKey);
  }

  public void add(int id, double reference, double actual, String label, String tooltipLabel, String section, boolean isCurrent, boolean isSelected, boolean isFuture) {
    add(new HistoDiffElement(id, label, tooltipLabel, section, reference, actual, isCurrent, isSelected, isFuture));
    updateMax(reference);
    updateMax(actual);
  }

  public String getTooltip(int index, Key objectKey) {
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
}