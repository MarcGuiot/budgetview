package com.budgetview.shared.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.NoObfuscation;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

/** @deprecated */
public class TransactionValues {
  public static GlobType TYPE;

  @Key
  @NoObfuscation
  public static IntegerField ID;

  @Target(SeriesEntity.class)
  @NoObfuscation
  public static LinkField SERIES;

  @Target(AccountEntity.class)
  @NoObfuscation
  public static LinkField ACCOUNT;

  @NoObfuscation
  public static StringField LABEL;

  @NoObfuscation
  public static DoubleField AMOUNT;

  @Target(MonthEntity.class)
  @NoObfuscation
  public static LinkField BANK_MONTH;
  @NoObfuscation
  public static IntegerField BANK_DAY;

  @NoObfuscation
  public static IntegerField SEQUENCE_NUMBER;

  static {
    GlobTypeLoader.init(TransactionValues.class, "transactionValues");
  }
}
