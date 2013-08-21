package org.designup.picsou.gui.projects.utils;

import org.designup.picsou.gui.model.ProjectStat;
import org.designup.picsou.model.Project;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.Utils;

import java.util.Comparator;

import static org.designup.picsou.model.Account.*;

public class ProjectComparator implements Comparator<Glob> {
  private GlobRepository repository;

  public ProjectComparator(GlobRepository repository) {
    this.repository = repository;
  }

  public int compare(Glob project1, Glob project2) {
    if (project1 == null && project2 == null) {
      return 0;
    }
    if (project1 == null) {
      return -1;
    }
    if (project2 == null) {
      return 1;
    }

    Glob stat1 = repository.find(Key.create(ProjectStat.TYPE,  project1.get(Project.ID)));
    Integer month1 = stat1 != null ? stat1.get(ProjectStat.FIRST_MONTH) : null;
    Glob stat2 = repository.find(Key.create(ProjectStat.TYPE,  project2.get(Project.ID)));
    Integer month2 = stat2 != null ? stat2.get(ProjectStat.FIRST_MONTH) : null;
    if (month1 == null && month2 == null) {
      return 0;
    }
    if (month1 == null) {
      return -1;
    }
    if (month2 == null) {
      return 1;
    }
    if (month1 < month2) {
      return -1;
    }
    if (month1 > month2) {
      return 1;
    }

    int nameDiff = Utils.compare(project1.get(Project.NAME), project2.get(Project.NAME));
    if (nameDiff != 0) {
      return nameDiff;
    }

    return project2.get(ID) - project1.get(ID);
  }
}
