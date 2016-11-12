package com.budgetview.server.cloud.model;

import com.budgetview.shared.model.Provider;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.DefaultBoolean;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.*;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.sqlstreams.annotations.AutoIncrement;

public class CloudUser {
  public static GlobType TYPE;

  @Key
  @AutoIncrement
  public static IntegerField ID;

  public static StringField EMAIL;

  public static BooleanField EMAIL_VERIFIED;

  @Target(Provider.class)
  public static LinkField PROVIDER;

  public static IntegerField PROVIDER_ID;

  public static StringField PROVIDER_ACCESS_TOKEN;

  public static StringField LAST_VALIDATION_CODE;

  public static DateField LAST_VALIDATION_DATE;

  public static DateField SUBSCRIPTION_END_DATE;

  static {
    GlobTypeLoader.init(CloudUser.class, "cloudUser");
  }
}
