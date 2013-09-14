package org.designup.picsou.gui.projects.utils;

import org.designup.picsou.model.ProjectItem;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatcher;

import java.util.SortedSet;

import static org.globsframework.model.utils.GlobMatchers.linkedTo;

public class ProjectFilter implements GlobMatcher {

  private Integer firstMonth;
  private Integer currentMonth;

  public ProjectFilter(SortedSet<Integer> selectedMonths, Integer currentMonth) {
    this.currentMonth = currentMonth;
    if (!selectedMonths.isEmpty()) {
      firstMonth = selectedMonths.first();
    }
  }

  public boolean matches(Glob project, GlobRepository repository) {
    if ((project == null) || (firstMonth == null) || (currentMonth == null)) {
      return false;
    }

    Integer lastItemMonth =
      repository.getAll(ProjectItem.TYPE, linkedTo(project.getKey(), ProjectItem.PROJECT))
        .getSortedSet(ProjectItem.FIRST_MONTH)
        .last();

    return (lastItemMonth >= currentMonth) || (lastItemMonth >= firstMonth);
  }
}
