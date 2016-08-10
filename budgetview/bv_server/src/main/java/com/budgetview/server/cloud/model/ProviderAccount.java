package com.budgetview.server.cloud.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class ProviderAccount {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @Target(Provider.class)
  public static LinkField PROVIDER;

  public static IntegerField PROVIDER_ID;

  public static StringField LABEL;

  public static IntegerField POSITION_MONTH;

  public static IntegerField POSITION_DAY;

  public static DoubleField POSITION;

  public static StringField ACCOUNT_TYPE;

  static {
    GlobTypeLoader.init(ProviderAccount.class, "accountEntity");
  }
}
