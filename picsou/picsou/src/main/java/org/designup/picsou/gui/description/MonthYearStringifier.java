package org.designup.picsou.gui.description;

import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.metamodel.fields.IntegerField;
import org.designup.picsou.model.Month;

public class MonthYearStringifier implements GlobListStringifier {
  private IntegerField monthField;

  public MonthYearStringifier(IntegerField month) {
    monthField = month;
  }

  public String toString(GlobList list, GlobRepository repository) {
    if (list.isEmpty()) {
      return null;
    }
    Integer monthId = list.get(0).get(monthField);
    if (monthId == null) {
      return null;
    }
    return Month.getShortMonthLabel(monthId) + " " + Month.toYearString(monthId);
  }
}
