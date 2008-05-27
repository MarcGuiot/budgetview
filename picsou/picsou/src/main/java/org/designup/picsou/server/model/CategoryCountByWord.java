package org.designup.picsou.server.model;

import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.metamodel.annotations.Key;
import org.crossbowlabs.globs.metamodel.fields.IntegerField;
import org.crossbowlabs.globs.metamodel.fields.LongField;
import org.crossbowlabs.globs.metamodel.utils.GlobTypeLoader;

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
