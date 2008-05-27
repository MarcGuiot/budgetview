package org.designup.picsou.server.model;

import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.metamodel.annotations.Key;
import org.crossbowlabs.globs.metamodel.fields.IntegerField;
import org.crossbowlabs.globs.metamodel.utils.GlobTypeLoader;

public class CategoryTimeStamp {

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  public static IntegerField LAST_TOUCH;

  static {
    GlobTypeLoader.init(CategoryTimeStamp.class);
  }
}
