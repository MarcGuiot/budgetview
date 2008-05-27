package org.designup.picsou.server.model;

import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.metamodel.annotations.Key;
import org.crossbowlabs.globs.metamodel.annotations.MaxSize;
import org.crossbowlabs.globs.metamodel.fields.IntegerField;
import org.crossbowlabs.globs.metamodel.fields.StringField;
import org.crossbowlabs.globs.metamodel.utils.GlobTypeLoader;

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
