package org.designup.picsou.server.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LongField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class CategoryCountByWord {
  public static GlobType TYPE;

  @Key
  public static LongField WORD_STATISTIC_ID;
  @Key
  public static IntegerField CATEGORY;

  public static IntegerField COUNT;

  static {
    GlobTypeLoader.init(CategoryCountByWord.class);
  }
}
