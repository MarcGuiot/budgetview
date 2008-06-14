package org.designup.picsou.gui.model;

import org.designup.picsou.model.Category;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class GlobalStat {

  public static GlobType TYPE;

  @Key
  @Target(Category.class)
  public static LinkField CATEGORY;

  public static DoubleField MIN_INCOME;
  public static DoubleField MAX_INCOME;

  public static DoubleField MIN_EXPENSES;
  public static DoubleField MAX_EXPENSES;

  static {
    GlobTypeLoader.init(GlobalStat.class);
  }
}
