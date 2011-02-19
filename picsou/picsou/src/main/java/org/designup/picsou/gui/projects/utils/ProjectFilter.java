package org.designup.picsou.gui.projects.utils;

import org.designup.picsou.model.ProjectItem;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatcher;

import java.util.SortedSet;

import static org.globsframework.model.utils.GlobMatchers.linkedTo;

public class ProjectFilter implements GlobMatcher {

  private Integer firstMonth;

  public ProjectFilter(SortedSet<Integer> months) {
    if (!months.isEmpty()) {
      firstMonth = months.first();
    }
  }

  public boolean matches(Glob project, GlobRepository repository) {
    if ((project == null) || (firstMonth == null)) {
      return false;
    }

    Integer lastItemMonth =
      repository.getAll(ProjectItem.TYPE, linkedTo(project.getKey(), ProjectItem.PROJECT))
        .getSortedSet(ProjectItem.MONTH)
        .last();

    return lastItemMonth >= firstMonth;
  }
}
