package org.designup.picsou.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.DefaultBoolean;
import org.globsframework.metamodel.annotations.DefaultInteger;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.*;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.Glob;

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

  @Target(PremiumEvolutionState.class)
  @DefaultInteger(0)
  public static LinkField PREMIUM_EVOLUTION_STATE;

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
    GlobTypeLoader.init(User.class);
    KEY = org.globsframework.model.Key.create(TYPE, SINGLETON_ID);
  }

  public static boolean isDemoUser(Glob user) {
    return user.isTrue(User.IS_DEMO_USER);
  }

  public static PremiumEvolutionState getPremiumEvolutionState(Glob user) {
    if (user == null) {
      return null;
    }
    return PremiumEvolutionState.get(user.get(PREMIUM_EVOLUTION_STATE));
  }
}
