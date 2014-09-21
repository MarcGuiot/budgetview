package org.designup.picsou.gui.projects.utils;

import org.designup.picsou.triggers.projects.ProjectItemToSeriesTrigger;
import org.designup.picsou.triggers.projects.ProjectToSeriesGroupTrigger;
import org.globsframework.model.GlobRepository;

public class ProjectErrorsUpgrade {

  public static void createMissingGroupsAndSeries(GlobRepository repository) {
    ProjectToSeriesGroupTrigger.createGroupsForProjects(repository);
    ProjectItemToSeriesTrigger.createMissingSeries(repository);
  }
}
