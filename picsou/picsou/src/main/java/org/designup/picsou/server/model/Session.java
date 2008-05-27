package org.designup.picsou.server.model;

import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.metamodel.annotations.Key;
import org.crossbowlabs.globs.metamodel.fields.BlobField;
import org.crossbowlabs.globs.metamodel.fields.LongField;
import org.crossbowlabs.globs.metamodel.fields.TimeStampField;
import org.crossbowlabs.globs.metamodel.utils.GlobTypeLoader;

public class Session {
  public static GlobType TYPE;

  @Key
  public static LongField SESSION_ID;
  public static BlobField USER_ID;
  public static TimeStampField CREATION_DATE;

  static {
    GlobTypeLoader.init(Session.class);
  }
}
