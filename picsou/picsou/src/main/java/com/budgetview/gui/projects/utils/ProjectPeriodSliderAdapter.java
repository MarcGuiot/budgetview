package com.budgetview.gui.projects.utils;

import com.budgetview.gui.components.MonthSliderAdapter;
import com.budgetview.gui.description.stringifiers.MonthRangeFormatter;
import com.budgetview.gui.model.ProjectStat;
import com.budgetview.model.Project;
import com.budgetview.model.Month;
import com.budgetview.model.ProjectItem;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;

public class ProjectPeriodSliderAdapter implements MonthSliderAdapter {
  public String getText(Glob projectStat, GlobRepository repository) {
    if ((projectStat == null)) {
      return null;
    }
    Integer firstMonth = projectStat.get(ProjectStat.FIRST_MONTH);
    Integer lastMonth = projectStat.get(ProjectStat.LAST_MONTH);
    if (firstMonth == null || lastMonth == null) {
      return null;
    }
    if (Month.toYear(firstMonth) == Month.toYear(lastMonth)) {
      return MonthRangeFormatter.STANDARD.monthRangeInYear(firstMonth, lastMonth, Month.toYear(firstMonth));
    }
    return MonthRangeFormatter.STANDARD.monthRangeAcrossYears(firstMonth, lastMonth);
  }

  public String getMaxText() {
    int maxMonthId = -1;
    int maxLabelWidth = -1;
    for (int monthId = 200001; monthId <= 200012; monthId++) {
      int monthWidth = Month.getFullMonthLabel(monthId, true).length();
      if (monthWidth > maxLabelWidth) {
        maxLabelWidth = monthWidth;
        maxMonthId = monthId;
      }
    }
    return MonthRangeFormatter.STANDARD.monthRangeAcrossYears(maxMonthId, maxMonthId + 100);
  }

  public int getCurrentMonth(Glob projectStat, GlobRepository repository) {
    return projectStat.get(ProjectStat.FIRST_MONTH);
  }

  public void setMonth(Glob projectStat, int selectedMonthId, GlobRepository repository) {
    int delta = Month.distance(projectStat.get(ProjectStat.FIRST_MONTH), selectedMonthId);
    Integer projectId = projectStat.get(ProjectStat.PROJECT);
    Glob project = repository.get(Key.create(Project.TYPE, projectId));
    repository.startChangeSet();
    try {
      for (Glob item : repository.findLinkedTo(project, ProjectItem.PROJECT)) {
        int newMonth = Month.offset(item.get(ProjectItem.FIRST_MONTH), delta);
        repository.update(item.getKey(), ProjectItem.FIRST_MONTH, newMonth);
      }
    }
    finally {
      repository.completeChangeSet();
    }
  }
}
