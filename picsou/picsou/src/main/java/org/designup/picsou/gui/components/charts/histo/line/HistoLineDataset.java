package org.designup.picsou.gui.components.charts.histo.line;

import org.designup.picsou.gui.components.charts.histo.utils.AbstractHistoDataset;
import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.Key;

import java.util.Collections;
import java.util.Set;

public class HistoLineDataset extends AbstractHistoDataset<HistoLineElement> {

  private int multiplier = 1;
  private Set<Key> keys = Collections.emptySet();

  public HistoLineDataset(String tooltipKey) {
    super(tooltipKey);
  }

  public void add(int id, double value, String label, String tooltip, String section,
                  boolean current, boolean future, boolean selected) {
    add(new HistoLineElement(id, label, tooltip, section, value, current, future, selected));
    updateMax(value);
  }

  public void setKeys(Set<Key> keys) {
    this.keys = keys;
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

  public String getTooltip(int index, Set<Key> objectKey) {
    if ((index < 0) || (index >= size())) {
      return "";
    }
    return Lang.get(getTooltipKey(), getElement(index).tooltip, Formatting.toString(getValue(index)));
  }

  public Set<Key> getKeys() {
    return keys;
  }
}