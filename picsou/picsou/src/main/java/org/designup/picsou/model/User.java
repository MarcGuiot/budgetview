package org.designup.picsou.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.BlobField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class User {

  public static final Integer SINGLETON_ID = 0;
  public static org.globsframework.model.Key KEY;

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  public static StringField NAME;
  public static StringField PASSWORD;
  public static StringField MAIL;
  public static StringField ACTIVATION_CODE;
  public static BlobField SIGNATURE;

  static {
    GlobTypeLoader.init(User.class);
    KEY = org.globsframework.model.Key.create(TYPE, SINGLETON_ID);
  }
}
