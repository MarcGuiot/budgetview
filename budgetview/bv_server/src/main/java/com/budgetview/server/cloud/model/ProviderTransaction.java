package com.budgetview.server.cloud.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.NoObfuscation;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.sqlstreams.annotations.AutoIncrement;

public class ProviderTransaction {
  public static GlobType TYPE;

  @Key
  @AutoIncrement
  public static IntegerField ID;

  @Target(CloudUser.class)
  public static LinkField USER;

  @Target(Provider.class)
  public static LinkField PROVIDER;

  public static IntegerField PROVIDER_ID;

  public static IntegerField CATEGORY;

  @Target(ProviderAccount.class)
  public static LinkField ACCOUNT;

  public static StringField LABEL;

  public static StringField ORIGINAL_LABEL;

  public static DoubleField AMOUNT;

  public static IntegerField OPERATION_MONTH;
  public static IntegerField OPERATION_DAY;

  public static IntegerField BANK_MONTH;
  public static IntegerField BANK_DAY;

  public static IntegerField SEQUENCE_NUMBER;

  static {
    GlobTypeLoader.init(ProviderTransaction.class);
  }
}