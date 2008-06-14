package org.designup.picsou.server.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.MaxSize;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class Uncategorised {

  public static GlobType TYPE;

  @Key
  @MaxSize(100)
  public static StringField INFO;

  static {
    GlobTypeLoader.init(Uncategorised.class);
  }
}
