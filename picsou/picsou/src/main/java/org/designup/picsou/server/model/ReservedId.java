package org.designup.picsou.server.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.MaxSize;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class ReservedId {

  public static GlobType TYPE;

  @Key
  @MaxSize(40)
  public static StringField TABLE_NAME;
  @Key
  public static IntegerField HIDDEN_USER;

  public static IntegerField LAST_RESERVED_ID;

  static {
    GlobTypeLoader.init(ReservedId.class);
  }
}
