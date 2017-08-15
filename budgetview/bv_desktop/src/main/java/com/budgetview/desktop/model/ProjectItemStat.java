package com.budgetview.desktop.model;

import com.budgetview.model.ProjectItem;
import com.budgetview.model.util.TypeLoader;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.*;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.LinkField;

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

  @DefaultBoolean(false)
  public static BooleanField CATEGORIZATION_WARNING;

  static {
    TypeLoader.init(ProjectItemStat.class, "projectItemStat");
  }
}
