package org.designup.picsou.gui.model;

import org.designup.picsou.model.ProjectItem;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.DefaultDouble;
import org.globsframework.metamodel.annotations.DoublePrecision;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class ProjectItemStat {
  public static GlobType TYPE;

  @Key @Target(ProjectItem.class)
  public static LinkField PROJECT_ITEM;

  @DefaultDouble(0.00)
  @DoublePrecision(4)
  public static DoubleField ACTUAL_AMOUNT;

  @DefaultDouble(0.00)
  @DoublePrecision(4)
  public static DoubleField PLANNED_AMOUNT;

  static {
    GlobTypeLoader.init(ProjectItemStat.class);
  }
}
