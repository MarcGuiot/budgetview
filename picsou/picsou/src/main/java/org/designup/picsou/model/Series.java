package org.designup.picsou.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.NamingField;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class Series {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  public static StringField LABEL;

  @NamingField
  public static StringField NAME;

  @Target(BudgetArea.class)
  public static LinkField BUDGET_AREA;

  @Target(Category.class)
  public static LinkField DEFAULT_CATEGORY;

  static {
    GlobTypeLoader.init(Series.class);
  }
}
