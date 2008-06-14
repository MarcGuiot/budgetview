package org.designup.picsou.server.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class CategoryTimeStamp {

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  public static IntegerField LAST_TOUCH;

  static {
    GlobTypeLoader.init(CategoryTimeStamp.class);
  }
}
