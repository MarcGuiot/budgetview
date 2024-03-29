package com.budgetview.model;

import com.budgetview.model.util.TypeLoader;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.DefaultBoolean;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.*;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;

public class User {

  public static final Integer SINGLETON_ID = 0;
  public static org.globsframework.model.Key KEY;

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  public static StringField NAME;
  public static StringField EMAIL;
  public static StringField ACTIVATION_CODE;
  public static BlobField SIGNATURE;

  @Target(LicenseActivationState.class)
  public static LinkField LICENSE_ACTIVATION_STATE;

  @DefaultBoolean(false)
  public static BooleanField CONNECTED;

  public static BooleanField IS_REGISTERED_USER;

  @DefaultBoolean(false)
  public static BooleanField IS_DEMO_USER;

  @DefaultBoolean(false)
  public static BooleanField AUTO_LOGIN;

  static {
    TypeLoader.init(User.class, "user");
    KEY = org.globsframework.model.Key.create(TYPE, SINGLETON_ID);
  }

  public static boolean isDemoUser(Glob user) {
    return user.isTrue(User.IS_DEMO_USER);
  }

  public static boolean isRegistered(GlobRepository repository) {
    Glob user = repository.get(KEY);
    return user != null && user.isTrue(IS_REGISTERED_USER);
  }
}
