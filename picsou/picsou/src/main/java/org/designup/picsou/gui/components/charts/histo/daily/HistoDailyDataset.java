package org.designup.picsou.gui.components.charts.histo.daily;

import com.budgetview.shared.gui.dailychart.AbstractHistoDailyDataset;
import com.budgetview.shared.gui.dailychart.HistoDailyElement;
import com.budgetview.shared.utils.AmountFormat;
import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.model.Day;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.Key;

import java.util.Set;

public class HistoDailyDataset extends AbstractHistoDailyDataset {

  public HistoDailyDataset(String tooltipKey, Integer currentMonthId, Integer currentDayId, String currentDayLabel) {
    super(tooltipKey, currentMonthId, currentDayId, currentDayLabel);
  }

  public String getTooltip(int index, Set<Key> objectKeys) {
    if ((index < 0) || (index >= size()) || (objectKeys.isEmpty())) {
      return "";
    }

    Key objectKey = objectKeys.iterator().next();
    return Lang.get(getTooltipKey(),
                    Day.getFullLabel(objectKey),
                    AmountFormat.toStandardValueString(getValue(index, objectKey.get(Day.DAY) - 1)));
  }

  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("daily(").append(currentMonthId).append(currentDayId).append(")\n");
    for (HistoDailyElement element : this.elements) {
      builder.append("- ").append(element.id).append('\n');
      for (int dayIndex = 0; dayIndex < element.values.length; dayIndex++) {
        builder.append("    [").append(Formatting.TWO_DIGIT_INTEGER_FORMAT.format(dayIndex))
          .append("] ").append(element.values[dayIndex]);
        if (element.daySelections[dayIndex]) {
          builder.append('*');
        }
        if (dayIndex == element.minDay) {
          builder.append(" min");
        }
        builder.append("\n");
      }
    }
    return builder.toString();
  }

  public String toString(int monthIndex) {
    return getElement(monthIndex).toString();
  }

  public Key getKey(int monthIndex, int dayIndex) {
    return Key.create(Day.MONTH, getElement(monthIndex).id, Day.DAY, dayIndex + 1);
  }
}
