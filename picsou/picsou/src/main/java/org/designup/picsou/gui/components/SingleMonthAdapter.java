package org.designup.picsou.gui.components;

import org.designup.picsou.model.Month;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;

public class SingleMonthAdapter implements MonthSliderAdapter {
  private IntegerField monthField;

  public SingleMonthAdapter(IntegerField monthField) {
    this.monthField = monthField;
  }

  public String getText(Glob glob, GlobRepository repository) {
    return glob == null ? "" : convertToString(glob.get(monthField));
  }

  public String convertToString(Integer monthId) {
    return Month.getShortMonthLabel(monthId);
  }

  public String getMaxText() {
    int maxMonthId = -1;
    int maxLabelWidth = -1;
    for (int monthId = 200001; monthId <= 200012; monthId++) {
      int monthWidth = convertToString(monthId).length();
      if (monthWidth > maxLabelWidth) {
        maxLabelWidth = monthWidth;
        maxMonthId = monthId;
      }
    }
    return convertToString(maxMonthId);
  }

  public int getCurrentMonth(Glob glob, GlobRepository repository) {
    return glob.get(monthField);
  }

  public void setMonth(Glob glob, int selectedMonthId, GlobRepository repository) {
    repository.update(glob.getKey(), monthField, selectedMonthId);
  }
}
