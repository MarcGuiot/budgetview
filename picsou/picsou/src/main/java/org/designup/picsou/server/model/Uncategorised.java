package org.designup.picsou.server.model;

import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.metamodel.annotations.Key;
import org.crossbowlabs.globs.metamodel.annotations.MaxSize;
import org.crossbowlabs.globs.metamodel.fields.StringField;
import org.crossbowlabs.globs.metamodel.utils.GlobTypeLoader;

public class Uncategorised {

  public static GlobType TYPE;

  @Key
  @MaxSize(100)
  public static StringField INFO;

  static {
    GlobTypeLoader.init(Uncategorised.class);
  }
}
