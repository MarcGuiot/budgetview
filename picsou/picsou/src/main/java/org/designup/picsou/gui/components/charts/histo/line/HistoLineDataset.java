package org.designup.picsou.gui.components.charts.histo.line;

import org.designup.picsou.gui.components.charts.histo.utils.AbstractHistoDataset;
import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.utils.Lang;

public class HistoLineDataset extends AbstractHistoDataset<HistoLineElement> {

  public HistoLineDataset(String tooltipKey) {
    super(tooltipKey);
  }

  public void add(int id, double value, String label, String tooltip, String section, boolean current, boolean selected) {
    add(new HistoLineElement(id, label, tooltip, section, value, current, selected));
    updateMax(value);
  }

  public Double getValue(int index) {
    Double result = getElement(index).value;
    if (result == null) {
      return 0.0;
    }
    return result;
  }

  public String getTooltip(int index) {
    if ((index < 0) || (index >= size())) {
      return "";
    }
    return Lang.get(getTooltipKey(), getElement(index).tooltip, Formatting.toString(getValue(index)));
  }
}