package org.designup.picsou.server.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.BlobField;
import org.globsframework.metamodel.fields.LongField;
import org.globsframework.metamodel.fields.TimeStampField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

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
