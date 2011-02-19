package org.designup.picsou.gui.projects.utils;

import org.designup.picsou.model.Project;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.model.utils.GlobUtils;

import java.util.Comparator;

public class ProjectStringifier implements GlobStringifier {
  public String toString(Glob glob, GlobRepository repository) {
    return GlobUtils.safeGet(glob, Project.NAME);
  }

  public Comparator<Glob> getComparator(GlobRepository repository) {
    return new ProjectComparator();
  }
}
