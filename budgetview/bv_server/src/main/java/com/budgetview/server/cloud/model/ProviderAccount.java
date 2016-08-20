package com.budgetview.server.cloud.model;

import com.budgetview.shared.model.Provider;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.*;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.sqlstreams.annotations.AutoIncrement;

public class ProviderAccount {
  public static GlobType TYPE;

  @Key
  @AutoIncrement
  public static IntegerField ID;

  @Target(CloudUser.class)
  public static LinkField USER;

  @Target(Provider.class)
  public static LinkField PROVIDER;

  public static IntegerField PROVIDER_ACCOUNT_ID;

  public static IntegerField PROVIDER_BANK_ID;

  public static StringField PROVIDER_BANK_NAME;

  public static StringField NAME;

  public static StringField NUMBER;

  public static DoubleField POSITION;

  public static IntegerField POSITION_MONTH;

  public static IntegerField POSITION_DAY;

  public static StringField ACCOUNT_TYPE;

  public static BooleanField DELETED;

  static {
    GlobTypeLoader.init(ProviderAccount.class, "providerAccount");
  }
}
