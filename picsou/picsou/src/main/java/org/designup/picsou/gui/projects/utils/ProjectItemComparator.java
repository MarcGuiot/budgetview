package org.designup.picsou.gui.projects.utils;

import org.designup.picsou.model.ProjectItem;
import org.globsframework.model.Glob;
import org.globsframework.utils.Utils;

import java.util.Comparator;

public class ProjectItemComparator implements Comparator<Glob> {
  public int compare(Glob item1, Glob item2) {
    if (item1 == null && item2 == null) {
      return 0;
    }
    if (item1 == null) {
      return -1;
    }
    if (item2 == null) {
      return 1;
    }

    int monthDiff = Utils.compare(item1.get(ProjectItem.MONTH), item2.get(ProjectItem.MONTH));
    if (monthDiff != 0) {
      return monthDiff;
    }

    return item1.get(ProjectItem.ID) - item2.get(ProjectItem.ID);
  }
}
