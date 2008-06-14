package org.designup.picsou.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

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
