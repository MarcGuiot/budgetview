package org.designup.picsou.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.DefaultBoolean;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.BlobField;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.Glob;

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
  public static IntegerField ACTIVATION_STATE;
  @DefaultBoolean(false)
  public static BooleanField CONNECTED;

  public static BooleanField IS_REGISTERED_USER;

  @DefaultBoolean(false)
  public static BooleanField IS_DEMO_USER;
  
  @DefaultBoolean(false)
  public static BooleanField AUTO_LOGIN;

  public static final int ACTIVATION_IN_PROGRESS = 1;
  public static final int ACTIVATION_OK = 2;
  public static final int ACTIVATION_FAILED_BAD_SIGNATURE = 3;
  public static final int ACTIVATION_FAILED_CAN_NOT_CONNECT = 4;
  public static final int ACTIVATION_FAILED_HTTP_REQUEST = 5;
  public static final int ACTIVATION_FAILED_MAIL_UNKNOWN = 6;
  // On n'a pas envoyé au serveur les mail, signature et code d'activation alors que c'est un utilisateur
  // enregistré (donc le a priori le repo a ete modifié manuellement)
  public static final int ACTIVATION_FAILED_MAIL_SENT = 8;
  public static final int ACTIVATION_FAILED_BAD_ACTIVATION_CODE = 9;
  public static final int ACTIVATION_FAILED = 10;


  static {
    GlobTypeLoader.init(User.class, "user");
    KEY = org.globsframework.model.Key.create(TYPE, SINGLETON_ID);
  }

  public static boolean isDemoUser(Glob user) {
    return user.get(User.IS_DEMO_USER);
  }
}
