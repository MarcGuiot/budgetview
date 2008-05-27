package org.designup.picsou.model;

import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.metamodel.annotations.Key;
import org.crossbowlabs.globs.metamodel.fields.IntegerField;
import org.crossbowlabs.globs.metamodel.fields.StringField;
import org.crossbowlabs.globs.metamodel.utils.GlobTypeLoader;

public class User {

  public static final Integer SINGLETON_ID = 0;

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  public static StringField NAME;
  public static StringField PASSWORD;

  static {
    GlobTypeLoader.init(User.class);
  }
}
