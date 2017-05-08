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

  public static StringField LANG;

  public static StringField EMAIL;

  public static TimeStampField CREATION_DATE;

  @DefaultBoolean(false)
  public static BooleanField EMAIL_VERIFIED;

  @Target(Provider.class)
  public static LinkField PROVIDER;

  public static IntegerField PROVIDER_USER_ID;

  public static StringField PROVIDER_ACCESS_TOKEN;

  public static StringField STRIPE_CUSTOMER_ID;

  public static StringField STRIPE_TOKEN;

  public static StringField STRIPE_SUBSCRIPTION_ID;

  public static StringField LAST_STRIPE_INVOICE_EVENT_ID;

  public static DateField SUBSCRIPTION_END_DATE;

  static {
    GlobTypeLoader.init(CloudUser.class, "cloudUser");
  }
}
