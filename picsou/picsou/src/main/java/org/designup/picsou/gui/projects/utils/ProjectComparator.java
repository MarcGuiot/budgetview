package org.designup.picsou.gui.projects.utils;

import org.designup.picsou.model.Project;
import org.globsframework.model.Glob;
import org.globsframework.utils.Utils;

import java.util.Comparator;

import static org.designup.picsou.model.Account.*;

public class ProjectComparator implements Comparator<Glob> {
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

    int nameDiff = Utils.compare(project1.get(Project.NAME), project2.get(Project.NAME));
    if (nameDiff != 0) {
      return nameDiff;
    }

    return project2.get(ID) - project1.get(ID);
  }
}
