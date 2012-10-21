package com.budgetview.shared.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.*;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class TransactionValues {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @Target(SeriesValues.class)
  public static LinkField SERIES_VALUES;

  @Target(AccountEntity.class)
  public static LinkField ACCOUNT;

  public static StringField LABEL;

  public static DoubleField AMOUNT;

  @Target(MonthEntity.class)
  public static LinkField BANK_MONTH;
  public static IntegerField BANK_DAY;

  public static BooleanField PLANNED;

  public static IntegerField SEQUENCE_NUMBER;

  static {
    GlobTypeLoader.init(TransactionValues.class, "transactionValues");
  }
}
