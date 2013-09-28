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

    int sequenceNumberDiff = Utils.compare(item1.get(ProjectItem.SEQUENCE_NUMBER), item2.get(ProjectItem.SEQUENCE_NUMBER));
    if (sequenceNumberDiff != 0) {
      return sequenceNumberDiff;
    }

    return item1.get(ProjectItem.ID) - item2.get(ProjectItem.ID);
  }
}
