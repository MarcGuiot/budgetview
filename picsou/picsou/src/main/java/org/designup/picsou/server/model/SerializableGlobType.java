package org.designup.picsou.server.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.BlobField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class SerializableGlobType {
  public static GlobType TYPE;
  @Key
  public static IntegerField ID;
  @Key
  public static StringField GLOB_TYPE_NAME;

  public static IntegerField VERSION;
  public static BlobField DATA;

  static {
    GlobTypeLoader.init(SerializableGlobType.class);
  }
}
