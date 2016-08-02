package com.budgetview.desktop.model;

import com.budgetview.model.Project;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.DefaultDouble;
import org.globsframework.metamodel.annotations.DoublePrecision;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class ProjectStat {
  public static GlobType TYPE;

  @Key @Target(Project.class)
  public static LinkField PROJECT;

  @DefaultDouble(0.00)
  @DoublePrecision(4)
  public static DoubleField ACTUAL_AMOUNT;

  @DefaultDouble(0.00)
  @DoublePrecision(4)
  public static DoubleField PLANNED_AMOUNT;

  public static IntegerField FIRST_MONTH;

  public static IntegerField LAST_MONTH;

  static {
    GlobTypeLoader.init(ProjectStat.class);
  }
}
