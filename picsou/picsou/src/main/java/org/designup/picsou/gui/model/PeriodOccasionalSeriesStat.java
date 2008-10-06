package org.designup.picsou.gui.model;

import org.designup.picsou.model.Category;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.DefaultDouble;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class PeriodOccasionalSeriesStat {
  public static GlobType TYPE;

  @Key
  @Target(Category.class)
  public static LinkField CATEGORY;

  @DefaultDouble(0.0)
  public static DoubleField AMOUNT;

  static {
    GlobTypeLoader.init(PeriodOccasionalSeriesStat.class);
  }
}