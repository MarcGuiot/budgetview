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

public class CloudTransaction {
  public static GlobType TYPE;

  @Key
  @NoObfuscation
  public static IntegerField ID;

  @Target(CloudSeries.class)
  @NoObfuscation
  public static LinkField SERIES;

  @Target(CloudAccount.class)
  @NoObfuscation
  public static LinkField ACCOUNT;

  @NoObfuscation
  public static StringField LABEL;

  @NoObfuscation
  public static DoubleField AMOUNT;

  @Target(CloudMonth.class)
  @NoObfuscation
  public static LinkField BANK_MONTH;
  @NoObfuscation
  public static IntegerField BANK_DAY;

  @NoObfuscation
  public static IntegerField SEQUENCE_NUMBER;

  static {
    GlobTypeLoader.init(CloudTransaction.class, "transactionValues");
  }
}
