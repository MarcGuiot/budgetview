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
  public static IntegerField ACTIVATION_STEP;

  public static final int ACTIVATION_IN_PROCESS = 1;
  public static final int ACTIVATION_OK = 2;
  public static final int ACTIVATION_FAIL_BAD_SIGNATURE = 3;
  public static final int ACTIVATION_FAIL_CAN_NOT_CONNECT = 4;
  public static final int ACTIVATION_FAIL_HTTP_REQUEST = 5;


  static {
    GlobTypeLoader.init(User.class, "user");
    KEY = org.globsframework.model.Key.create(TYPE, SINGLETON_ID);
  }
}
