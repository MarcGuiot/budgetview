package com.budgetview.desktop.description.stringifiers;

import com.budgetview.model.Month;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.model.format.utils.AbstractGlobStringifier;

public class MonthYearStringifier extends AbstractGlobStringifier implements GlobListStringifier{
  private IntegerField monthField;

  public MonthYearStringifier(IntegerField month) {
    monthField = month;
  }

  public String toString(GlobList list, GlobRepository repository) {
    if (list.isEmpty()) {
      return null;
    }
    return toString(list.get(0), repository);
  }

  public String toString(Glob glob, GlobRepository repository) {
    if (glob == null){
      return null;
    }
    Integer monthId = glob.get(monthField);
    if (monthId == null) {
      return null;
    }
    return Month.getShortMonthLabel(monthId) + " " + Month.toYearString(monthId);
  }
}
