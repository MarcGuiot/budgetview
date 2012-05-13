package org.designup.picsou.gui.components.charts.histo.line;

import org.designup.picsou.gui.components.charts.histo.utils.AbstractHistoDataset;
import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.Key;

public class HistoLineDataset extends AbstractHistoDataset<HistoLineElement> {

  private int multiplier = 1;

  public HistoLineDataset(String tooltipKey) {
    super(tooltipKey);
  }

  public void add(int id, double value, String label, String tooltip, String section,
                  boolean current, boolean future, boolean selected) {
    add(new HistoLineElement(id, label, tooltip, section, value, current, future, selected));
    updateMax(value);
  }

  public void setInverted() {
    multiplier = -1;
  }

  public Double getValue(int index) {
    Double result = getElement(index).value;
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

  public String getTooltip(int index, Key objectKey) {
    if ((index < 0) || (index >= size())) {
      return "";
    }
    return Lang.get(getTooltipKey(), getElement(index).tooltip, Formatting.toString(getValue(index)));
  }
}