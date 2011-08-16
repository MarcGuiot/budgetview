package org.designup.picsou.gui.model;

import org.designup.picsou.model.Project;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class ProjectStat {
  public static GlobType TYPE;

  @Key @Target(Project.class)
  public static LinkField PROJECT;

  public static DoubleField ACTUAL_AMOUNT;

  public static DoubleField PLANNED_AMOUNT;

  static {
    GlobTypeLoader.init(ProjectStat.class);
  }
}
