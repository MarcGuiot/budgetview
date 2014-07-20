package org.designup.picsou.gui.projects.utils;

import org.designup.picsou.gui.model.ProjectStat;
import org.designup.picsou.model.CurrentMonth;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatcher;

public class CurrentProjectsMatcher implements GlobMatcher {
  public boolean matches(Glob stat, GlobRepository repository) {
    Integer lastMonth = stat.get(ProjectStat.LAST_MONTH);
    return lastMonth == null || lastMonth >= CurrentMonth.getCurrentMonth(repository);
  }
}
