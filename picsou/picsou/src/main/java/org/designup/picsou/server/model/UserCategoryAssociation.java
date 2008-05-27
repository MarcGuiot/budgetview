package org.designup.picsou.server.model;

import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.metamodel.annotations.Key;
import org.crossbowlabs.globs.metamodel.annotations.MaxSize;
import org.crossbowlabs.globs.metamodel.fields.IntegerField;
import org.crossbowlabs.globs.metamodel.fields.LongField;
import org.crossbowlabs.globs.metamodel.fields.StringField;
import org.crossbowlabs.globs.metamodel.utils.GlobTypeLoader;
import org.crossbowlabs.globs.sqlstreams.annotations.AutoIncrement;

public class UserCategoryAssociation {

  public static GlobType TYPE;

  @Key
  @AutoIncrement
  public static IntegerField ID;

  public static LongField USER_ID;

  @MaxSize(100)
  public static StringField INFO;

  public static IntegerField CATEGORY_ID;
  public static IntegerField COUNT;

  static {
    GlobTypeLoader.init(UserCategoryAssociation.class);
  }
}
