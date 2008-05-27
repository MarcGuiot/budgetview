package org.designup.picsou.gui.model;

import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.metamodel.annotations.Key;
import org.crossbowlabs.globs.metamodel.annotations.Target;
import org.crossbowlabs.globs.metamodel.fields.DoubleField;
import org.crossbowlabs.globs.metamodel.fields.LinkField;
import org.crossbowlabs.globs.metamodel.utils.GlobTypeLoader;
import org.designup.picsou.model.Category;

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
