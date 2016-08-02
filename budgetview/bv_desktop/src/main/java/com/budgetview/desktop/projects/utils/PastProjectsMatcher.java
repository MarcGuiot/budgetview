package com.budgetview.desktop.projects.utils;

import com.budgetview.desktop.model.ProjectStat;
import com.budgetview.model.CurrentMonth;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatcher;

public class PastProjectsMatcher implements GlobMatcher {
  public boolean matches(Glob stat, GlobRepository repository) {
    Integer lastMonth = stat.get(ProjectStat.LAST_MONTH);
    return lastMonth != null && lastMonth < CurrentMonth.getCurrentMonth(repository);
  }
}
